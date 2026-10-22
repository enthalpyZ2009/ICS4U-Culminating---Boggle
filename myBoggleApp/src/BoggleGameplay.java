import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Implements core turn, board, AI, and round gameplay logic.
 */
public class BoggleGameplay extends MyBoggleApp {
    /**
     * Validates, scores, and applies one submitted word.
     * @param rawInput submitted text
     * @return status triplet: message, game-over flag, status code
     */
    public static String[] submitWord(String rawInput) {
        int cur = MyBoggleApp.currentPlayerIndex;
        String nm = MyBoggleApp.seatNames.get(cur);
        String word;
        if (rawInput == null) {
            word = "";
        } else {
            word = rawInput.trim().toLowerCase(Locale.ROOT);
        }

        if (word.isEmpty()) {
            return new String[] {"Please enter a word.", "false", "REJECT"};
        }
        for (int i = 0; i < word.length(); i++) {
            if (!Character.isLetter(word.charAt(i))) {
                return new String[] {"Use letters only.", "false", "REJECT"};
            }
        }
        if (word.length() < MyBoggleApp.userPrefs.minWordLength) {
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " tried \"" + word + "\" — too short.", "bad");
            if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                    && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                    && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
                BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                        "Invalid: \"" + word + "\" — too short.", "bad");
            }
            return new String[] {"Too short — need at least " + MyBoggleApp.userPrefs.minWordLength + " letters.", "false", "REJECT"};
        }
        if (MyBoggleApp.usedWords.contains(word)) {
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " tried \"" + word + "\" — already used.", "bad");
            if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                    && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                    && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
                BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                        "Invalid: \"" + word + "\" — duplicate.", "bad");
            }
            return new String[] {"That word was already played.", "false", "REJECT"};
        }
        boardModel.useGrid(board);
        if (!boardModel.isWordOnBoard(word)) {
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " tried \"" + word + "\" — not on board.", "bad");
            if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                    && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                    && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
                BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                        "Invalid: \"" + word + "\" — not on board.", "bad");
            }
            return new String[] {"Can't spell that from the board.", "false", "REJECT"};
        }
        if (!boardModel.isKnownWord(word)) {
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " tried \"" + word + "\" — not in dictionary.", "bad");
            if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                    && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                    && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
                BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                        "Invalid: \"" + word + "\" — not in dictionary.", "bad");
            }
            return new String[] {"Not in the dictionary.", "false", "REJECT"};
        }

        int points = word.length();
        int newScore = MyBoggleApp.seats.get(cur).scoreWord(word);
        syncSeats();
        MyBoggleApp.usedWords.add(word);

        MyBoggleApp.consecutivePasses = 0;
        MyBoggleApp.doublePassCycles = 0;
        MyBoggleApp.shakeAvailable = false;

        if (newScore >= MyBoggleApp.userPrefs.targetScore) {
            MyBoggleApp.gameOver = true;
            MyBoggleApp.winnerSeat = cur;
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " played \"" + word + "\" for +" + points
                    + " — reached " + MyBoggleApp.userPrefs.targetScore + " points and wins!", "winGold");
            if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                    && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                    && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
                BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                        "\"" + word + "\" +" + points + " — WIN", "winGold");
            }
            return new String[] {nm + " wins the game!", "true", "WIN"};
        }

        BoggleUI.appendToLogPane(MyBoggleApp.allLog, nm + " played \"" + word + "\" for +" + points + " points.", "score");
        if (MyBoggleApp.playerLogs != null && MyBoggleApp.currentPlayerIndex >= 0
                && MyBoggleApp.currentPlayerIndex < MyBoggleApp.playerLogs.length
                && MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex] != null) {
            BoggleUI.appendToLogPane(MyBoggleApp.playerLogs[MyBoggleApp.currentPlayerIndex],
                    "\"" + word + "\" +" + points, "score");
        }

        if (MyBoggleApp.seatNames.isEmpty()) {
            MyBoggleApp.currentPlayerIndex = 0;
        } else {
            MyBoggleApp.currentPlayerIndex = (MyBoggleApp.currentPlayerIndex + 1) % MyBoggleApp.seatNames.size();
        }
        return new String[] {"Good word — \"" + word + "\" for +" + points + ". Next player's turn.", "false", "OK"};
    }

    /**
     * Applies pass-turn rules for the active seat.
     * @return status triplet: message, game-over flag, status code
     */
    public static String[] passTurn() {
        int cur = MyBoggleApp.currentPlayerIndex;
        MyBoggleApp.seats.get(cur).passTurn();
        syncSeats();
        BoggleUI.appendToLogPane(MyBoggleApp.allLog, MyBoggleApp.seatNames.get(cur) + " passed.", "passLine");

        boolean shakeJustUnlocked = false;
        MyBoggleApp.consecutivePasses++;
        if (MyBoggleApp.consecutivePasses >= MyBoggleApp.seatNames.size()) {
            MyBoggleApp.consecutivePasses = 0;
            MyBoggleApp.doublePassCycles++;
            BoggleUI.appendToLogPane(MyBoggleApp.allLog, "All players passed.", "passLine");
            if (MyBoggleApp.doublePassCycles >= 2) {
                boolean wasLocked = !MyBoggleApp.shakeAvailable;
                MyBoggleApp.shakeAvailable = true;
                if (wasLocked) {
                    BoggleUI.appendToLogPane(MyBoggleApp.allLog,
                            "Shake Board is now available (two pass rounds).", "plain");
                    shakeJustUnlocked = true;
                }
            }
        }

        if (MyBoggleApp.seatNames.isEmpty()) {
            MyBoggleApp.currentPlayerIndex = 0;
        } else {
            MyBoggleApp.currentPlayerIndex = (MyBoggleApp.currentPlayerIndex + 1) % MyBoggleApp.seatNames.size();
        }
        if (shakeJustUnlocked) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MyBoggleApp.frame,
                    "Shake Board is now available!\n\nTwo full pass rounds completed — you can shake the board.",
                    "Shake Board unlocked",
                    JOptionPane.WARNING_MESSAGE));
        }
        return new String[] {MyBoggleApp.seatNames.get(cur) + " passed.", "false", "OK"};
    }

    /**
     * Clears all seat objects and synchronized seat lists.
     */
    public static void clearAllSeats() {
        seats.clear();
        seatNames.clear();
        seatIsAi.clear();
        seatScores.clear();
        seatWordLists.clear();
        seatPasses.clear();
        seatRoundWins.clear();
        aiSeatIndex = -1;
    }

    /**
     * Locks contest rules: Impossible AI, timer disabled (0 seconds).
     */
    public static void applyContestModePreset() {
        userPrefs.timerChoice = "Disabled";
        userPrefs.customTimer = "";
        userPrefs.timerLocked = true;
        userPrefs.aiDifficulty = "Impossible";
        if (homeTimeBox != null) {
            homeTimeBox.setSelectedItem("Disabled");
            homeTimeBox.setEnabled(false);
        }
        homeTimeLocked = true;
        if (homeAIDifficultyBox != null) {
            homeAIDifficultyBox.setSelectedItem("Impossible");
            homeAIDifficultyBox.setEnabled(false);
        }
        if (setGpTimeBox != null) {
            setGpTimeBox.setSelectedItem("Disabled");
            setGpTimeBox.setEnabled(false);
        }
        setGpTimeLocked = true;
        if (setGpAiDifficultyBox != null) {
            setGpAiDifficultyBox.setSelectedItem("Impossible");
            setGpAiDifficultyBox.setEnabled(false);
        }
    }

    /**
     * Refreshes widget enabled/disabled state according to selected mode.
     */
    public static void refreshModeDependentUi() {
        if ("AI vs AI Contest".equals(selectedHomeMode)
                || "AI vs AI Contest".equals(userPrefs.gameMode)) {
            applyContestModePreset();
            return;
        }
        if (homeTimeBox != null) {
            homeTimeBox.setEnabled(true);
        }
        if (setGpTimeBox != null) {
            setGpTimeBox.setEnabled(true);
        }
        boolean ai = "Player vs AI".equals(String.valueOf(homeModeBox == null ? "" : homeModeBox.getSelectedItem()))
                || "Multiplayers + AI".equals(String.valueOf(homeModeBox == null ? "" : homeModeBox.getSelectedItem()));
        if (homeAIDifficultyBox != null) {
            homeAIDifficultyBox.setEnabled(ai);
        }
        if (setGpAiDifficultyBox != null) {
            boolean setAi = "Player vs AI".equals(String.valueOf(setGpModeBox == null ? "" : setGpModeBox.getSelectedItem()))
                    || "Multiplayers + AI".equals(String.valueOf(setGpModeBox == null ? "" : setGpModeBox.getSelectedItem()));
            setGpAiDifficultyBox.setEnabled(setAi);
        }
    }

    /**
     * Parses and applies board rows from spaced text lines.
     * @param lines input lines
     * @return {@code true} when a full valid board is applied
     */
    public static boolean applyBoardFromSpacedLines(java.util.List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        int row = 0;
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            String[] parts = t.split("\\s+");
            if (parts.length < BOARD_SIZE) {
                String compact = t.replace(" ", "");
                if (compact.length() != BOARD_SIZE) {
                    continue;
                }
                parts = new String[BOARD_SIZE];
                for (int c = 0; c < BOARD_SIZE; c++) {
                    parts[c] = String.valueOf(compact.charAt(c));
                }
            }
            if (parts.length != BOARD_SIZE) {
                return false;
            }
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (parts[col].length() != 1 || !Character.isLetter(parts[col].charAt(0))) {
                    return false;
                }
                board[row][col] = Character.toUpperCase(parts[col].charAt(0));
            }
            row++;
            if (row >= BOARD_SIZE) {
                BoggleUI.repaintTrace();
                return true;
            }
        }
        return row == BOARD_SIZE;
    }

    /**
     * Resolves the contest board file path.
     * @return contest board file location
     */
    public static File contestBoardFile() {
        String base = System.getProperty("user.dir");
        File[] candidates = {
                new File(base, "boggle-java-gui/src/resources/test_boards/contest_board.txt"),
                new File(base, "src/resources/test_boards/contest_board.txt"),
        };
        for (File f : candidates) {
            if (f.isFile()) {
                return f.getAbsoluteFile();
            }
        }
        return candidates[0].getAbsoluteFile();
    }

    /**
     * Filters non-empty lines used for board parsing.
     * @param fileLines raw file lines
     * @return normalized board lines
     */
    public static java.util.List<String> extractContestBoardLines(java.util.List<String> fileLines) {
        java.util.List<String> plain = new ArrayList<>();
        for (String ln : fileLines) {
            if (ln != null && !ln.trim().isEmpty()) {
                plain.add(ln);
            }
        }
        return plain;
    }

    /**
     * Loads a contest board from file into pending board storage.
     * @param file source file
     * @return {@code true} when a valid board is parsed
     */
    public static boolean loadContestBoard(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        try {
            java.util.List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            java.util.List<String> boardLines = extractContestBoardLines(lines);
            char[][] trial = new char[BOARD_SIZE][BOARD_SIZE];
            char[][] savedBoard = board;
            board = trial;
            boolean ok = applyBoardFromSpacedLines(boardLines);
            if (ok) {
                pendingContestBoard = new char[BOARD_SIZE][BOARD_SIZE];
                for (int r = 0; r < BOARD_SIZE; r++) {
                    pendingContestBoard[r] = Arrays.copyOf(board[r], BOARD_SIZE);
                }
            } else {
                pendingContestBoard = null;
            }
            board = savedBoard;
            return ok;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Adds a player or AI seat to the current match.
     * @param name seat display name
     * @param isAiSeat whether the seat is AI-controlled
     */
    public static void appendSeat(String name, boolean isAiSeat) {
        PlayerSeat seat = new PlayerSeat(name, isAiSeat);
        seats.add(seat);
        seatNames.add(seat.getName());
        seatIsAi.add(seat.isAi());
        aiSeatIndex = -1;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i).isAi()) {
                aiSeatIndex = i;
                break;
            }
        }
        seatScores.add(seat.getScore());
        seatWordLists.add(seat.getWords());
        seatPasses.add(seat.getPassCount());
        seatRoundWins.add(seat.getRoundsWon());
    }

    /**
     * Randomizes play order for a new round.
     */
    public static void shuffleSeatOrder() {
        // Lobby option "Randomize order" — keep AI index tracking in sync when seats move.
        for (int i = seatNames.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            // Swap every parallel list so indices still refer to the same player.
            Collections.swap(seats, i, j);
            Collections.swap(seatNames, i, j);
            Collections.swap(seatScores, i, j);
            Collections.swap(seatWordLists, i, j);
            Collections.swap(seatPasses, i, j);
            Collections.swap(seatRoundWins, i, j);
            Collections.swap(seatIsAi, i, j);
            aiSeatIndex = -1;
            for (int k = 0; k < seats.size(); k++) {
                if (seats.get(k).isAi()) {
                    aiSeatIndex = k;
                    break;
                }
            }
        }
    }

    /**
     * Rebuilds synchronized seat lists from seat objects.
     */
    public static void syncSeats() {
        seatNames.clear();
        seatIsAi.clear();
        seatScores.clear();
        seatWordLists.clear();
        seatPasses.clear();
        seatRoundWins.clear();
        for (PlayerSeat seat : seats) {
            seatNames.add(seat.getName());
            seatIsAi.add(seat.isAi());
            seatScores.add(seat.getScore());
            seatWordLists.add(seat.getWords());
            seatPasses.add(seat.getPassCount());
            seatRoundWins.add(seat.getRoundsWon());
        }
        aiSeatIndex = -1;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i).isAi()) {
                aiSeatIndex = i;
                break;
            }
        }
    }

    /**
     * Clears scores and words for a fresh match on same seats.
     */
    public static void resetMatchState() {
        // Wipe scores and round progress but keep the same seat list (lobby already filled it).
        currentPlayerIndex = 0;
        currentRoundNumber = 1;
        gameOver = false;
        winnerSeat = -1;
        usedWords.clear();
        consecutivePasses = 0;
        doublePassCycles = 0;
        shakeAvailable = false;
        if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
            aiMoveTimer.stop();
        }
        aiMoveTimer = null;
        aiWaitMs = 0;
        for (int si = 0; si < seats.size(); si++) {
            seats.get(si).resetForMatch();
        }
        syncSeats();
        dragAutoSubmit = false;
        gamePaused = false;
    }

    /**
     * Resets round state and generates a new board.
     */
    public static void resetForNextRound() {
        // Same players and round-win counts; only this round's scores and board start over.
        gameOver = false;
        winnerSeat = -1;
        gamePaused = false;
        usedWords.clear();
        consecutivePasses = 0;
        doublePassCycles = 0;
        shakeAvailable = false;
        if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
            aiMoveTimer.stop();
        }
        aiMoveTimer = null;
        aiWaitMs = 0;
        for (int si = 0; si < seats.size(); si++) {
            seats.get(si).resetForRound();
        }
        syncSeats();
        generateBoggleBoard();
        dragAutoSubmit = false;
        BoggleUI.clearAllLogs();
        BoggleUI.appendToLogPane(allLog, "Round " + currentRoundNumber + " / " + userPrefs.totalRounds + " started.", "plain");
        BoggleUI.appendToLogPane(allLog, "Players: " + playerNamesLine() + ".", "plain");
        BoggleUI.appendToLogPane(allLog, "First turn: " + seatNames.get(currentPlayerIndex) + ".", "plain");
        BoggleUI.appendToLogPane(allLog, "Board generated.", "plain");
        BoggleUI.showFeedbackLine("Click or drag adjacent letters to build a word, then Submit or press Enter.",
                "HINT");
    }

    /**
     * Formats seated player names for log headers.
     * @return readable player list (for example, "A vs B")
     */
    public static String playerNamesLine() {
        if (seatNames.isEmpty()) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seatNames.size(); i++) {
            if (i > 0) {
                sb.append(" vs ");
            }
            sb.append(seatNames.get(i));
        }
        return sb.toString();
    }

    
    /**
     * Shakes dice and fills the letter grid.
     */
    public static void generateBoggleBoard() {
        // New letters — clear any half-built word on the old board.
        tracePath.clear();
        lastScoredWordPath.clear();
        traceDragging = false;
        pressR = -1;
        pressC = -1;
        pressDragged = false;
        boardModel.useGrid(board);
        boardModel.generateBoggleBoard();
        board = boardModel.grid();
        BoggleUI.repaintTrace();
    }

    /**
     * Schedules and runs one AI turn when {@code currentPlayerIndex == aiSeatIndex}.
     * Flow: disable human controls → countdown via {@link #aiWaitMs} (100 ms ticks) →
     * background thread collects all playable words → difficulty logic picks one word or pass →
     * submits or passes on the EDT if {@code moveSerial} still matches.
     */
    public static void runAiTurn() {
        // Only runs when it's the AI seat's turn: wait, search words in background, then submit or pass.
        if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
            aiMoveTimer.stop();
        }
        aiMoveTimer = null;
        if (gamePaused || gameOver || seatNames.isEmpty()) {
            aiWaitMs = 0;
            return;
        }
        if (currentPlayerIndex < 0 || currentPlayerIndex >= seats.size()
                || !seats.get(currentPlayerIndex).isAi()) {
            aiWaitMs = 0;
            return;
        }
        // AI vs AI: skip the artificial think pause entirely so moves are instant.
        boolean aiVsAi = "AI vs AI Contest".equals(selectedHomeMode)
                || "AI vs AI Contest".equals(userPrefs.gameMode);
        if (aiVsAi) {
            aiWaitMs = 0;
        }
        // Block typing while AI "thinks" and searches.
        if (wordField != null) {
            wordField.setEnabled(false);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
        }
        if (passButton != null) {
            passButton.setEnabled(false);
        }
        if (!aiVsAi && aiWaitMs <= 0) {
            aiWaitMs = userPrefs.aiThinkDelayMs;
        }
        aiMoveSerial++;  // bump so an older AI thread knows to stay quiet if we cancel or turn changes
        final int turnIndex = currentPlayerIndex;
        final int moveSerial = aiMoveSerial;
        if (aiWaitMs <= 0) {
            // Zero think delay — go straight to the worker, no Swing Timer round-trip.
            launchAiMoveWorker(turnIndex, moveSerial);
            return;
        }
        aiMoveTimer = new javax.swing.Timer(100, e -> {
            aiWaitMs -= 100;  // visible "thinking" pause before the AI picks a word
            if (aiWaitMs > 0) {
                return;
            }
            aiWaitMs = 0;
            if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
                aiMoveTimer.stop();
            }
            aiMoveTimer = null;
            launchAiMoveWorker(turnIndex, moveSerial);
        });
        aiMoveTimer.setRepeats(true);
        aiMoveTimer.start();
    }

    /**
     * Runs the AI's word search off the EDT and submits the pick (or passes) when it returns.
     * Serial guard drops late results if the turn was cancelled or already advanced.
     */
    private static void launchAiMoveWorker(int turnIndex, int moveSerial) {
        // Heavy DFS off EDT so the window stays responsive during AI turns and hints.
        new Thread(() -> {
            ArrayList<String> playable;
            try {
                playable = boardModel.findAllPlayableWordsSorted(usedWords, userPrefs.minWordLength);
            } catch (Exception ex) {
                return;
            }
            String pick = BoggleAi.forDifficulty(activeAiDifficulty).chooseWord(playable, userPrefs.minWordLength);
            final String aiPick = pick;
            // Must run submit/pass on EDT; serial guard drops late results after cancel or new turn.
            SwingUtilities.invokeLater(() -> {
                if (gameOver || seatNames.isEmpty()) {
                    return;
                }
                if (moveSerial != aiMoveSerial) {
                    return;
                }
                if (currentPlayerIndex != turnIndex || currentPlayerIndex >= seatNames.size()) {
                    return;
                }
                if (currentPlayerIndex < 0 || currentPlayerIndex >= seats.size()
                        || !seats.get(currentPlayerIndex).isAi()) {
                    return;
                }
                if (aiPick == null) {
                    BoggleUI.onPass();
                } else {
                    wordField.setText(aiPick);
                    BoggleUI.onSubmit();
                }
            });
        }, "boggle-ai-move").start();
    }

    /**
     * Sorts seat indices by round wins (insertion sort).
     * @param ranked seat indices to sort in place
     * @param n number of active entries in {@code ranked}
     */
    public static void sortSeatsByRounds(int[] ranked, int n) {
        for (int i = 1; i < n; i++) {
            int key = ranked[i];
            int j = i - 1;
            while (j >= 0) {
                int c = Integer.compare(seatRoundWins.get(key), seatRoundWins.get(ranked[j]));  // higher round wins first
                if (c == 0) {
                    c = seatNames.get(ranked[j]).compareToIgnoreCase(seatNames.get(key));
                }
                if (c <= 0) {
                    break;
                }
                ranked[j + 1] = ranked[j];
                j--;
            }
            ranked[j + 1] = key;
        }
    }

}
