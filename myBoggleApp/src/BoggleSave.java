import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Save-file path resolution, parsing, match restore, and persistence for myBoggle.
 */
public class BoggleSave {

    // --- Paths and file queries ---

    /**
     * Resolves the guest's save directory path.
     * @param guestName entered guest name
     * @return absolute directory path used for save files
     */
    public static File guestSaveGameDir(String guestName) {
        String n;
        if (guestName == null) {
            n = "";
        } else {
            n = guestName.trim();
        }
        String slug;
        if (n.isEmpty()) {
            slug = "Player";
        } else {
            String s = n.replaceAll("[^a-zA-Z0-9_-]+", "_");
            if (s.isEmpty() || ".".equals(s) || "..".equals(s)) {
                slug = "Player";
            } else {
                slug = s;
            }
        }
        return new File(new File(new File(System.getProperty("user.dir"), "src/resources").getAbsoluteFile(), "saves"), slug).getAbsoluteFile();
    }

    /**
     * Checks whether a save file contains an active match section.
     * @param file save file
     * @return {@code true} when player count indicates saved match data
     */
    public static boolean saveFileHasMatch(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.startsWith("#") || t.isEmpty()) {
                    continue;
                }
                if (t.startsWith("### ")) {
                    continue;
                }
                if (t.startsWith("playerCount:")) {
                    String v = t.substring("playerCount:".length()).trim();
                    return Integer.parseInt(v) > 0;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    // --- Read and parse ---

    /**
     * Parses key-value and block sections from save file lines.
     * @param lines raw file lines
     * @param saveValuesOut target map for key-value pairs
     * @param saveBlocksOut target map for BEGIN/END blocks
     * @return {@code true} on successful parse
     */
    public static boolean parseSave(List<String> lines, HashMap<String, String> saveValuesOut,
            HashMap<String, String> saveBlocksOut) {
        saveValuesOut.clear();
        saveBlocksOut.clear();
        for (int i = 0; i < lines.size(); ) {
            if (lines.get(i).trim().startsWith("### BEGIN ")) {
                String blockKey = lines.get(i).trim().substring("### BEGIN ".length()).trim();
                i++;
                StringBuilder sb = new StringBuilder();
                String endMark = "### END " + blockKey;
                boolean closed = false;
                while (i < lines.size()) {
                    String ln = lines.get(i);
                    if (ln.trim().equals(endMark)) {
                        i++;
                        closed = true;
                        break;
                    }
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(ln);
                    i++;
                }
                if (!closed) {
                    saveValuesOut.clear();
                    saveBlocksOut.clear();
                    return false;
                }
                saveBlocksOut.put(blockKey, sb.toString());
                continue;
            }
            if (lines.get(i).trim().isEmpty() || lines.get(i).trim().startsWith("#")) {
                i++;
                continue;
            }
            int colon = lines.get(i).trim().indexOf(':');
            if (colon > 0) {
                String key = lines.get(i).trim().substring(0, colon).trim();
                String val = lines.get(i).trim().substring(colon + 1).trim();
                saveValuesOut.put(key, val);
            }
            i++;
        }
        return true;
    }

    /**
     * Reads preferences stored inside a save file.
     * @param file save file
     * @return loaded properties (empty when missing/unreadable)
     */
    public static Properties readPrefs(File file) {
        Properties p = new Properties();
        if (file == null || !file.exists()) {
            return p;
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            HashMap<String, String> saveValues = new HashMap<>();
            HashMap<String, String> saveBlocks = new HashMap<>();
            if (!parseSave(lines, saveValues, saveBlocks)) {
                return p;
            }
            String body = saveBlocks.get("preferences");
            if (body != null && !body.isBlank()) {
                try (java.io.StringReader sr = new java.io.StringReader(body.trim())) {
                    p.load(sr);
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return p;
    }

    /**
     * Rebuilds in-memory match state from parsed save.txt content.
     * Log bodies are stored in pending* fields until game UI mounts.
     *
     * @param saveValues key: value lines from the save file
     * @param saveBlocks multiline sections (usedWords, player words, logs)
     * @return {@code true} if restore succeeded
     */
    public static boolean restoreGame(HashMap<String, String> saveValues, HashMap<String, String> saveBlocks) {
        try {
            MyBoggleApp.guestName = saveValues.getOrDefault("guestName", "Player");
            MyBoggleApp.selectedHomeMode = saveValues.getOrDefault("selectedHomeMode", "Player vs Player");
            MyBoggleApp.activeAiDifficulty = saveValues.getOrDefault("activeAiDifficulty", "Easy");
            MyBoggleApp.userPrefs.minWordLength = UserPreferences.parseSaveInt(saveValues, "minWordLength", 3);
            MyBoggleApp.userPrefs.targetScore = UserPreferences.parseSaveInt(saveValues, "targetScore", 50);
            MyBoggleApp.userPrefs.totalRounds = UserPreferences.parseSaveInt(saveValues, "totalRounds", 1);
            MyBoggleApp.userPrefs.timerSeconds = UserPreferences.parseSaveInt(saveValues, "timerSeconds", 0);
            MyBoggleApp.currentRoundNumber = Integer.parseInt(saveValues.getOrDefault("currentRoundNumber", "1"));
            if (MyBoggleApp.currentRoundNumber < 1) {
                MyBoggleApp.currentRoundNumber = 1;
            }
            MyBoggleApp.currentPlayerIndex = Integer.parseInt(saveValues.getOrDefault("currentPlayerIndex", "0"));
            if (MyBoggleApp.currentPlayerIndex < 0) {
                MyBoggleApp.currentPlayerIndex = 0;
            }
            MyBoggleApp.secondsLeft = Integer.parseInt(saveValues.getOrDefault("secondsLeft", "0"));
            if (MyBoggleApp.secondsLeft < 0) {
                MyBoggleApp.secondsLeft = 0;
            }
            MyBoggleApp.aiWaitMs = Integer.parseInt(saveValues.getOrDefault("aiWaitMs", "0"));
            if (MyBoggleApp.aiWaitMs < 0) {
                MyBoggleApp.aiWaitMs = 0;
            }
            if (MyBoggleApp.aiMoveTimer != null && MyBoggleApp.aiMoveTimer.isRunning()) {
                MyBoggleApp.aiMoveTimer.stop();
            }
            MyBoggleApp.aiMoveTimer = null;
            for (int r = 0; r < MyBoggleApp.BOARD_SIZE; r++) {
                String row = saveValues.getOrDefault("boardRow" + r, "");
                if (row.length() != MyBoggleApp.BOARD_SIZE) {
                    return false;
                }
                for (int c = 0; c < MyBoggleApp.BOARD_SIZE; c++) {
                    MyBoggleApp.board[r][c] = row.charAt(c);
                }
            }
            MyBoggleApp.usedWords.clear();
            String usedWordsBlock = saveBlocks.get("usedWords");
            if (usedWordsBlock != null && !usedWordsBlock.isEmpty()) {
                for (String line : usedWordsBlock.split("\n", -1)) {
                    String word = line.trim();
                    if (!word.isEmpty()) {
                        MyBoggleApp.usedWords.add(word);
                    }
                }
            }
            MyBoggleApp.consecutivePasses = Integer.parseInt(saveValues.getOrDefault("consecutivePasses", "0"));
            MyBoggleApp.doublePassCycles = Integer.parseInt(saveValues.getOrDefault("doublePassCycles", "0"));
            MyBoggleApp.shakeAvailable = Boolean.parseBoolean(saveValues.getOrDefault("shakeAvailable", "false"));
            int n = Integer.parseInt(saveValues.getOrDefault("playerCount", "0"));
            MyBoggleApp.seats.clear();
            MyBoggleApp.seatNames.clear();
            MyBoggleApp.seatIsAi.clear();
            MyBoggleApp.aiSeatIndex = -1;
            MyBoggleApp.seatScores.clear();
            MyBoggleApp.seatWordLists.clear();
            MyBoggleApp.seatPasses.clear();
            MyBoggleApp.seatRoundWins.clear();
            for (int i = 0; i < n; i++) {
                String name = saveValues.getOrDefault("player" + i + "Name", "Player " + (i + 1));
                boolean isAi = Boolean.parseBoolean(saveValues.getOrDefault("player" + i + "IsAi", "false"));
                BoggleGameplay.appendSeat(name, isAi);
                int lx = MyBoggleApp.seatNames.size() - 1;
                ArrayList<String> restoredWords = new ArrayList<>();
                String wordsBlock = saveBlocks.get("player" + i + "Words");
                if (wordsBlock != null && !wordsBlock.isEmpty()) {
                    for (String line : wordsBlock.split("\n", -1)) {
                        String word = line.trim();
                        if (!word.isEmpty()) {
                            restoredWords.add(word);
                        }
                    }
                }
                MyBoggleApp.seats.get(lx).setSavedState(
                        Integer.parseInt(saveValues.getOrDefault("player" + i + "Score", "0")),
                        Integer.parseInt(saveValues.getOrDefault("player" + i + "PassCount", "0")),
                        Integer.parseInt(saveValues.getOrDefault("player" + i + "RoundsWon", "0")),
                        restoredWords);
                BoggleGameplay.syncSeats();
            }
            if (MyBoggleApp.currentPlayerIndex >= MyBoggleApp.seatNames.size()) {
                MyBoggleApp.currentPlayerIndex = 0;
            }
            MyBoggleApp.gameOver = false;
            MyBoggleApp.winnerSeat = -1;

            MyBoggleApp.pendingMatchLogBody = saveBlocks.get("matchLog");
            if (MyBoggleApp.pendingMatchLogBody == null) {
                MyBoggleApp.pendingMatchLogBody = "";
            }
            MyBoggleApp.pendingPlayerLogBodies = new String[n];
            for (int i = 0; i < n; i++) {
                String txt = saveBlocks.get("playerLog" + i);
                if (txt == null) {
                    txt = "";
                }
                MyBoggleApp.pendingPlayerLogBodies[i] = txt;
            }

            String pathSpec = saveValues.getOrDefault("lastScoredPath", "");
            MyBoggleApp.lastScoredWordPath.clear();
            if (pathSpec != null && !pathSpec.isBlank()) {
                try {
                    for (String part : pathSpec.split(";")) {
                        if (part.isBlank()) {
                            continue;
                        }
                        String[] rc = part.split(",");
                        if (rc.length == 2) {
                            int r = Integer.parseInt(rc[0].trim());
                            int c = Integer.parseInt(rc[1].trim());
                            if (r >= 0 && r < MyBoggleApp.BOARD_SIZE && c >= 0 && c < MyBoggleApp.BOARD_SIZE) {
                                MyBoggleApp.lastScoredWordPath.add(new int[] {r, c});
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {
                    MyBoggleApp.lastScoredWordPath.clear();
                }
            }
            BoggleUI.repaintTrace();
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    // --- Write ---

    /**
     * Writes guest preferences to the save file (full match snapshot when a game is in progress).
     */
    public static void persistPrefs() {
        if (!MyBoggleApp.seatNames.isEmpty() && !MyBoggleApp.gameOver) {
            save(null, false);
            return;
        }
        if (MyBoggleApp.homeModeBox != null) {
            MyBoggleApp.userPrefs.readFromHomeUi();
        }
        save(null, false);
    }

    /**
     * Writes save.txt.
     *
     * @param prefsOverride optional properties block; {@code null} uses current user preferences
     * @param prefsOnly when {@code true}, never writes a match snapshot (preferences block only)
     */
    public static void save(Properties prefsOverride, boolean prefsOnly) {
        boolean includeMatch = !prefsOnly && prefsOverride == null
                && !MyBoggleApp.seatNames.isEmpty() && !MyBoggleApp.gameOver;
        Properties p = prefsOverride != null ? prefsOverride : buildSavePrefs();
        writeSaveFile(p, includeMatch);
    }

    /**
     * Writes the current match snapshot to a writer (pair of {@link #restoreGame}).
     * @param w destination writer
     */
    public static void writeSnap(PrintWriter w) {
        String gn = "Player";
        if (MyBoggleApp.guestName != null) {
            gn = MyBoggleApp.guestName;
        }
        String mode = "Player vs Player";
        if (MyBoggleApp.selectedHomeMode != null) {
            mode = MyBoggleApp.selectedHomeMode;
        }
        String aiDiff = "Easy";
        if (MyBoggleApp.activeAiDifficulty != null) {
            aiDiff = MyBoggleApp.activeAiDifficulty;
        }
        w.println("guestName: " + gn.replace('\r', ' ').replace('\n', ' '));
        w.println("selectedHomeMode: " + mode.replace('\r', ' ').replace('\n', ' '));
        w.println("activeAiDifficulty: " + aiDiff.replace('\r', ' ').replace('\n', ' '));
        w.println("minWordLength: " + MyBoggleApp.userPrefs.minWordLength);
        w.println("targetScore: " + MyBoggleApp.userPrefs.targetScore);
        w.println("totalRounds: " + MyBoggleApp.userPrefs.totalRounds);
        w.println("timerSeconds: " + MyBoggleApp.userPrefs.timerSeconds);
        w.println("currentRoundNumber: " + MyBoggleApp.currentRoundNumber);
        w.println("currentPlayerIndex: " + MyBoggleApp.currentPlayerIndex);
        w.println("secondsLeft: " + MyBoggleApp.secondsLeft);
        w.println("aiWaitMs: " + MyBoggleApp.aiWaitMs);
        w.println("boardSize: " + MyBoggleApp.BOARD_SIZE);
        for (int r = 0; r < MyBoggleApp.BOARD_SIZE; r++) {
            w.println("boardRow" + r + ": " + String.valueOf(MyBoggleApp.board[r]));
        }
        w.println("### BEGIN usedWords");
        for (String word : MyBoggleApp.usedWords) {
            if (word != null && !word.isEmpty()) {
                w.println(word.replace('\r', ' ').replace('\n', ' '));
            }
        }
        w.println("### END usedWords");
        w.println("consecutivePasses: " + MyBoggleApp.consecutivePasses);
        w.println("doublePassCycles: " + MyBoggleApp.doublePassCycles);
        w.println("shakeAvailable: " + MyBoggleApp.shakeAvailable);
        StringBuilder pathSpec = new StringBuilder();
        for (int[] p : MyBoggleApp.lastScoredWordPath) {
            if (pathSpec.length() > 0) {
                pathSpec.append(';');
            }
            pathSpec.append(p[0]).append(',').append(p[1]);
        }
        w.println("lastScoredPath: " + pathSpec.toString().replace('\r', ' ').replace('\n', ' '));
        w.println("playerCount: " + MyBoggleApp.seatNames.size());
        for (int i = 0; i < MyBoggleApp.seatNames.size(); i++) {
            String name = MyBoggleApp.seatNames.get(i);
            boolean isAi = i >= 0 && i < MyBoggleApp.seats.size() && MyBoggleApp.seats.get(i).isAi();
            String safeName = "";
            if (name != null) {
                safeName = name.replace('\r', ' ').replace('\n', ' ');
            }
            w.println("player" + i + "Name: " + safeName);
            w.println("player" + i + "IsAi: " + isAi);
            w.println("player" + i + "Score: " + MyBoggleApp.seatScores.get(i));
            w.println("player" + i + "PassCount: " + MyBoggleApp.seatPasses.get(i));
            w.println("player" + i + "RoundsWon: " + MyBoggleApp.seatRoundWins.get(i));
            w.println("### BEGIN player" + i + "Words");
            for (String word : MyBoggleApp.seatWordLists.get(i)) {
                if (word != null && !word.isEmpty()) {
                    w.println(word.replace('\r', ' ').replace('\n', ' '));
                }
            }
            w.println("### END player" + i + "Words");
        }
        String allLogText = "";
        if (MyBoggleApp.allLog != null) {
            Document allDoc = MyBoggleApp.allLog.getDocument();
            try {
                allLogText = allDoc.getText(0, allDoc.getLength());
            } catch (BadLocationException e) {
                String t = MyBoggleApp.allLog.getText();
                if (t != null) {
                    allLogText = t;
                }
            }
        }
        w.println("### BEGIN matchLog");
        if (!allLogText.isEmpty()) {
            w.print(allLogText);
            if (!allLogText.endsWith("\n")) {
                w.println();
            }
        }
        w.println("### END matchLog");
        if (MyBoggleApp.playerLogs != null) {
            for (int i = 0; i < MyBoggleApp.playerLogs.length; i++) {
                String body = "";
                if (MyBoggleApp.playerLogs[i] != null) {
                    Document logDoc = MyBoggleApp.playerLogs[i].getDocument();
                    try {
                        body = logDoc.getText(0, logDoc.getLength());
                    } catch (BadLocationException e) {
                        String t = MyBoggleApp.playerLogs[i].getText();
                        if (t != null) {
                            body = t;
                        }
                    }
                }
                w.println("### BEGIN playerLog" + i);
                if (!body.isEmpty()) {
                    w.print(body);
                    if (!body.endsWith("\n")) {
                        w.println();
                    }
                }
                w.println("### END playerLog" + i);
            }
        }
    }

    // --- Private write helpers ---

    private static Properties buildSavePrefs() {
        Properties p = new Properties();
        File file = new File(guestSaveGameDir(MyBoggleApp.guestName), "save.txt");
        p.putAll(readPrefs(file));
        p.setProperty("aiThinkDelayMs", String.valueOf(MyBoggleApp.userPrefs.aiThinkDelayMs));
        p.setProperty("allowBoardHint", String.valueOf(MyBoggleApp.userPrefs.allowBoardHint));
        p.setProperty("uiTheme", MyBoggleApp.userPrefs.uiTheme);
        p.setProperty("musicVolumePct", String.valueOf(MyBoggleApp.userPrefs.musicVolumePct));
        p.setProperty("sfxVolumePct", String.valueOf(MyBoggleApp.userPrefs.sfxVolumePct));
        p.setProperty("sfxEnabled", String.valueOf(MyBoggleApp.userPrefs.sfxEnabled));
        MyBoggleApp.userPrefs.writeGameplayTo(p, "quick.");
        return p;
    }

    private static void writeSaveFile(Properties prefs, boolean includeMatch) {
        String prefsBody = "";
        if (prefs != null && !prefs.isEmpty()) {
            try {
                StringWriter sw = new StringWriter();
                prefs.store(sw, null);
                prefsBody = sw.toString().trim();
            } catch (IOException ignored) {
            }
        }
        File file = new File(guestSaveGameDir(MyBoggleApp.guestName), "save.txt");
        File dir = file.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            return;
        }
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8), true)) {
            w.println("# myBoggle saved match (UTF-8). Logs and word lists use ### BEGIN/END blocks.");
            if (!includeMatch) {
                w.println("# Preferences only — no saved match in progress.");
            }
            w.println("### BEGIN preferences");
            if (!prefsBody.isEmpty()) {
                w.print(prefsBody);
                if (!prefsBody.endsWith("\n")) {
                    w.println();
                }
            }
            w.println("### END preferences");
            if (includeMatch) {
                writeSnap(w);
            }
        } catch (IOException ignored) {
        }
    }
}
