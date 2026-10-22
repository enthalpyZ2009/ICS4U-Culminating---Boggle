import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Builds and drives Swing UI screens, dialogs, and in-game interactions.
 */
public class BoggleUI extends BoggleGameplay {
    /**
     * Reapplies the active color theme to open windows.
     */
    public static void refreshTheme() {
        // User picked a new theme — repaint the whole frame and rebuild home widgets with new colors.
        if (frame == null) {
            return;
        }
        activePalette = BoggleTheme.select(userPrefs.uiTheme);
        try {
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception ignored) {
        }
        buildHome();
    }


    /**
     * Builds the home card panel and quick settings.
     */
    public static void buildHome() {
        if (homeHeaderPanel != null) {
            homeHeaderPanel.setBackground(activePalette.headerBg);
        }
        if (homeTitleLabel != null) {
            homeTitleLabel.setForeground(Color.WHITE);
        }
        if (homeSubtitleLabel != null) {
            homeSubtitleLabel.setForeground(activePalette.headerFgMuted);
        }
        if (homeUserLabel != null) {
            if (guestName == null || guestName.isBlank()) {
                homeUserLabel.setText("Playing as: Guest");
            } else {
                homeUserLabel.setText("Playing as: " + guestName.trim());
            }
            homeUserLabel.setForeground(activePalette.headerFgMuted);
        }
        if (homeBodyPanel != null) {
            homeBodyPanel.setBackground(activePalette.appBg);
        }
        if (homeStartBtn != null) {
            stylePrimaryButton(homeStartBtn);
        }
        if (homeResumeButton != null) {
            styleSecondaryButton(homeResumeButton);
            refreshHomeResumeButton();
        }
        if (homeSettingsBtn != null) {
            styleSecondaryButton(homeSettingsBtn);
        }
        if (homeHelpBtn != null) {
            styleSecondaryButton(homeHelpBtn);
            homeHelpBtn.setBackground(activePalette.chipNeutral);
        }
        if (homeQuickPanel != null) {
            homeQuickPanel.setBackground(activePalette.panelBg);
            javax.swing.border.TitledBorder quickBorder = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(activePalette.line), "Quick Settings");
            quickBorder.setTitleColor(activePalette.textMuted);
            homeQuickPanel.setBorder(BorderFactory.createCompoundBorder(quickBorder,
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            ArrayDeque<Container> quickDfs = new ArrayDeque<>();
            quickDfs.push(homeQuickPanel);
            while (!quickDfs.isEmpty()) {
                Container cur = quickDfs.pop();
                for (Component ch : cur.getComponents()) {
                    if (ch instanceof JPanel jp) {
                        jp.setBackground(activePalette.panelBg);
                        quickDfs.push(jp);
                    } else if (ch instanceof JLabel jl) {
                        if (!Color.RED.equals(jl.getForeground())) {
                            jl.setForeground(activePalette.textMuted);
                        }
                    } else if (ch instanceof JTextField tf) {
                        tf.setBackground(activePalette.inputBg);
                        tf.setCaretColor(activePalette.textMuted);
                        if ((tf == homeCustomTimeField && homeTimeLocked)
                                || (tf == homeCustomMinWordField && homeMinWordLocked)
                                || (tf == homeCustomTargetField && homeTargetLocked)
                                || (tf == homeCustomRoundsField && homeRoundsLocked)) {
                            tf.setForeground(activePalette.textQuiet);
                        } else {
                            tf.setForeground(activePalette.textMuted);
                        }
                    }
                    if (ch instanceof Container nested && !(ch instanceof JPanel)) {
                        quickDfs.push(nested);
                    }
                }
            }
        }

        if (panel != null) {
            panel.setBackground(activePalette.formBg);
        }
        if (settingsTitleLabel != null) {
            settingsTitleLabel.setForeground(activePalette.textStrong);
        }
        if (settingsSubtitleLabel != null) {
            settingsSubtitleLabel.setForeground(activePalette.textQuiet);
        }
        if (settingsTabs != null) {
            settingsTabs.setBackground(activePalette.formBg);
        }
        if (settingsSouth != null) {
            settingsSouth.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, activePalette.footerRule),
                    BorderFactory.createEmptyBorder(18, 16, 18, 16)));
        }
        if (settingsBackBtn != null) {
            styleFlatSettingsButton(settingsBackBtn, activePalette.buttonMuted, 12, 24);
        }
        if (settingsThemeTab != null) {
            settingsThemeTab.setBackground(activePalette.scrollTrackBg);
        }
        if (settingsThemeCard != null) {
            settingsThemeCard.setBackground(activePalette.panelBg);
            settingsThemeCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line, 1),
                    BorderFactory.createEmptyBorder(28, 24, 28, 24)));
        }
        if (settingsSoundTab != null) {
            settingsSoundTab.setBackground(activePalette.scrollTrackBg);
        }
        if (settingsSoundCard != null) {
            settingsSoundCard.setBackground(activePalette.panelBg);
            settingsSoundCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line, 1),
                    BorderFactory.createEmptyBorder(24, 24, 28, 24)));
        }
        if (settingsGameplayScroll != null) {
            Color sb = activePalette.scrollTrackBg;
            settingsGameplayScroll.getViewport().setBackground(sb);
            settingsGameplayScroll.setBackground(sb);
            Component view = settingsGameplayScroll.getViewport().getView();
            if (view instanceof JPanel jv) {
                jv.setBackground(sb);
            }
        }
        for (JPanel card : settingBlocks) {
            card.setBackground(activePalette.panelBg);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line, 1),
                    BorderFactory.createEmptyBorder(14, 16, 16, 16)));
        }
        if (panel != null) {
            ArrayDeque<Container> settingsDfs = new ArrayDeque<>();
            settingsDfs.push(panel);
            while (!settingsDfs.isEmpty()) {
                Container cur = settingsDfs.pop();
                for (Component ch : cur.getComponents()) {
                    if (ch instanceof JLabel jl) {
                        if (!Color.RED.equals(jl.getForeground())) {
                            Font f = jl.getFont();
                            if (f != null && (f.getStyle() & Font.BOLD) == Font.BOLD && f.getSize() == 13) {
                                jl.setForeground(activePalette.textStrong);
                            } else {
                                jl.setForeground(activePalette.textMuted);
                            }
                        }
                    } else if (ch instanceof JTextArea ta) {
                        ta.setForeground(activePalette.textMuted);
                    } else if (ch instanceof JCheckBox cb) {
                        cb.setForeground(activePalette.textMuted);
                    }
                    if (ch instanceof Container nested) {
                        settingsDfs.push(nested);
                    }
                }
            }
        }
        if (setGpRestoreDefaultButton != null) {
            styleFlatSettingsButton(setGpRestoreDefaultButton, activePalette.chipNeutral, 9, 16);
            setGpRestoreDefaultButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        }
        if (setGpSaveDefaultButton != null) {
            styleFlatSettingsButton(setGpSaveDefaultButton, activePalette.homeConfirm, 9, 16);
            setGpSaveDefaultButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        }
        if (setGpBuiltInFactoryButton != null) {
            styleFlatSettingsButton(setGpBuiltInFactoryButton, new Color(132, 100, 78), 9, 16);
            setGpBuiltInFactoryButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        }

        if (lobbyRoot != null) {
            lobbyRoot.setBackground(activePalette.appBg);
        }
        if (lobbyTop != null) {
            lobbyTop.setBackground(activePalette.appBg);
        }
        if (lobbyPlayersCard != null) {
            lobbyPlayersCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line, 2),
                    BorderFactory.createEmptyBorder(22, 26, 22, 26)));
        }
        if (lobbyStartBtn != null) {
            stylePrimaryButton(lobbyStartBtn);
            lobbyStartBtn.setFont(new Font("SansSerif", Font.BOLD, 28));
            lobbyStartBtn.setPreferredSize(new Dimension(420, 76));
            lobbyStartBtn.setMaximumSize(new Dimension(420, 76));
        }
        if (lobbyBackBtn != null) {
            styleSecondaryButton(lobbyBackBtn);
            lobbyBackBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
            lobbyBackBtn.setPreferredSize(new Dimension(260, 56));
            lobbyBackBtn.setMaximumSize(new Dimension(260, 56));
        }

        if (gameLivePanel != null) {
            gameLivePanel.setBackground(activePalette.appBg);
        }
        if (gameNorthPanel != null) {
            gameNorthPanel.setBackground(activePalette.appBg);
        }
        if (gameWestWrap != null) {
            gameWestWrap.setBackground(activePalette.appBg);
        }
        if (gameEastPanel != null) {
            gameEastPanel.setBackground(activePalette.appBg);
        }
        if (gameSouthPanel != null) {
            gameSouthPanel.setOpaque(false);
        }
        if (timerLabel != null) {
            timerLabel.setForeground(activePalette.textStrong);
        }
        if (turnLabel != null) {
            turnLabel.setForeground(activePalette.accent);
        }
        if (allLog != null) {
            allLog.setBackground(activePalette.editorBg);
        }
        if (playerLogs != null) {
            for (JTextPane pl : playerLogs) {
                if (pl != null) {
                    pl.setBackground(activePalette.editorBg);
                }
            }
        }
        if (wordField != null) {
            wordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line, 2),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        }
        if (playFeedbackLabel != null) {
            playFeedbackLabel.setBackground(activePalette.bannerBg);
            playFeedbackLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, activePalette.accent),
                    BorderFactory.createEmptyBorder(6, 16, 8, 16)));
        }
        if (wordPrompt != null) {
            wordPrompt.setForeground(activePalette.textStrong);
        }
        if (submitButton != null) {
            styleGamePrimaryButton(submitButton);
        }
        JButton[] gameSecondaries = {
                passButton, shakeButton, restartButton, clearWordsButton,
                gameMenuBtn, wordsHintButton, gameHelpBtn,
                saveQuitButton, quitWithoutSaveButton
        };
        for (JButton b : gameSecondaries) {
            if (b != null) {
                styleGameSecondaryButton(b);
            }
        }
        if (gameSaveBlock != null) {
            gameSaveBlock.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, activePalette.line),
                    BorderFactory.createEmptyBorder(14, 0, 0, 0)));
        }
        if (gameBoardWrap != null) {
            gameBoardWrap.setBackground(activePalette.boardFrame);
            gameBoardWrap.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.textStrong, 2),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        }
        if (boardGridPanel != null) {
            boardGridPanel.setBackground(activePalette.appBg);
        }
        if (playerCards != null) {
            highlightCurrentPlayerBorder();
        }
        repaintTrace();

        JComboBox<?>[] combos = new JComboBox<?>[] {
                homeModeBox, homeTimeBox, homeAIDifficultyBox, homeMinWordBox, homeTargetBox, homeRoundsBox,
                setGpModeBox, setGpTimeBox, setGpMinWordBox, setGpTargetBox, setGpRoundsBox,
                setGpAiDifficultyBox, setGpAiThinkDelayBox,
                lobbyPlayerCountBox, lobbyTurnOrderBox, lobbyFirstTurnBox,
                settingsThemeCombo
        };
        for (JComboBox<?> c : combos) {
            if (c != null) {
                styleComboBox(c);
            }
        }
        if (gamePaused) {
            pauseGameMatch();
        }
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Applies shared styling rules to combo boxes.
     * @param combo target combo box
     */
    public static void styleComboBox(JComboBox<?> combo) {
        if (combo == null) {
            return;
        }
        combo.setBackground(activePalette.listBg);
        combo.setForeground(activePalette.listHiFg);
        combo.setOpaque(true);
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 14f));
        if (combo.getEditor() != null) {
            if (combo.getEditor().getEditorComponent() instanceof JTextField) {
                JTextField tf = (JTextField) combo.getEditor().getEditorComponent();
                tf.setBackground(activePalette.listBg);
                tf.setForeground(activePalette.listHiFg);
                tf.setCaretColor(activePalette.listHiFg);
                tf.setOpaque(true);
            }
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (comp instanceof JLabel) {
                    JLabel c = (JLabel) comp;
                    c.setOpaque(true);
                    if (isSelected) {
                        c.setBackground(activePalette.listHiBg);
                        c.setForeground(activePalette.listHiFg);
                    } else {
                        c.setBackground(activePalette.listBg);
                        c.setForeground(activePalette.listHiFg);
                    }
                }
                return comp;
            }
        });
    }

    /**
     * Styles a flat settings button with custom padding and color.
     * @param b target button
     * @param bg background color
     * @param padTop vertical padding
     * @param padSides horizontal padding
     */
    public static void styleFlatSettingsButton(JButton b, Color bg, int padTop, int padSides) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg),
                BorderFactory.createEmptyBorder(padTop, padSides, padTop, padSides)));
    }
   


    /**
     * Shows the full game-rules help dialog.
     */
    public static void showWelcomeRulesDialog() {
        JLabel welcomeLabel = new JLabel("Dear " + guestName + ", welcome to Boggle!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        welcomeLabel.setForeground(activePalette.textStrong);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));

        final String tabGameRules = """
                The board is 5×5. Make words by moving to neighboring squares, including diagonals. Use each square at most once in one word.

                The word must follow a path on the board and appear in the game's word list.

                Words must be at least the minimum length chosen for this match (Quick Settings or Settings).

                Score equals the number of letters when a word is accepted.

                Be the first to reach the target score shown next to your name.

                Each word can only be scored once in the same match. After Shake Board or Restart, that changes (see below).

                Pass ends your turn with no word.

                When every player has passed in order twice in a row, Shake Board turns on. It rolls new letters. You may score words again that you already played on the old board.

                Restart starts the match over after you confirm (new board, scores reset).""";

        final String tabPlayScreen = """
                Letters: click cells, or click and drag along a path. The word appears in the Word box.

                Clear empties the path and the Word box.

                Submit or Enter sends the word in the Word box.

                Under the board: Pass, Shake Board (only when unlocked), and Restart.

                Pause opens a pause window. The turn timer and AI wait until you Resume or use Return to main menu. Closing the pause window resumes play, same as Resume.

                Hint shows words you can still play (longest first). Turn hints on under Settings → Gameplay → Allow board word list (Hint button).

                Help opens this full Game Rules dialog (same tabs as on the home screen).

                Menu saves the match and returns to the main menu. Quit the game saves and closes the program. If the game is paused, use Resume or Return to main menu from Pause first — then Menu or Quit work as usual.

                Closing the main window while a match is in progress saves it like a normal quit, so you can resume later.""";

        final String tabSettings = """
                Open Settings from the main menu. The screen has three tabs — Gameplay, Theme, and Sound. Each change saves immediately. Back to Home returns to the menu (your latest choices are already saved).

                Gameplay tab
                • Match — Player vs AI, Player vs Player, Multiplayers, Multiplayers + AI, or AI vs AI Contest (paste the teacher board into test_boards/contest_board.txt).
                • Rounds: 1, 3, 5, or Custom (type a number, then press the small ✓ next to the box).
                • Target score: 20, 50, 100, or Custom (same ✓ for Custom).
                • Turn timer — Time (sec): Disabled, 15 sec, 30 sec, or Custom (✓ for Custom).
                • Words — Min word: 3 letters, 4 letters, or Custom (✓ for Custom).
                • AI (when the mode includes AI): Difficulty — Easy, Medium, Hard, or Impossible. Think delay (AI) — Instant, Fast, Normal, Slow, or Very slow.
                • Help & shortcuts: Allow board word list (Hint button) — when checked, Hint works in a match.
                • Defaults: Save current as default remembers this Gameplay setup. Restore my default loads that save (or starter rules if you never saved). Return to factory defaults resets the built-in starter rules.

                Theme tab
                • UI theme: Default (Ocean), Healing (Soft), or Dark mode.

                Sound tab
                • Music — background music and end-of-match fanfare.
                • Sound effects — reject sounds and word score notes.

                Quick Settings on the home screen lets you change many of the same match options before Start New Game without opening full Settings.""";

        final String tabMainMenu = """
                Start New Game begins a fresh match with the rules you have set (home Quick Settings and/or Settings).

                Resume appears when a saved match exists; it continues that game.

                Settings opens the full settings screen.

                Game Rules opens this dialog.

                On the home screen, Quick Settings is a compact panel for mode, timer, target score, rounds, and minimum word length — useful for quick changes before you start.

                From a match, Menu and Quit the game both save progress; Menu returns to this menu, Quit closes the app.""";

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(activePalette.appBg);
        tabs.setForeground(activePalette.textStrong);
        String[] tabTitles = { "Game rules", "Play screen", "Settings", "Main menu" };
        String[] tabTexts = { tabGameRules, tabPlayScreen, tabSettings, tabMainMenu };
        for (int i = 0; i < tabTitles.length; i++) {
            JTextArea area = new JTextArea(tabTexts[i].trim());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font("SansSerif", Font.PLAIN, 17));
            area.setBackground(activePalette.appBg);
            area.setForeground(activePalette.textStrong);
            area.setBorder(BorderFactory.createEmptyBorder(12, 14, 16, 14));
            JScrollPane sp = new JScrollPane(area);
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.getViewport().setBackground(activePalette.appBg);
            sp.setPreferredSize(new Dimension(720, 460));
            tabs.addTab(tabTitles[i], sp);

            JLabel lbl = new JLabel(tabTitles[i]);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
            tabs.setTabComponentAt(i, lbl);
        }
        
        ChangeListener rulesTabColorListener = e -> {
            int selected = tabs.getSelectedIndex();
            for (int i = 0; i < tabs.getTabCount(); i++) {
                JLabel lbl = (JLabel) tabs.getTabComponentAt(i);
                if (lbl == null) {
                    continue;
                }
                if (i == selected) {
                    lbl.setForeground(new Color(30, 30, 30));
                } else {
                    lbl.setForeground(activePalette.textStrong);
                }
            }
        };
        tabs.addChangeListener(rulesTabColorListener);
        rulesTabColorListener.stateChanged(null);
        JDialog d = new JDialog((Frame) null, "Game Rules", true);
        
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(activePalette.appBg);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));
        root.add(welcomeLabel, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        JButton ok = new JButton("OK");
        ok.setFont(new Font("SansSerif", Font.BOLD, 15));
        ok.addActionListener(e -> d.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        south.setOpaque(false);
        south.add(ok);
        root.add(south, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    /**
     * Builds and mounts the main application frame and cards.
     */
    public static void mainWindow() {
        // One window, four cards: home → settings / lobby → fullscreen game.
        frame = new JFrame("myBoggle App");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            /**
             * Handles main window close: save match if in progress.
             * @param e E
             */
            public void windowClosing(WindowEvent e) {
                // Active match → full save; at home with no match → prefs only.
                if (!seatNames.isEmpty() && !gameOver) {
                    BoggleSave.save(null, false);
                } else {
                    BoggleSave.persistPrefs();
                }
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setSize(new Dimension(1000, 740));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        cards = new CardLayout();
        deck = new JPanel(cards);

        // Card names: home, settings, lobby, game (game panel built when match starts).
        JPanel homeShell = new JPanel(new BorderLayout());
        homeShell.add(buildTitleHeader(), BorderLayout.NORTH);
        homeShell.add(buildHomeBody(), BorderLayout.CENTER);

        deck.add(homeShell, "home");
        deck.add(buildSettingsPanel(), "settings");
        deck.add(buildLobbyPanel(), "lobby");

        gameMount = new JPanel(new BorderLayout());
        deck.add(gameMount, "game");

        frame.add(deck, BorderLayout.CENTER);
        cards.show(deck, "home");
        refreshHomeResumeButton();
        refreshSetUi();
        refreshTheme();
        BoggleAudio.refreshVolumes(userPrefs);
        BoggleAudio.startHomeMusic(userPrefs);
    }

    /**
     * Builds the branded header panel used by top-level cards.
     * @return header panel
     */
    public static JPanel buildTitleHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(activePalette.headerBg);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(34, 20, 28, 20));

        JLabel titleLabel = new JLabel("Boggle", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 54));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Classic Word Game", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        subtitleLabel.setForeground(activePalette.headerFgMuted);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(subtitleLabel);
        String userLine;
        if (guestName == null || guestName.isBlank()) {
            userLine = "Playing as: Guest";
        } else {
            userLine = "Playing as: " + guestName.trim();
        }
        JLabel userLabel = new JLabel(userLine, JLabel.CENTER);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
        userLabel.setForeground(activePalette.headerFgMuted);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        headerPanel.add(userLabel);
        homeHeaderPanel = headerPanel;
        homeTitleLabel = titleLabel;
        homeSubtitleLabel = subtitleLabel;
        homeUserLabel = userLabel;
        return headerPanel;
    }

    /**
     * Builds the Home screen body content.
     * @return home body panel
     */
    public static JPanel buildHomeBody() {
        JPanel homePanel = new JPanel();
        homeBodyPanel = homePanel;
        homePanel.setBackground(activePalette.appBg);
        homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
        homePanel.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        JButton startButton = new JButton("Start New Game");
        homeStartBtn = startButton;
        stylePrimaryButton(startButton);

        homeResumeButton = new JButton("Resume Saved Game");
        styleSecondaryButton(homeResumeButton);
        homeResumeButton.setBackground(activePalette.homeConfirm);
        homeResumeButton.setToolTipText("Continue the previously saved match.");

        JButton settingsButton = new JButton("Settings");
        homeSettingsBtn = settingsButton;
        styleSecondaryButton(settingsButton);

        JButton helpButton = new JButton("Game Rules");
        homeHelpBtn = helpButton;
        styleSecondaryButton(helpButton);
        helpButton.setBackground(activePalette.chipNeutral);

        JPanel centerColumn = new JPanel();
        centerColumn.setOpaque(false);
        centerColumn.setLayout(new BoxLayout(centerColumn, BoxLayout.Y_AXIS));
        centerColumn.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerColumn.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        centerColumn.add(Box.createVerticalStrut(22));
        centerColumn.add(startButton);
        centerColumn.add(Box.createVerticalStrut(18));
        centerColumn.add(homeResumeButton);
        centerColumn.add(Box.createVerticalStrut(18));
        centerColumn.add(settingsButton);
        centerColumn.add(Box.createVerticalStrut(18));
        centerColumn.add(helpButton);

        startButton.addActionListener(e -> {
            // Copy quick settings into runtime ints, save prefs, then open lobby with the right player count.
            userPrefs.readFromHomeUi();
            userPrefs.applyRuntimeRules();
            BoggleSave.persistPrefs();
            cards.show(deck, "lobby");
            if (lobbyPlayerCountBox == null) {
                return;
            }
            boolean pvp = "Player vs Player".equals(selectedHomeMode);
            boolean pva = "Player vs AI".equals(selectedHomeMode);
            boolean mpAi = "Multiplayers + AI".equals(selectedHomeMode);
            boolean contest = ("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode));
            lobbyPlayerCountBox.setEnabled(!pvp && !pva && !contest);
            if (lobbyCustomPlayerCountField != null) {
                lobbyCustomPlayerCountField.setVisible(!pvp && !pva && !contest
                        && "Custom".equals(lobbyPlayerCountBox.getSelectedItem()));
                lobbyCustomPlayerCountField.setEnabled(!pvp && !pva && !contest && lobbyCustomPlayerCountField.isVisible());
            }
            if (lobbySubmitPlayerCountBtn != null) {
                lobbySubmitPlayerCountBtn.setEnabled(!pvp && !pva && !contest && lobbyCustomPlayerCountField != null
                        && lobbyCustomPlayerCountField.isVisible());
            }
            if (lobbyPlayerCountErrorLabel != null) {
                lobbyPlayerCountErrorLabel.setVisible(false);
            }
            if (pvp) {
                lobbyPlayerCountBox.setSelectedItem("2");
                rebuildLobbyNameFields(2);
                if (lobbyTurnOrderBox != null) {
                    lobbyTurnOrderBox.setSelectedItem("As entered");
                }
            } else if (pva) {
                lobbyPlayerCountBox.setSelectedItem("2");
                rebuildLobbyNameFields(2);
                if (lobbyTurnOrderBox != null) {
                    lobbyTurnOrderBox.setSelectedItem("As entered");
                }
                lobbyPlayerCountLocked = false;
            } else if (contest) {
                applyContestModePreset();
                userPrefs.applyRuntimeRules();
                loadContestBoard(contestBoardFile());
                lobbyPlayerCountBox.setSelectedItem("2");
                rebuildLobbyNameFields(2);
                if (lobbyTurnOrderBox != null) {
                    lobbyTurnOrderBox.setSelectedItem("As entered");
                }
                lobbyPlayerCountLocked = true;
            } else if (mpAi) {
                lobbyPlayerCountBox.setSelectedItem("3");
                rebuildLobbyNameFields(3);
                if (lobbyTurnOrderBox != null) {
                    lobbyTurnOrderBox.setSelectedItem("As entered");
                }
                lobbyPlayerCountLocked = false;
            } else {
                lobbyPlayerCountBox.setSelectedItem("3");
                if (lobbyCustomPlayerCountField != null) {
                    lobbyCustomPlayerCountField.setVisible(false);
                    lobbyCustomPlayerCountField.setEnabled(false);
                    lobbyCustomPlayerCountField.setText("");
                    lobbyCustomPlayerCountField.setForeground(activePalette.textMuted);
                }
                if (lobbySubmitPlayerCountBtn != null) {
                    lobbySubmitPlayerCountBtn.setEnabled(false);
                    lobbySubmitPlayerCountBtn.setText("✓");
                }
                lobbyPlayerCountLocked = false;
                rebuildLobbyNameFields(3);
                if (lobbyTurnOrderBox != null) {
                    lobbyTurnOrderBox.setSelectedItem("As entered");
                }
            }
        });
        homeResumeButton.addActionListener(e -> {
            java.io.File f = new File(BoggleSave.guestSaveGameDir(guestName), "save.txt");
            if (!BoggleSave.saveFileHasMatch(f)) {
                JOptionPane.showMessageDialog(frame,
                        "No saved match available.",
                        "Resume game", JOptionPane.INFORMATION_MESSAGE);
                refreshHomeResumeButton();
                return;
            }
            SwingUtilities.invokeLater(() -> resumeSavedMatch());
        });
        settingsButton.addActionListener(e -> {
            refreshSetUi();
            cards.show(deck, "settings");
        });
        helpButton.addActionListener(e -> showWelcomeRulesDialog());

        JPanel quickPanel = buildQuickSettingsPanel();
        centerColumn.add(Box.createVerticalStrut(28));
        centerColumn.add(quickPanel);

        JPanel menuRow = new JPanel();
        menuRow.setOpaque(false);
        menuRow.setLayout(new BoxLayout(menuRow, BoxLayout.X_AXIS));
        menuRow.add(Box.createHorizontalGlue());
        menuRow.add(centerColumn);
        menuRow.add(Box.createHorizontalGlue());
        homePanel.add(menuRow);

        refreshHomeResumeButton();
        return homePanel;
    }

    
    /**
     * Refreshes Resume visibility/state based on saved match availability.
     */
    public static void refreshHomeResumeButton() {
        // Gray out Resume when save.txt has no active match (only prefs, or missing file).
        boolean hasSave = BoggleSave.saveFileHasMatch(new File(BoggleSave.guestSaveGameDir(guestName), "save.txt"));
        homeResumeButton.setEnabled(hasSave);
        homeResumeButton.setOpaque(true);
        if (hasSave) {
            // Bright button when playerCount > 0 in save file.
            homeResumeButton.setBackground(activePalette.homeConfirm);
            homeResumeButton.setForeground(Color.WHITE);
        } else {
            boolean dark = "DARK".equals(userPrefs.uiTheme);
            if (dark) {
                homeResumeButton.setBackground(new Color(48, 54, 64));
                homeResumeButton.setForeground(new Color(158, 168, 182));
            } else {
                homeResumeButton.setBackground(new Color(198, 206, 216));
                homeResumeButton.setForeground(new Color(88, 96, 108));
            }
        }
        if (hasSave) {
            homeResumeButton.setToolTipText("Continue your previously saved match. ");
        } else {
            homeResumeButton.setToolTipText("No saved match available.");
        }
    }

    /**
     * Applies primary button styling.
     * @param startButton target button
     */
    public static void stylePrimaryButton(JButton startButton) {
        startButton.setBackground(activePalette.action);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setFont(new Font("SansSerif", Font.BOLD, 23));
        startButton.setOpaque(true);
        startButton.setContentAreaFilled(true);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension homeBtn = new Dimension(292, 68);
        startButton.setPreferredSize(homeBtn);
        startButton.setMaximumSize(homeBtn);
    }

    /**
     * Applies secondary button styling.
     * @param settingsButton target button
     */
    public static void styleSecondaryButton(JButton settingsButton) {
        Font buttonFont = new Font("SansSerif", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(292, 60);
        settingsButton.setBackground(activePalette.homeOutline);
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setOpaque(true);
        settingsButton.setContentAreaFilled(true);
        settingsButton.setFont(buttonFont);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setPreferredSize(buttonSize);
        settingsButton.setMaximumSize(buttonSize);
    }

    /**
     * Builds the Quick Settings card on the Home screen.
     * @return quick settings panel
     */
    public static JPanel buildQuickSettingsPanel() {
        // Mode, timer, min word, target, rounds — same rules as Settings but on the home screen.
        JPanel quickPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        javax.swing.border.TitledBorder quickBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(activePalette.line), "Quick Settings");
        quickBorder.setTitleColor(activePalette.textMuted);
        quickPanel.setBorder(BorderFactory.createCompoundBorder(quickBorder,
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        quickPanel.setBackground(activePalette.panelBg);
        
        Dimension quickSz = new Dimension(520, 320);
        quickPanel.setPreferredSize(quickSz);
        quickPanel.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
        quickPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        homeModeBox = new JComboBox<>(new String[] {
                "Player vs AI", "Player vs Player", "Multiplayers", "Multiplayers + AI",
                "AI vs AI Contest"
        });
        homeModeBox.setSelectedItem("Player vs Player");
        styleComboBox(homeModeBox);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel modeRowLabel = new JLabel("Mode:");
        modeRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(modeRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickPanel.add(homeModeBox, gbc);

        JPanel timePanel = new JPanel(new GridBagLayout());
        timePanel.setOpaque(false);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.insets = new Insets(0, 2, 0, 2);
        tgbc.fill = GridBagConstraints.HORIZONTAL;
        tgbc.gridy = 0;

        homeTimeBox = new JComboBox<>(new String[] { "Disabled", "15 sec", "30 sec", "Custom" });
        styleComboBox(homeTimeBox);
        homeTimeBox.setSelectedItem("15 sec");

        tgbc.gridx = 0;
        tgbc.weightx = 0.6;
        timePanel.add(homeTimeBox, tgbc);

        homeCustomTimeField = new JTextField();
        homeCustomTimeField.setColumns(4);
        homeCustomTimeField.setPreferredSize(new Dimension(70, 28));
        homeCustomTimeField.setEnabled(false);
        homeCustomTimeField.setVisible(false);
        homeCustomTimeField.setForeground(activePalette.textMuted);
        homeCustomTimeField.setBackground(activePalette.inputBg);
        homeCustomTimeField.setCaretColor(activePalette.textMuted);

        tgbc.gridx = 1;
        tgbc.weightx = 0.4;
        timePanel.add(homeCustomTimeField, tgbc);

        homeSubmitTimeBtn = new JButton("✓");
        homeSubmitTimeBtn.setMargin(new Insets(0, 4, 0, 4));
        homeSubmitTimeBtn.setFocusable(false);
        homeSubmitTimeBtn.setEnabled(false);

        tgbc.gridx = 2;
        tgbc.weightx = 0;
        timePanel.add(homeSubmitTimeBtn, tgbc);

        homeTimeErrorLabel = new JLabel("* invalid input");
        homeTimeErrorLabel.setForeground(Color.RED);
        homeTimeErrorLabel.setVisible(false);

        tgbc.gridx = 3;
        tgbc.weightx = 0;
        timePanel.add(homeTimeErrorLabel, tgbc);

        homeSubmitTimeBtn.addActionListener(e -> customFieldLock(true, "time"));

        homeTimeBox.addActionListener(e -> {
            String selected = (String) homeTimeBox.getSelectedItem();
            boolean custom = "Custom".equals(selected);
            homeCustomTimeField.setVisible(custom);
            homeCustomTimeField.setEnabled(custom);
            homeSubmitTimeBtn.setEnabled(custom);
            if (custom) {
                homeCustomTimeField.setText("");
                homeCustomTimeField.requestFocus();
            }
            userPrefs.timerLocked = false;
            timePanel.revalidate();
            timePanel.repaint();
            if (userPrefs.uiNest <= 0) {
                userPrefs.readFromHomeUi();
                userPrefs.applyToSettingsUi();
                if (!setBulk && (seatNames.isEmpty() || gameOver)) {
                    BoggleSave.persistPrefs();
                }
            }
        });

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel timeRowLabel = new JLabel("Time (sec):");
        timeRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(timeRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickPanel.add(timePanel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel aiRowLabel = new JLabel("AI Difficulty:");
        aiRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(aiRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        homeAIDifficultyBox = new JComboBox<>(new String[] { "Easy", "Medium", "Hard", "Impossible" });
        homeAIDifficultyBox.setSelectedItem("Easy");
        styleComboBox(homeAIDifficultyBox);
        quickPanel.add(homeAIDifficultyBox, gbc);

        JPanel minWordPanel = new JPanel(new GridBagLayout());
        minWordPanel.setOpaque(false);
        GridBagConstraints mw = new GridBagConstraints();
        mw.insets = new Insets(0, 2, 0, 2);
        mw.fill = GridBagConstraints.HORIZONTAL;
        mw.gridy = 0;

        homeMinWordBox = new JComboBox<>(new String[] { "3 letters", "4 letters", "Custom" });
        styleComboBox(homeMinWordBox);
        mw.gridx = 0;
        mw.weightx = 0.6;
        minWordPanel.add(homeMinWordBox, mw);

        homeCustomMinWordField = new JTextField();
        homeCustomMinWordField.setColumns(4);
        homeCustomMinWordField.setPreferredSize(new Dimension(70, 28));
        homeCustomMinWordField.setEnabled(false);
        homeCustomMinWordField.setVisible(false);
        homeCustomMinWordField.setForeground(activePalette.textMuted);
        homeCustomMinWordField.setBackground(activePalette.inputBg);
        homeCustomMinWordField.setCaretColor(activePalette.textMuted);
        mw.gridx = 1;
        mw.weightx = 0.4;
        minWordPanel.add(homeCustomMinWordField, mw);

        homeSubmitMinWordBtn = new JButton("✓");
        homeSubmitMinWordBtn.setMargin(new Insets(0, 4, 0, 4));
        homeSubmitMinWordBtn.setFocusable(false);
        homeSubmitMinWordBtn.setEnabled(false);
        mw.gridx = 2;
        mw.weightx = 0;
        minWordPanel.add(homeSubmitMinWordBtn, mw);

        homeMinWordErrorLabel = new JLabel("* invalid input");
        homeMinWordErrorLabel.setForeground(Color.RED);
        homeMinWordErrorLabel.setVisible(false);
        mw.gridx = 3;
        minWordPanel.add(homeMinWordErrorLabel, mw);

        homeSubmitMinWordBtn.addActionListener(e -> customFieldLock(true, "minWord"));
        homeMinWordBox.addActionListener(e -> {
            boolean custom = "Custom".equals(homeMinWordBox.getSelectedItem());
            homeCustomMinWordField.setVisible(custom);
            homeCustomMinWordField.setEnabled(custom);
            homeSubmitMinWordBtn.setEnabled(custom);
            homeMinWordErrorLabel.setVisible(false);
            userPrefs.minWordLocked = false;
            homeSubmitMinWordBtn.setText("✓");
            if (custom) {
                homeCustomMinWordField.setText("");
                homeCustomMinWordField.requestFocus();
            }
            minWordPanel.revalidate();
            minWordPanel.repaint();
            if (userPrefs.uiNest <= 0) {
                userPrefs.readFromHomeUi();
                userPrefs.applyToSettingsUi();
                if (!setBulk && (seatNames.isEmpty() || gameOver)) {
                    BoggleSave.persistPrefs();
                }
            }
        });

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel minWordRowLabel = new JLabel("Min word:");
        minWordRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(minWordRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickPanel.add(minWordPanel, gbc);

        JPanel targetPanel = new JPanel(new GridBagLayout());
        targetPanel.setOpaque(false);
        GridBagConstraints tg = new GridBagConstraints();
        tg.insets = new Insets(0, 2, 0, 2);
        tg.fill = GridBagConstraints.HORIZONTAL;
        tg.gridy = 0;

        homeTargetBox = new JComboBox<>(new String[] { "Target 20", "Target 50", "Target 100", "Custom" });
        homeTargetBox.setSelectedItem("Target 50");
        styleComboBox(homeTargetBox);
        tg.gridx = 0;
        tg.weightx = 0.6;
        targetPanel.add(homeTargetBox, tg);

        homeCustomTargetField = new JTextField();
        homeCustomTargetField.setColumns(4);
        homeCustomTargetField.setPreferredSize(new Dimension(70, 28));
        homeCustomTargetField.setEnabled(false);
        homeCustomTargetField.setVisible(false);
        homeCustomTargetField.setForeground(activePalette.textMuted);
        homeCustomTargetField.setBackground(activePalette.inputBg);
        homeCustomTargetField.setCaretColor(activePalette.textMuted);
        tg.gridx = 1;
        tg.weightx = 0.4;
        targetPanel.add(homeCustomTargetField, tg);

        homeSubmitTargetBtn = new JButton("✓");
        homeSubmitTargetBtn.setMargin(new Insets(0, 4, 0, 4));
        homeSubmitTargetBtn.setFocusable(false);
        homeSubmitTargetBtn.setEnabled(false);
        tg.gridx = 2;
        tg.weightx = 0;
        targetPanel.add(homeSubmitTargetBtn, tg);

        homeTargetErrorLabel = new JLabel("* invalid input");
        homeTargetErrorLabel.setForeground(Color.RED);
        homeTargetErrorLabel.setVisible(false);
        tg.gridx = 3;
        targetPanel.add(homeTargetErrorLabel, tg);

        homeSubmitTargetBtn.addActionListener(e -> customFieldLock(true, "target"));
        homeTargetBox.addActionListener(e -> {
            boolean custom = "Custom".equals(homeTargetBox.getSelectedItem());
            homeCustomTargetField.setVisible(custom);
            homeCustomTargetField.setEnabled(custom);
            homeSubmitTargetBtn.setEnabled(custom);
            homeTargetErrorLabel.setVisible(false);
            userPrefs.targetLocked = false;
            homeSubmitTargetBtn.setText("✓");
            if (custom) {
                homeCustomTargetField.setText("");
                homeCustomTargetField.requestFocus();
            }
            targetPanel.revalidate();
            targetPanel.repaint();
            if (userPrefs.uiNest <= 0) {
                userPrefs.readFromHomeUi();
                userPrefs.applyToSettingsUi();
                if (!setBulk && (seatNames.isEmpty() || gameOver)) {
                    BoggleSave.persistPrefs();
                }
            }
        });

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel targetRowLabel = new JLabel("Target score:");
        targetRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(targetRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickPanel.add(targetPanel, gbc);

        JPanel roundsPanel = new JPanel(new GridBagLayout());
        roundsPanel.setOpaque(false);
        GridBagConstraints rd = new GridBagConstraints();
        rd.insets = new Insets(0, 2, 0, 2);
        rd.fill = GridBagConstraints.HORIZONTAL;
        rd.gridy = 0;

        homeRoundsBox = new JComboBox<>(new String[] { "1 round", "3 rounds", "5 rounds", "Custom" });
        homeRoundsBox.setSelectedItem("3 rounds");
        styleComboBox(homeRoundsBox);
        rd.gridx = 0;
        rd.weightx = 0.6;
        roundsPanel.add(homeRoundsBox, rd);

        homeCustomRoundsField = new JTextField();
        homeCustomRoundsField.setColumns(4);
        homeCustomRoundsField.setPreferredSize(new Dimension(70, 28));
        homeCustomRoundsField.setEnabled(false);
        homeCustomRoundsField.setVisible(false);
        homeCustomRoundsField.setForeground(activePalette.textMuted);
        homeCustomRoundsField.setBackground(activePalette.inputBg);
        homeCustomRoundsField.setCaretColor(activePalette.textMuted);
        rd.gridx = 1;
        rd.weightx = 0.4;
        roundsPanel.add(homeCustomRoundsField, rd);

        homeSubmitRoundsBtn = new JButton("✓");
        homeSubmitRoundsBtn.setMargin(new Insets(0, 4, 0, 4));
        homeSubmitRoundsBtn.setFocusable(false);
        homeSubmitRoundsBtn.setEnabled(false);
        rd.gridx = 2;
        rd.weightx = 0;
        roundsPanel.add(homeSubmitRoundsBtn, rd);

        homeRoundsErrorLabel = new JLabel("* invalid input");
        homeRoundsErrorLabel.setForeground(Color.RED);
        homeRoundsErrorLabel.setVisible(false);
        rd.gridx = 3;
        roundsPanel.add(homeRoundsErrorLabel, rd);

        homeSubmitRoundsBtn.addActionListener(e -> customFieldLock(true, "rounds"));
        homeRoundsBox.addActionListener(e -> {
            boolean custom = "Custom".equals(homeRoundsBox.getSelectedItem());
            homeCustomRoundsField.setVisible(custom);
            homeCustomRoundsField.setEnabled(custom);
            homeSubmitRoundsBtn.setEnabled(custom);
            homeRoundsErrorLabel.setVisible(false);
            homeRoundsLocked = false;
            homeSubmitRoundsBtn.setText("✓");
            if (custom) {
                homeCustomRoundsField.setText("");
                homeCustomRoundsField.requestFocus();
            }
            roundsPanel.revalidate();
            roundsPanel.repaint();
        });

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel roundsRowLabel = new JLabel("Rounds:");
        roundsRowLabel.setForeground(activePalette.textMuted);
        quickPanel.add(roundsRowLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickPanel.add(roundsPanel, gbc);

        
        homeModeBox.addActionListener(e -> {
            refreshModeDependentUi();
            if (userPrefs.uiNest <= 0) {
                userPrefs.readFromHomeUi();
                if (("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode))) {
                    applyContestModePreset();
                }
                userPrefs.applyToSettingsUi();
                if (!setBulk && (seatNames.isEmpty() || gameOver)) {
                    BoggleSave.persistPrefs();
                }
            }
        });
        userPrefs.uiNest++;
        try {
            refreshModeDependentUi();
        } finally {
            userPrefs.uiNest--;
        }

        homeQuickPanel = quickPanel;
        return quickPanel;
    }

    /**
     * Locks or unlocks custom-rule text fields.
     * @param homeScreen whether to target home widgets
     * @param rule rule key to lock/unlock
     */
    public static void customFieldLock(boolean homeScreen, String rule) {
        JTextField field;
        JLabel errorLabel;
        JButton submitButton;
        boolean locked;
        int minValue;
        int maxValue;

        // Pick which custom row (home quick settings, full settings, or lobby count).
        if ("playerCount".equals(rule)) {
            field = lobbyCustomPlayerCountField;
            errorLabel = lobbyPlayerCountErrorLabel;
            submitButton = lobbySubmitPlayerCountBtn;
            locked = lobbyPlayerCountLocked;
            minValue = 2;
            maxValue = 9;
        } else if (homeScreen) {
            if ("time".equals(rule)) {
                field = homeCustomTimeField;
                errorLabel = homeTimeErrorLabel;
                submitButton = homeSubmitTimeBtn;
                locked = userPrefs.timerLocked;
                minValue = 1;
                maxValue = Integer.MAX_VALUE;
            } else if ("minWord".equals(rule)) {
                field = homeCustomMinWordField;
                errorLabel = homeMinWordErrorLabel;
                submitButton = homeSubmitMinWordBtn;
                locked = userPrefs.minWordLocked;
                minValue = 3;
                maxValue = 10;
            } else if ("target".equals(rule)) {
                field = homeCustomTargetField;
                errorLabel = homeTargetErrorLabel;
                submitButton = homeSubmitTargetBtn;
                locked = userPrefs.targetLocked;
                minValue = 1;
                maxValue = 999;
            } else if ("rounds".equals(rule)) {
                field = homeCustomRoundsField;
                errorLabel = homeRoundsErrorLabel;
                submitButton = homeSubmitRoundsBtn;
                locked = userPrefs.roundsLocked;
                minValue = 1;
                maxValue = 20;
            } else {
                return;
            }
        } else {
            if ("time".equals(rule)) {
                field = setGpCustomTimeField;
                errorLabel = setGpTimeErrLabel;
                submitButton = setGpSubmitTimeBtn;
                locked = userPrefs.timerLocked;
                minValue = 1;
                maxValue = Integer.MAX_VALUE;
            } else if ("minWord".equals(rule)) {
                field = setGpCustomMinWordField;
                errorLabel = setGpMinWordErrLabel;
                submitButton = setGpSubmitMinWordBtn;
                locked = userPrefs.minWordLocked;
                minValue = 3;
                maxValue = 10;
            } else if ("target".equals(rule)) {
                field = setGpCustomTargetField;
                errorLabel = setGpTargetErrLabel;
                submitButton = setGpSubmitTargetBtn;
                locked = userPrefs.targetLocked;
                minValue = 1;
                maxValue = 999;
            } else if ("rounds".equals(rule)) {
                field = setGpCustomRoundsField;
                errorLabel = setGpRoundsErrLabel;
                submitButton = setGpSubmitRoundsBtn;
                locked = userPrefs.roundsLocked;
                minValue = 1;
                maxValue = 20;
            } else {
                return;
            }
        }
        if (field == null || !field.isVisible()) {
            return;
        }
        if (!locked) {
            // ✓ click: validate digits and range, then lock field until ✎ unlock.
            String text = field.getText().trim();
            boolean ok = false;
            if (!text.isEmpty()) {
                boolean digits = true;
                for (int i = 0; i < text.length(); i++) {
                    if (!Character.isDigit(text.charAt(i))) {
                        digits = false;
                        break;
                    }
                }
                if (digits) {
                    int value = Integer.parseInt(text);
                    ok = value >= minValue && value <= maxValue;
                    if (ok && "playerCount".equals(rule)) {
                        rebuildLobbyNameFields(value);
                    }
                }
            }
            if (ok) {
                errorLabel.setVisible(false);
                field.setEnabled(false);
                if ("playerCount".equals(rule) || homeScreen) {
                    field.setForeground(activePalette.textQuiet);
                } else {
                    field.setForeground(Color.DARK_GRAY);
                }
                submitButton.setText("✎");
                if ("playerCount".equals(rule)) {
                    lobbyPlayerCountLocked = true;
                } else if ("time".equals(rule)) {
                    userPrefs.timerLocked = true;
                    userPrefs.customTimer = text;
                } else if ("minWord".equals(rule)) {
                    userPrefs.minWordLocked = true;
                    userPrefs.customMinWord = text;
                } else if ("target".equals(rule)) {
                    userPrefs.targetLocked = true;
                    userPrefs.customTarget = text;
                } else {
                    userPrefs.roundsLocked = true;
                    userPrefs.customRounds = text;
                }
            } else {
                errorLabel.setVisible(true);
            }
        } else {
            // ✎ unlock — let the player edit the custom number again.
            field.setEnabled(true);
            if ("playerCount".equals(rule) || homeScreen) {
                field.setForeground(activePalette.textMuted);
            } else {
                field.setForeground(Color.BLACK);
            }
            field.requestFocus();
            submitButton.setText("✓");
            if ("playerCount".equals(rule)) {
                lobbyPlayerCountLocked = false;
            } else if ("time".equals(rule)) {
                userPrefs.timerLocked = false;
            } else if ("minWord".equals(rule)) {
                userPrefs.minWordLocked = false;
            } else if ("target".equals(rule)) {
                userPrefs.targetLocked = false;
            } else {
                userPrefs.roundsLocked = false;
            }
            errorLabel.setVisible(false);
        }
        if ("playerCount".equals(rule)) {
            return;
        }
        if (homeScreen) {
            userPrefs.readFromHomeUi();
        } else {
            userPrefs.readFromSettingsUi();
        }
        userPrefs.applyToHomeUi();
        userPrefs.applyToSettingsUi();
        if (!setBulk && (seatNames.isEmpty() || gameOver)) {
            BoggleSave.persistPrefs();
        }
    }

    public static final String[] AI_THINK_DELAY_LABELS = {
            "Instant (0 ms)",
            "Fast (400 ms)",
            "Normal (1.2 s)",
            "Slow (3 s)",
            "Very slow (6 s)" };
    public static final int[] AI_THINK_DELAY_VALS = { 0, 400, 1200, 3000, 6000 };

    /**
     * Builds the Settings root panel.
     * @return settings panel
     */
    public static JPanel buildSettingsPanel() {
        panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(activePalette.formBg);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 20, 12, 20));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 12, 8));
        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        settingsTitleLabel = title;
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(activePalette.textStrong);
        JLabel subtitle = new JLabel("Gameplay rules and appearance", SwingConstants.CENTER);
        settingsSubtitleLabel = subtitle;
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 15));
        subtitle.setForeground(activePalette.textQuiet);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        settingsTabs = new JTabbedPane();
        settingsTabs.setBackground(activePalette.formBg);
        settingsTabs.setFont(new Font("SansSerif", Font.BOLD, 15));
        settingsTabs.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        settingsTabs.addTab("Gameplay", buildSettingsGameplayTab());
        settingsTabs.addTab("Theme", buildSettingsThemeTab());
        settingsTabs.addTab("Sound", buildSettingsSoundTab());
        panel.add(settingsTabs, BorderLayout.CENTER);

        JButton back = new JButton("Back to Home");
        settingsBackBtn = back;
        styleFlatSettingsButton(back, activePalette.buttonMuted, 12, 24);
        back.setFont(new Font("SansSerif", Font.BOLD, 15));
        back.addActionListener(e -> {
            applySettings();
            frame.dispose();
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(new Dimension(1000, 740));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            cards.show(deck, "home");
            refreshHomeResumeButton();
BoggleAudio.startHomeMusic(userPrefs);
        });

        JPanel south = new JPanel(new BorderLayout());
        settingsSouth = south;
        south.setOpaque(false);
        south.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, activePalette.footerRule),
                BorderFactory.createEmptyBorder(18, 16, 18, 16)));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        btnRow.setOpaque(false);
        btnRow.add(back);
        south.add(btnRow, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the Settings gameplay tab scroll content.
     * @return gameplay settings scroll pane
     */
    public static JScrollPane buildSettingsGameplayTab() {
        settingBlocks.clear();
        final Color scrollBg = activePalette.scrollTrackBg;
        JPanel col = new JPanel();
        settingsGameplayColumn = col;
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setBorder(BorderFactory.createEmptyBorder(14, 0, 28, 0));

        col.add(wrapSettingsSection("Match",
                buildSetGpLabeledRow("Mode:", initSetGpModeBox())));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Rounds", buildSetGpRounds()));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Target score", buildSetGpTarget()));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Turn timer",
                buildSetGpLabeledRow("Time (sec):", buildSetGpTime())));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Words",
                buildSetGpLabeledRow("Min word:", buildSetGpMinWord())));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("AI",
                buildSetGpAi()));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Help & shortcuts",
                buildSetGpHelp()));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapSettingsSection("Defaults",
                buildSetGpDefaults()));

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrap.setOpaque(true);
        centerWrap.setBackground(scrollBg);
        centerWrap.add(col);

        JScrollPane sp = new JScrollPane(centerWrap,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(scrollBg);
        sp.setBackground(scrollBg);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        settingsGameplayScroll = sp;
        return sp;
    }

    /**
     * Wraps a settings section with border/title styling.
     * @param title section title
     * @param inner section content
     * @return wrapped section panel
     */
    public static JPanel wrapSettingsSection(String title, JComponent inner) {
        JPanel block = new JPanel(new BorderLayout(0, 12));
        block.setBackground(activePalette.panelBg);
        block.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.line, 1),
                BorderFactory.createEmptyBorder(14, 16, 16, 16)));
        block.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
        block.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingBlocks.add(block);

        JLabel cap = new JLabel(title);
        cap.setFont(new Font("SansSerif", Font.BOLD, 13));
        cap.setForeground(activePalette.textStrong);
        block.add(cap, BorderLayout.NORTH);
        inner.setOpaque(false);
        block.add(inner, BorderLayout.CENTER);
        return block;
    }

    /**
     * Builds a labeled form row for settings controls.
     * @param label label text
     * @param field input component
     * @return row panel
     */
    public static JPanel buildSetGpLabeledRow(String label, JComponent field) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 12);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(activePalette.textMuted);
        row.add(l, c);
        c.gridx = 1;
        c.insets = new Insets(3, 0, 3, 0);
        row.add(field, c);
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel glue = new JPanel();
        glue.setOpaque(false);
        row.add(glue, c);
        return row;
    }

    /**
     * Creates and initializes the settings game-mode combo box.
     * @return initialized combo box
     */
    public static JComboBox<String> initSetGpModeBox() {
        setGpModeBox = new JComboBox<>(new String[] {
                "Player vs AI", "Player vs Player", "Multiplayers", "Multiplayers + AI",
                "AI vs AI Contest" });
        styleComboBox(setGpModeBox);
        setGpModeBox.setPreferredSize(new Dimension(272, 34));
        setGpModeBox.setMaximumSize(new Dimension(320, 38));
        setGpModeBox.setMinimumSize(new Dimension(128, 28));
        setGpModeBox.addActionListener(e -> {
            if ("AI vs AI Contest".equals(String.valueOf(setGpModeBox.getSelectedItem()))) {
                applyContestModePreset();
            } else {
                refreshModeDependentUi();
            }
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        return setGpModeBox;
    }

    /**
     * Builds the settings section for turn timer options.
     * @return section panel
     */
    public static JPanel buildSetGpTime() {
        JPanel timePanel = new JPanel(new GridBagLayout());
        timePanel.setOpaque(false);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.insets = new Insets(0, 2, 0, 2);
        tgbc.gridy = 0;
        tgbc.anchor = GridBagConstraints.WEST;

        setGpTimeBox = new JComboBox<>(new String[] { "Disabled", "15 sec", "30 sec", "Custom" });
        styleComboBox(setGpTimeBox);
        setGpTimeBox.setPreferredSize(new Dimension(272, 34));
        setGpTimeBox.setMaximumSize(new Dimension(320, 38));
        setGpTimeBox.setMinimumSize(new Dimension(128, 28));
        tgbc.gridx = 0;
        tgbc.weightx = 0;
        tgbc.fill = GridBagConstraints.NONE;
        timePanel.add(setGpTimeBox, tgbc);

        setGpCustomTimeField = new JTextField();
        setGpCustomTimeField.setColumns(4);
        setGpCustomTimeField.setPreferredSize(new Dimension(70, 28));
        setGpCustomTimeField.setEnabled(false);
        setGpCustomTimeField.setVisible(false);
        setGpCustomTimeField.setForeground(activePalette.textMuted);
        tgbc.gridx = 1;
        tgbc.weightx = 0;
        tgbc.fill = GridBagConstraints.NONE;
        timePanel.add(setGpCustomTimeField, tgbc);

        setGpSubmitTimeBtn = new JButton("✓");
        setGpSubmitTimeBtn.setMargin(new Insets(0, 4, 0, 4));
        setGpSubmitTimeBtn.setFocusable(false);
        setGpSubmitTimeBtn.setEnabled(false);
        tgbc.gridx = 2;
        tgbc.weightx = 0;
        tgbc.fill = GridBagConstraints.NONE;
        timePanel.add(setGpSubmitTimeBtn, tgbc);

        setGpTimeErrLabel = new JLabel("* invalid input");
        setGpTimeErrLabel.setForeground(Color.RED);
        setGpTimeErrLabel.setVisible(false);
        tgbc.gridx = 3;
        tgbc.weightx = 0;
        tgbc.fill = GridBagConstraints.NONE;
        timePanel.add(setGpTimeErrLabel, tgbc);

        JPanel timeGlue = new JPanel();
        timeGlue.setOpaque(false);
        tgbc.gridx = 4;
        tgbc.weightx = 1;
        tgbc.fill = GridBagConstraints.HORIZONTAL;
        timePanel.add(timeGlue, tgbc);

        setGpSubmitTimeBtn.addActionListener(e -> {
            customFieldLock(false, "time");
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });

        setGpTimeBox.addActionListener(e -> {
            boolean custom = "Custom".equals(setGpTimeBox.getSelectedItem());
            setGpCustomTimeField.setVisible(custom);
            setGpCustomTimeField.setEnabled(custom);
            setGpSubmitTimeBtn.setEnabled(custom);
            setGpTimeErrLabel.setVisible(false);
            setGpTimeLocked = false;
            setGpSubmitTimeBtn.setText("✓");
            if (custom) {
                setGpCustomTimeField.setText("");
                setGpCustomTimeField.requestFocus();
            }
            timePanel.revalidate();
            timePanel.repaint();
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        return timePanel;
    }

    /**
     * Builds the settings section for minimum word length options.
     * @return section panel
     */
    public static JPanel buildSetGpMinWord() {
        JPanel minWordPanel = new JPanel(new GridBagLayout());
        minWordPanel.setOpaque(false);
        GridBagConstraints mw = new GridBagConstraints();
        mw.insets = new Insets(0, 2, 0, 2);
        mw.gridy = 0;
        mw.anchor = GridBagConstraints.WEST;

        setGpMinWordBox = new JComboBox<>(new String[] { "3 letters", "4 letters", "Custom" });
        styleComboBox(setGpMinWordBox);
        setGpMinWordBox.setPreferredSize(new Dimension(272, 34));
        setGpMinWordBox.setMaximumSize(new Dimension(320, 38));
        setGpMinWordBox.setMinimumSize(new Dimension(128, 28));
        mw.gridx = 0;
        mw.weightx = 0;
        mw.fill = GridBagConstraints.NONE;
        minWordPanel.add(setGpMinWordBox, mw);

        setGpCustomMinWordField = new JTextField();
        setGpCustomMinWordField.setColumns(4);
        setGpCustomMinWordField.setPreferredSize(new Dimension(70, 28));
        setGpCustomMinWordField.setEnabled(false);
        setGpCustomMinWordField.setVisible(false);
        setGpCustomMinWordField.setForeground(activePalette.textMuted);
        mw.gridx = 1;
        mw.weightx = 0;
        mw.fill = GridBagConstraints.NONE;
        minWordPanel.add(setGpCustomMinWordField, mw);

        setGpSubmitMinWordBtn = new JButton("✓");
        setGpSubmitMinWordBtn.setMargin(new Insets(0, 4, 0, 4));
        setGpSubmitMinWordBtn.setFocusable(false);
        setGpSubmitMinWordBtn.setEnabled(false);
        mw.gridx = 2;
        mw.weightx = 0;
        mw.fill = GridBagConstraints.NONE;
        minWordPanel.add(setGpSubmitMinWordBtn, mw);

        setGpMinWordErrLabel = new JLabel("* invalid input");
        setGpMinWordErrLabel.setForeground(Color.RED);
        setGpMinWordErrLabel.setVisible(false);
        mw.gridx = 3;
        mw.fill = GridBagConstraints.NONE;
        minWordPanel.add(setGpMinWordErrLabel, mw);

        JPanel minGlue = new JPanel();
        minGlue.setOpaque(false);
        mw.gridx = 4;
        mw.weightx = 1;
        mw.fill = GridBagConstraints.HORIZONTAL;
        minWordPanel.add(minGlue, mw);

        setGpSubmitMinWordBtn.addActionListener(e -> {
            customFieldLock(false, "minWord");
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        setGpMinWordBox.addActionListener(e -> {
            boolean custom = "Custom".equals(setGpMinWordBox.getSelectedItem());
            setGpCustomMinWordField.setVisible(custom);
            setGpCustomMinWordField.setEnabled(custom);
            setGpSubmitMinWordBtn.setEnabled(custom);
            setGpMinWordErrLabel.setVisible(false);
            setGpMinWordLocked = false;
            setGpSubmitMinWordBtn.setText("✓");
            if (custom) {
                setGpCustomMinWordField.setText("");
                setGpCustomMinWordField.requestFocus();
            }
            minWordPanel.revalidate();
            minWordPanel.repaint();
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        return minWordPanel;
    }

    /**
     * Builds the settings section for target score options.
     * @return section panel
     */
    public static JPanel buildSetGpTarget() {
        JPanel targetPanel = new JPanel(new GridBagLayout());
        targetPanel.setOpaque(false);
        GridBagConstraints tg = new GridBagConstraints();
        tg.insets = new Insets(0, 2, 0, 2);
        tg.gridy = 0;
        tg.anchor = GridBagConstraints.WEST;

        setGpTargetBox = new JComboBox<>(new String[] { "Target 20", "Target 50", "Target 100", "Custom" });
        styleComboBox(setGpTargetBox);
        setGpTargetBox.setPreferredSize(new Dimension(272, 34));
        setGpTargetBox.setMaximumSize(new Dimension(320, 38));
        setGpTargetBox.setMinimumSize(new Dimension(128, 28));
        tg.gridx = 0;
        tg.weightx = 0;
        tg.fill = GridBagConstraints.NONE;
        targetPanel.add(setGpTargetBox, tg);

        setGpCustomTargetField = new JTextField();
        setGpCustomTargetField.setColumns(4);
        setGpCustomTargetField.setPreferredSize(new Dimension(70, 28));
        setGpCustomTargetField.setEnabled(false);
        setGpCustomTargetField.setVisible(false);
        setGpCustomTargetField.setForeground(activePalette.textMuted);
        tg.gridx = 1;
        tg.weightx = 0;
        tg.fill = GridBagConstraints.NONE;
        targetPanel.add(setGpCustomTargetField, tg);

        setGpSubmitTargetBtn = new JButton("✓");
        setGpSubmitTargetBtn.setMargin(new Insets(0, 4, 0, 4));
        setGpSubmitTargetBtn.setFocusable(false);
        setGpSubmitTargetBtn.setEnabled(false);
        tg.gridx = 2;
        tg.weightx = 0;
        tg.fill = GridBagConstraints.NONE;
        targetPanel.add(setGpSubmitTargetBtn, tg);

        setGpTargetErrLabel = new JLabel("* invalid input");
        setGpTargetErrLabel.setForeground(Color.RED);
        setGpTargetErrLabel.setVisible(false);
        tg.gridx = 3;
        tg.fill = GridBagConstraints.NONE;
        targetPanel.add(setGpTargetErrLabel, tg);

        JPanel targetGlue = new JPanel();
        targetGlue.setOpaque(false);
        tg.gridx = 4;
        tg.weightx = 1;
        tg.fill = GridBagConstraints.HORIZONTAL;
        targetPanel.add(targetGlue, tg);

        setGpSubmitTargetBtn.addActionListener(e -> {
            customFieldLock(false, "target");
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        setGpTargetBox.addActionListener(e -> {
            boolean custom = "Custom".equals(setGpTargetBox.getSelectedItem());
            setGpCustomTargetField.setVisible(custom);
            setGpCustomTargetField.setEnabled(custom);
            setGpSubmitTargetBtn.setEnabled(custom);
            setGpTargetErrLabel.setVisible(false);
            setGpTargetLocked = false;
            setGpSubmitTargetBtn.setText("✓");
            if (custom) {
                setGpCustomTargetField.setText("");
                setGpCustomTargetField.requestFocus();
            }
            targetPanel.revalidate();
            targetPanel.repaint();
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        return targetPanel;
    }

    /**
     * Builds the settings section for round count options.
     * @return section panel
     */
    public static JPanel buildSetGpRounds() {
        JPanel roundsPanel = new JPanel(new GridBagLayout());
        roundsPanel.setOpaque(false);
        GridBagConstraints rd = new GridBagConstraints();
        rd.insets = new Insets(0, 2, 0, 2);
        rd.gridy = 0;
        rd.anchor = GridBagConstraints.WEST;

        setGpRoundsBox = new JComboBox<>(new String[] { "1 round", "3 rounds", "5 rounds", "Custom" });
        styleComboBox(setGpRoundsBox);
        setGpRoundsBox.setPreferredSize(new Dimension(272, 34));
        setGpRoundsBox.setMaximumSize(new Dimension(320, 38));
        setGpRoundsBox.setMinimumSize(new Dimension(128, 28));
        rd.gridx = 0;
        rd.weightx = 0;
        rd.fill = GridBagConstraints.NONE;
        roundsPanel.add(setGpRoundsBox, rd);

        setGpCustomRoundsField = new JTextField();
        setGpCustomRoundsField.setColumns(4);
        setGpCustomRoundsField.setPreferredSize(new Dimension(70, 28));
        setGpCustomRoundsField.setEnabled(false);
        setGpCustomRoundsField.setVisible(false);
        setGpCustomRoundsField.setForeground(activePalette.textMuted);
        rd.gridx = 1;
        rd.weightx = 0;
        rd.fill = GridBagConstraints.NONE;
        roundsPanel.add(setGpCustomRoundsField, rd);

        setGpSubmitRoundsBtn = new JButton("✓");
        setGpSubmitRoundsBtn.setMargin(new Insets(0, 4, 0, 4));
        setGpSubmitRoundsBtn.setFocusable(false);
        setGpSubmitRoundsBtn.setEnabled(false);
        rd.gridx = 2;
        rd.weightx = 0;
        rd.fill = GridBagConstraints.NONE;
        roundsPanel.add(setGpSubmitRoundsBtn, rd);

        setGpRoundsErrLabel = new JLabel("* invalid input");
        setGpRoundsErrLabel.setForeground(Color.RED);
        setGpRoundsErrLabel.setVisible(false);
        rd.gridx = 3;
        rd.fill = GridBagConstraints.NONE;
        roundsPanel.add(setGpRoundsErrLabel, rd);

        JPanel roundsGlue = new JPanel();
        roundsGlue.setOpaque(false);
        rd.gridx = 4;
        rd.weightx = 1;
        rd.fill = GridBagConstraints.HORIZONTAL;
        roundsPanel.add(roundsGlue, rd);

        setGpSubmitRoundsBtn.addActionListener(e -> {
            customFieldLock(false, "rounds");
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        setGpRoundsBox.addActionListener(e -> {
            boolean custom = "Custom".equals(setGpRoundsBox.getSelectedItem());
            setGpCustomRoundsField.setVisible(custom);
            setGpCustomRoundsField.setEnabled(custom);
            setGpSubmitRoundsBtn.setEnabled(custom);
            setGpRoundsErrLabel.setVisible(false);
            setGpRoundsLocked = false;
            setGpSubmitRoundsBtn.setText("✓");
            if (custom) {
                setGpCustomRoundsField.setText("");
                setGpCustomRoundsField.requestFocus();
            }
            roundsPanel.revalidate();
            roundsPanel.repaint();
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        return roundsPanel;
    }

    /**
     * Builds the settings section for AI options.
     * @return section panel
     */
    public static JPanel buildSetGpAi() {
        JPanel ai = new JPanel(new GridBagLayout());
        ai.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        JLabel diffL = new JLabel("Difficulty:");
        diffL.setFont(new Font("SansSerif", Font.PLAIN, 14));
        diffL.setForeground(activePalette.textMuted);
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        ai.add(diffL, g);
        setGpAiDifficultyBox = new JComboBox<>(new String[] { "Easy", "Medium", "Hard", "Impossible" });
        styleComboBox(setGpAiDifficultyBox);
        setGpAiDifficultyBox.setPreferredSize(new Dimension(272, 34));
        setGpAiDifficultyBox.setMaximumSize(new Dimension(320, 38));
        setGpAiDifficultyBox.setMinimumSize(new Dimension(128, 28));
        g.gridx = 1;
        g.weightx = 0;
        ai.add(setGpAiDifficultyBox, g);
        JPanel aiGlue0 = new JPanel();
        aiGlue0.setOpaque(false);
        g.gridx = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        ai.add(aiGlue0, g);

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        JLabel delayL = new JLabel("Think delay (AI):");
        delayL.setFont(new Font("SansSerif", Font.PLAIN, 14));
        delayL.setForeground(activePalette.textMuted);
        ai.add(delayL, g);
        setGpAiThinkDelayBox = new JComboBox<>(AI_THINK_DELAY_LABELS);
        styleComboBox(setGpAiThinkDelayBox);
        setGpAiThinkDelayBox.setPreferredSize(new Dimension(272, 34));
        setGpAiThinkDelayBox.setMaximumSize(new Dimension(320, 38));
        setGpAiThinkDelayBox.setMinimumSize(new Dimension(128, 28));
        g.gridx = 1;
        g.weightx = 0;
        ai.add(setGpAiThinkDelayBox, g);
        JPanel aiGlue1 = new JPanel();
        aiGlue1.setOpaque(false);
        g.gridx = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        ai.add(aiGlue1, g);
        ActionListener aiPersist = e -> {
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        };
        setGpAiDifficultyBox.addActionListener(aiPersist);
        setGpAiThinkDelayBox.addActionListener(aiPersist);
        return ai;
    }

    /**
     * Builds the settings help/info panel.
     * @return help section panel
     */
    public static JPanel buildSetGpHelp() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        setGpAllowHintCheckBox = new JCheckBox("Allow board word list (Hint button)");
        setGpAllowHintCheckBox.setOpaque(false);
        setGpAllowHintCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        setGpAllowHintCheckBox.setSelected(false);
        setGpAllowHintCheckBox.setToolTipText("Unchecked by default. Turn on to enable the Hint button during play.");
        setGpAllowHintCheckBox.addActionListener(e -> {
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        setGpAllowHintCheckBox.setForeground(activePalette.textMuted);
        p.add(setGpAllowHintCheckBox, BorderLayout.WEST);
        return p;
    }

    /**
     * Builds defaults actions panel (restore/save presets).
     * @return defaults section panel
     */
    public static JPanel buildSetGpDefaults() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);

        String desc = "Save current as default stores the options above. Restore my default loads that save, "
                + "or the starter rules (PvP, 15 second timer, 3 rounds, hints off) if you have never saved. "
                + "Return to factory defaults always reapplies those starter rules and ignores your saved profile.";
        JTextArea blurb = new JTextArea(desc);
        blurb.setEditable(false);
        blurb.setOpaque(false);
        blurb.setFocusable(false);
        blurb.setLineWrap(true);
        blurb.setWrapStyleWord(true);
        blurb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        blurb.setForeground(activePalette.textMuted);
        blurb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        blurb.setMargin(new Insets(0, 0, 0, 0));
        int approxCols = (520 - 72) / 8;
        if (approxCols < 24) {
            approxCols = 24;
        }
        blurb.setColumns(approxCols);
        col.add(blurb, gc);

        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        JPanel rowPrimary = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 10));
        rowPrimary.setOpaque(false);
        setGpRestoreDefaultButton = new JButton("Restore my default");
        setGpSaveDefaultButton = new JButton("Save current as default");
        styleFlatSettingsButton(setGpRestoreDefaultButton, activePalette.chipNeutral, 9, 16);
        setGpRestoreDefaultButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        styleFlatSettingsButton(setGpSaveDefaultButton, activePalette.homeConfirm, 9, 16);
        setGpSaveDefaultButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        setGpRestoreDefaultButton.addActionListener(e -> {
            Properties p = new Properties();
            p.putAll(BoggleSave.readPrefs(new File(BoggleSave.guestSaveGameDir(guestName), "save.txt")));
            boolean hadSaved = Boolean.parseBoolean(p.getProperty("customDefault.hasSaved", "false"));
            if (hadSaved) {
                userPrefs.readGameplayFrom(p, "customDefault.");
                userPrefs.allowBoardHint = Boolean.parseBoolean(
                        p.getProperty("customDefault.allowBoardHint", "false"));
                userPrefs.aiThinkDelayMs = Integer.parseInt(
                        p.getProperty("customDefault.aiThinkDelayMs", String.valueOf(userPrefs.aiThinkDelayMs)).trim());
                if (userPrefs.aiThinkDelayMs < 0) {
                    userPrefs.aiThinkDelayMs = 0;
                }
                if (userPrefs.aiThinkDelayMs > 10000) {
                    userPrefs.aiThinkDelayMs = 10000;
                }
                String customTheme = p.getProperty("customDefault.uiTheme");
                if (customTheme != null && !customTheme.isBlank()) {
                    userPrefs.uiTheme = customTheme.trim();
                }
                int mv = Integer.parseInt(p.getProperty("customDefault.musicVolumePct",
                        String.valueOf(userPrefs.musicVolumePct)).trim());
                int sv = Integer.parseInt(p.getProperty("customDefault.sfxVolumePct",
                        String.valueOf(userPrefs.sfxVolumePct)).trim());
                userPrefs.musicVolumePct = mv;
                if (userPrefs.musicVolumePct < 0) {
                    userPrefs.musicVolumePct = 0;
                }
                if (userPrefs.musicVolumePct > 100) {
                    userPrefs.musicVolumePct = 100;
                }
                userPrefs.sfxVolumePct = sv;
                if (userPrefs.sfxVolumePct < 0) {
                    userPrefs.sfxVolumePct = 0;
                }
                if (userPrefs.sfxVolumePct > 100) {
                    userPrefs.sfxVolumePct = 100;
                }
                userPrefs.sfxEnabled = Boolean.parseBoolean(p.getProperty("customDefault.sfxEnabled", "true"));
                userPrefs.applyToHomeUi();
                userPrefs.applyToSettingsUi();
            } else {
                resetToFactoryGameplay();
            }
            refreshSetUi();
            if (wordsHintButton != null) {
                wordsHintButton.setEnabled(userPrefs.allowBoardHint);
            }
            BoggleSave.persistPrefs();
            refreshTheme();
            BoggleAudio.refreshVolumes(userPrefs);
            String msg;
            if (hadSaved) {
                msg = "Restored your saved default settings.";
            } else {
                msg = "No saved default yet — applied built-in factory settings.";
            }
            JOptionPane.showMessageDialog(frame, msg, "Defaults", JOptionPane.INFORMATION_MESSAGE);
        });
        setGpSaveDefaultButton.addActionListener(e -> {
            Properties p = new Properties();
            p.putAll(BoggleSave.readPrefs(new File(BoggleSave.guestSaveGameDir(guestName), "save.txt")));
            p.setProperty("customDefault.hasSaved", "true");
            userPrefs.readFromSettingsUi();
            userPrefs.writeGameplayTo(p, "customDefault.");
            p.setProperty("customDefault.allowBoardHint",
                    String.valueOf(setGpAllowHintCheckBox != null && setGpAllowHintCheckBox.isSelected()));
            if (setGpAiThinkDelayBox != null) {
                int delayIdx = setGpAiThinkDelayBox.getSelectedIndex();
                int delayMs = 1200;
                if (delayIdx >= 0 && delayIdx < AI_THINK_DELAY_VALS.length) {
                    delayMs = AI_THINK_DELAY_VALS[delayIdx];
                }
                p.setProperty("customDefault.aiThinkDelayMs", String.valueOf(delayMs));
            }
            String themeSaved;
            if (settingsThemeCombo == null) {
                themeSaved = userPrefs.uiTheme;
            } else {
                Object sel = settingsThemeCombo.getSelectedItem();
                String lab = "";
                if (sel != null) {
                    lab = String.valueOf(sel);
                }
                if ("Healing (Soft)".equals(lab)) {
                    themeSaved = "HEALING_SOFT";
                } else if ("Dark mode".equals(lab)) {
                    themeSaved = "DARK";
                } else {
                    themeSaved = "OCEAN_DEFAULT";
                }
            }
            p.setProperty("customDefault.uiTheme", themeSaved);
            if (settingsMusicSlider != null) {
                p.setProperty("customDefault.musicVolumePct", String.valueOf(settingsMusicSlider.getValue()));
            }
            if (settingsSfxSlider != null) {
                p.setProperty("customDefault.sfxVolumePct", String.valueOf(settingsSfxSlider.getValue()));
            }
            if (settingsSfxOnCheck != null) {
                p.setProperty("customDefault.sfxEnabled", String.valueOf(settingsSfxOnCheck.isSelected()));
            }
            BoggleSave.save(p, true);
            JOptionPane.showMessageDialog(frame,
                    "Current Gameplay settings are saved as your default.\n"
                            + "Use \"Restore my default\" anytime to return to this profile.",
                    "Save as default", JOptionPane.INFORMATION_MESSAGE);
        });
        rowPrimary.add(setGpRestoreDefaultButton);
        rowPrimary.add(setGpSaveDefaultButton);
        col.add(rowPrimary, gc);

        gc.gridy = 2;
        gc.insets = new Insets(4, 0, 0, 0);
        JPanel rowFactory = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        rowFactory.setOpaque(false);
        setGpBuiltInFactoryButton = new JButton("Return to factory defaults");
        styleFlatSettingsButton(setGpBuiltInFactoryButton, new Color(132, 100, 78), 9, 16);
        setGpBuiltInFactoryButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        setGpBuiltInFactoryButton.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(frame,
                    "Reset everything to factory defaults.\n"
                            + "(Hints off, PvP, 15s timer, 3 rounds, target 50, min 3 letters.)",
                    "Return to factory defaults", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            resetToFactoryGameplay();
            refreshSetUi();
            if (wordsHintButton != null) {
                wordsHintButton.setEnabled(userPrefs.allowBoardHint);
            }
            BoggleSave.persistPrefs();
        });
        rowFactory.add(setGpBuiltInFactoryButton);
        col.add(rowFactory, gc);
        return col;
    }

    /**
     * Builds the theme selection tab content.
     * @return theme tab panel
     */
    public static JPanel buildSettingsThemeTab() {
        settingsThemeTab = new JPanel(new BorderLayout());
        settingsThemeTab.setOpaque(true);
        settingsThemeTab.setBackground(activePalette.scrollTrackBg);

        settingsThemeCard = new JPanel(new BorderLayout());
        settingsThemeCard.setOpaque(true);
        settingsThemeCard.setBackground(activePalette.panelBg);
        settingsThemeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.line, 1),
                BorderFactory.createEmptyBorder(28, 24, 28, 24)));
        JLabel cap = new JLabel("Appearance");
        cap.setFont(new Font("SansSerif", Font.BOLD, 13));
        cap.setForeground(activePalette.textStrong);
        settingsThemeCard.add(cap, BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row.setOpaque(false);
        JLabel lab = new JLabel("UI theme:");
        lab.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lab.setForeground(activePalette.textMuted);
        row.add(lab);
        settingsThemeCombo = new JComboBox<>(new String[] {"Default (Ocean)", "Healing (Soft)", "Dark mode"});
        styleComboBox(settingsThemeCombo);
        settingsThemeCombo.setPreferredSize(new Dimension(272, 34));
        settingsThemeCombo.setMaximumSize(new Dimension(320, 38));
        settingsThemeCombo.setMinimumSize(new Dimension(128, 28));
        if ("HEALING_SOFT".equals(userPrefs.uiTheme)) {
            settingsThemeCombo.setSelectedItem("Healing (Soft)");
        } else if ("DARK".equals(userPrefs.uiTheme)) {
            settingsThemeCombo.setSelectedItem("Dark mode");
        } else {
            settingsThemeCombo.setSelectedItem("Default (Ocean)");
        }
        settingsThemeCombo.addActionListener(e -> {
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });
        row.add(settingsThemeCombo);
        settingsThemeCard.add(row, BorderLayout.CENTER);

        settingsThemeTab.add(settingsThemeCard, BorderLayout.NORTH);
        return settingsThemeTab;
    }

    /**
     * Builds the sound settings tab content.
     * @return sound tab panel
     */
    public static JPanel buildSettingsSoundTab() {
        settingsSoundTab = new JPanel(new BorderLayout());
        settingsSoundTab.setOpaque(true);
        settingsSoundTab.setBackground(activePalette.scrollTrackBg);

        settingsSoundCard = new JPanel();
        settingsSoundCard.setLayout(new BoxLayout(settingsSoundCard, BoxLayout.PAGE_AXIS));
        settingsSoundCard.setOpaque(true);
        settingsSoundCard.setBackground(activePalette.panelBg);
        settingsSoundCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.line, 1),
                BorderFactory.createEmptyBorder(24, 24, 28, 24)));
        JLabel cap = new JLabel("Audio");
        cap.setFont(new Font("SansSerif", Font.BOLD, 13));
        cap.setForeground(activePalette.textStrong);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsSoundCard.add(cap);
        settingsSoundCard.add(Box.createVerticalStrut(16));

        JLabel musicLab = new JLabel("Music (background + match-end fanfare)");
        musicLab.setFont(new Font("SansSerif", Font.PLAIN, 14));
        musicLab.setForeground(activePalette.textMuted);
        musicLab.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsSoundCard.add(musicLab);
        settingsSoundCard.add(Box.createVerticalStrut(6));
        settingsMusicSlider = new JSlider(0, 100, userPrefs.musicVolumePct);
        settingsMusicSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsMusicSlider.setMajorTickSpacing(50);
        settingsMusicSlider.setPaintTicks(true);
        settingsSoundCard.add(settingsMusicSlider);
        settingsSoundCard.add(Box.createVerticalStrut(18));

        JLabel sfxLab = new JLabel("Sound effects (reject + word score notes)");
        sfxLab.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sfxLab.setForeground(activePalette.textMuted);
        sfxLab.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsSoundCard.add(sfxLab);
        settingsSoundCard.add(Box.createVerticalStrut(6));
        settingsSfxOnCheck = new JCheckBox("Sound effects on");
        settingsSfxOnCheck.setOpaque(false);
        settingsSfxOnCheck.setFont(new Font("SansSerif", Font.PLAIN, 14));
        settingsSfxOnCheck.setForeground(activePalette.textMuted);
        settingsSfxOnCheck.setSelected(userPrefs.sfxEnabled);
        settingsSfxOnCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsSoundCard.add(settingsSfxOnCheck);
        settingsSoundCard.add(Box.createVerticalStrut(8));
        settingsSfxSlider = new JSlider(0, 100, userPrefs.sfxVolumePct);
        settingsSfxSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsSfxSlider.setMajorTickSpacing(50);
        settingsSfxSlider.setPaintTicks(true);
        settingsSfxSlider.setEnabled(userPrefs.sfxEnabled);
        settingsSoundCard.add(settingsSfxSlider);

        settingsSfxOnCheck.addItemListener(e -> {
            userPrefs.sfxEnabled = settingsSfxOnCheck.isSelected();
            if (settingsSfxSlider != null) {
                settingsSfxSlider.setEnabled(userPrefs.sfxEnabled);
            }
            BoggleAudio.refreshVolumes(userPrefs);
            if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
        });

        ChangeListener liveVol = e -> {
            userPrefs.musicVolumePct = settingsMusicSlider.getValue();
            userPrefs.sfxVolumePct = settingsSfxSlider.getValue();
            BoggleAudio.refreshVolumes(userPrefs);
            if (!settingsMusicSlider.getValueIsAdjusting() && !settingsSfxSlider.getValueIsAdjusting()) {
                if (!setBulk && userPrefs.uiNest <= 0) {
                applySettings();
            }
            }
        };
        settingsMusicSlider.addChangeListener(liveVol);
        settingsSfxSlider.addChangeListener(liveVol);

        settingsSoundTab.add(settingsSoundCard, BorderLayout.NORTH);
        return settingsSoundTab;
    }

    /**
     * Refreshes settings UI state after preference changes.
     */
    public static void refreshSetUi() {
        // setBulk stops combo listeners from writing prefs while we are just loading values into widgets.
        setBulk = true;
        userPrefs.applyToSettingsUi();
            if (setGpAiThinkDelayBox != null) {
                int pick = 2;
                for (int i = 0; i < AI_THINK_DELAY_VALS.length; i++) {
                    if (AI_THINK_DELAY_VALS[i] == userPrefs.aiThinkDelayMs) {
                        pick = i;
                        break;
                    }
                }
                setGpAiThinkDelayBox.setSelectedIndex(pick);
            }
            if (setGpAllowHintCheckBox != null) {
                setGpAllowHintCheckBox.setSelected(userPrefs.allowBoardHint);
            }
            if (settingsThemeCombo != null) {
                if ("HEALING_SOFT".equals(userPrefs.uiTheme)) {
                    settingsThemeCombo.setSelectedItem("Healing (Soft)");
                } else if ("DARK".equals(userPrefs.uiTheme)) {
                    settingsThemeCombo.setSelectedItem("Dark mode");
                } else {
                    settingsThemeCombo.setSelectedItem("Default (Ocean)");
                }
            }
            if (settingsMusicSlider != null) {
                settingsMusicSlider.setValue(userPrefs.musicVolumePct);
            }
            if (settingsSfxOnCheck != null) {
                settingsSfxOnCheck.setSelected(userPrefs.sfxEnabled);
            }
            if (settingsSfxSlider != null) {
                settingsSfxSlider.setValue(userPrefs.sfxVolumePct);
                boolean sfxOn;
                if (settingsSfxOnCheck != null) {
                    sfxOn = userPrefs.sfxEnabled;
                } else {
                    sfxOn = true;
                }
                settingsSfxSlider.setEnabled(sfxOn);
            }
        setBulk = false;
    }


    /**
     * Synchronizes settings widgets with current preference values.
     */
    public static void syncSetFields() {
        // Show/hide custom number boxes and flip ✓ (lock) vs ✎ (edit) on each gameplay row.
        setGpCustomTimeField.setVisible("Custom".equals(setGpTimeBox.getSelectedItem()));
        setGpCustomTimeField.setEnabled("Custom".equals(setGpTimeBox.getSelectedItem()) && !setGpTimeLocked);
        setGpSubmitTimeBtn.setEnabled("Custom".equals(setGpTimeBox.getSelectedItem()));
        setGpTimeErrLabel.setVisible(false);
        if (setGpTimeLocked) {
            setGpSubmitTimeBtn.setText("✎");
        } else {
            setGpSubmitTimeBtn.setText("✓");
        }
        if (setGpTimeLocked && "Custom".equals(setGpTimeBox.getSelectedItem())) {
            setGpCustomTimeField.setEnabled(false);
            setGpCustomTimeField.setForeground(Color.DARK_GRAY);
        } else {
            setGpCustomTimeField.setForeground(Color.BLACK);
        }

        setGpCustomMinWordField.setVisible("Custom".equals(setGpMinWordBox.getSelectedItem()));
        setGpCustomMinWordField.setEnabled("Custom".equals(setGpMinWordBox.getSelectedItem()) && !setGpMinWordLocked);
        setGpSubmitMinWordBtn.setEnabled("Custom".equals(setGpMinWordBox.getSelectedItem()));
        setGpMinWordErrLabel.setVisible(false);
        if (setGpMinWordLocked) {
            setGpSubmitMinWordBtn.setText("✎");
        } else {
            setGpSubmitMinWordBtn.setText("✓");
        }
        if (setGpMinWordLocked && "Custom".equals(setGpMinWordBox.getSelectedItem())) {
            setGpCustomMinWordField.setEnabled(false);
            setGpCustomMinWordField.setForeground(Color.DARK_GRAY);
        } else {
            setGpCustomMinWordField.setForeground(Color.BLACK);
        }

        setGpCustomTargetField.setVisible("Custom".equals(setGpTargetBox.getSelectedItem()));
        setGpCustomTargetField.setEnabled("Custom".equals(setGpTargetBox.getSelectedItem()) && !setGpTargetLocked);
        setGpSubmitTargetBtn.setEnabled("Custom".equals(setGpTargetBox.getSelectedItem()));
        setGpTargetErrLabel.setVisible(false);
        if (setGpTargetLocked) {
            setGpSubmitTargetBtn.setText("✎");
        } else {
            setGpSubmitTargetBtn.setText("✓");
        }
        if (setGpTargetLocked && "Custom".equals(setGpTargetBox.getSelectedItem())) {
            setGpCustomTargetField.setEnabled(false);
            setGpCustomTargetField.setForeground(Color.DARK_GRAY);
        } else {
            setGpCustomTargetField.setForeground(Color.BLACK);
        }
        setGpCustomRoundsField.setVisible("Custom".equals(setGpRoundsBox.getSelectedItem()));
        setGpCustomRoundsField.setEnabled("Custom".equals(setGpRoundsBox.getSelectedItem()) && !setGpRoundsLocked);
        setGpSubmitRoundsBtn.setEnabled("Custom".equals(setGpRoundsBox.getSelectedItem()));
        setGpRoundsErrLabel.setVisible(false);
        if (setGpRoundsLocked) {
            setGpSubmitRoundsBtn.setText("✎");
        } else {
            setGpSubmitRoundsBtn.setText("✓");
        }
        if (setGpRoundsLocked && "Custom".equals(setGpRoundsBox.getSelectedItem())) {
            setGpCustomRoundsField.setEnabled(false);
            setGpCustomRoundsField.setForeground(Color.DARK_GRAY);
        } else {
            setGpCustomRoundsField.setForeground(Color.BLACK);
        }

        boolean ai = "Player vs AI".equals(setGpModeBox.getSelectedItem())
                || "Multiplayers + AI".equals(setGpModeBox.getSelectedItem());
        setGpAiDifficultyBox.setEnabled(ai);
    }

    /**
     * Refreshes visibility/enabled state of home quick-setting rows.
     */
    public static void refreshHomeRows() {
        boolean timeCustom = "Custom".equals(homeTimeBox.getSelectedItem());
        homeCustomTimeField.setVisible(timeCustom);
        homeCustomTimeField.setEnabled(timeCustom && !homeTimeLocked);
        homeSubmitTimeBtn.setEnabled(timeCustom);
        homeTimeErrorLabel.setVisible(false);
        if (homeTimeLocked) {
            homeSubmitTimeBtn.setText("✎");
        } else {
            homeSubmitTimeBtn.setText("✓");
        }
        if (homeTimeLocked && timeCustom) {
            homeCustomTimeField.setEnabled(false);
            homeCustomTimeField.setForeground(activePalette.textQuiet);
        } else {
            homeCustomTimeField.setForeground(activePalette.textMuted);
        }

        boolean mwCustom = "Custom".equals(homeMinWordBox.getSelectedItem());
        homeCustomMinWordField.setVisible(mwCustom);
        homeCustomMinWordField.setEnabled(mwCustom && !homeMinWordLocked);
        homeSubmitMinWordBtn.setEnabled(mwCustom);
        homeMinWordErrorLabel.setVisible(false);
        if (homeMinWordLocked) {
            homeSubmitMinWordBtn.setText("✎");
        } else {
            homeSubmitMinWordBtn.setText("✓");
        }
        if (homeMinWordLocked && mwCustom) {
            homeCustomMinWordField.setEnabled(false);
            homeCustomMinWordField.setForeground(activePalette.textQuiet);
        } else {
            homeCustomMinWordField.setForeground(activePalette.textMuted);
        }

        boolean tgtCustom = "Custom".equals(homeTargetBox.getSelectedItem());
        homeCustomTargetField.setVisible(tgtCustom);
        homeCustomTargetField.setEnabled(tgtCustom && !homeTargetLocked);
        homeSubmitTargetBtn.setEnabled(tgtCustom);
        homeTargetErrorLabel.setVisible(false);
        if (homeTargetLocked) {
            homeSubmitTargetBtn.setText("✎");
        } else {
            homeSubmitTargetBtn.setText("✓");
        }
        if (homeTargetLocked && tgtCustom) {
            homeCustomTargetField.setEnabled(false);
            homeCustomTargetField.setForeground(activePalette.textQuiet);
        } else {
            homeCustomTargetField.setForeground(activePalette.textMuted);
        }

        boolean rdCustom = "Custom".equals(homeRoundsBox.getSelectedItem());
        homeCustomRoundsField.setVisible(rdCustom);
        homeCustomRoundsField.setEnabled(rdCustom && !homeRoundsLocked);
        homeSubmitRoundsBtn.setEnabled(rdCustom);
        homeRoundsErrorLabel.setVisible(false);
        if (homeRoundsLocked) {
            homeSubmitRoundsBtn.setText("✎");
        } else {
            homeSubmitRoundsBtn.setText("✓");
        }
        if (homeRoundsLocked && rdCustom) {
            homeCustomRoundsField.setEnabled(false);
            homeCustomRoundsField.setForeground(activePalette.textQuiet);
        } else {
            homeCustomRoundsField.setForeground(activePalette.textMuted);
        }
    }

    /**
     * Restores gameplay options to built-in defaults.
     */
    public static void resetToFactoryGameplay() {
        userPrefs.aiThinkDelayMs = 1200;
        userPrefs.allowBoardHint = false;
        userPrefs.uiTheme = "OCEAN_DEFAULT";
        userPrefs.musicVolumePct = 80;
        userPrefs.sfxVolumePct = 80;
        userPrefs.sfxEnabled = true;
        userPrefs.gameMode = "Player vs Player";
        userPrefs.timerChoice = "15 sec";
        userPrefs.customTimer = "";
        userPrefs.timerLocked = false;
        userPrefs.aiDifficulty = "Easy";
        userPrefs.minWordChoice = "3 letters";
        userPrefs.customMinWord = "";
        userPrefs.minWordLocked = false;
        userPrefs.targetChoice = "Target 50";
        userPrefs.customTarget = "";
        userPrefs.targetLocked = false;
        userPrefs.roundsChoice = "3 rounds";
        userPrefs.customRounds = "";
        userPrefs.roundsLocked = false;
        userPrefs.applyRuntimeRules();

        setBulk = true;
        userPrefs.applyToHomeUi();
        userPrefs.applyToSettingsUi();
        if (setGpAiThinkDelayBox != null) {
            setGpAiThinkDelayBox.setSelectedIndex(2);
        }
        if (setGpAllowHintCheckBox != null) {
            setGpAllowHintCheckBox.setSelected(userPrefs.allowBoardHint);
        }
        setBulk = false;

        if (settingsThemeCombo != null) {
            if ("HEALING_SOFT".equals(userPrefs.uiTheme)) {
                settingsThemeCombo.setSelectedItem("Healing (Soft)");
            } else if ("DARK".equals(userPrefs.uiTheme)) {
                settingsThemeCombo.setSelectedItem("Dark mode");
            } else {
                settingsThemeCombo.setSelectedItem("Default (Ocean)");
            }
        }
        if (settingsMusicSlider != null) {
            settingsMusicSlider.setValue(userPrefs.musicVolumePct);
        }
        if (settingsSfxOnCheck != null) {
            settingsSfxOnCheck.setSelected(userPrefs.sfxEnabled);
        }
        if (settingsSfxSlider != null) {
            settingsSfxSlider.setValue(userPrefs.sfxVolumePct);
            settingsSfxSlider.setEnabled(userPrefs.sfxEnabled);
        }
        BoggleAudio.refreshVolumes(userPrefs);
        refreshTheme();
    }

    /**
     * Applies settings values to runtime state and persists preferences.
     */
    public static void applySettings() {
        if (setGpTimeBox != null && setGpMinWordBox != null && setGpTargetBox != null && setGpRoundsBox != null) {
            try {
                // Custom rows must be locked (✓ confirmed) with valid numbers before saving.
                if ("Custom".equals(String.valueOf(setGpTimeBox.getSelectedItem()))) {
                    if (!setGpTimeLocked || setGpCustomTimeField == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int value = Integer.parseInt(setGpCustomTimeField.getText().trim());
                    if (value < 1 || value > 3600) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if ("Custom".equals(String.valueOf(setGpMinWordBox.getSelectedItem()))) {
                    if (!setGpMinWordLocked || setGpCustomMinWordField == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int value = Integer.parseInt(setGpCustomMinWordField.getText().trim());
                    if (value < 3 || value > 10) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if ("Custom".equals(String.valueOf(setGpTargetBox.getSelectedItem()))) {
                    if (!setGpTargetLocked || setGpCustomTargetField == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int value = Integer.parseInt(setGpCustomTargetField.getText().trim());
                    if (value < 1 || value > 999) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if ("Custom".equals(String.valueOf(setGpRoundsBox.getSelectedItem()))) {
                    if (!setGpRoundsLocked || setGpCustomRoundsField == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int value = Integer.parseInt(setGpCustomRoundsField.getText().trim());
                    if (value < 1 || value > 20) {
                        JOptionPane.showMessageDialog(frame,
                                "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                                "Invalid or incomplete custom rules",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (NumberFormatException ignored) {
                JOptionPane.showMessageDialog(frame,
                        "Each \"Custom\" row in Gameplay must be filled and confirmed with ✓ before these settings can be saved.",
                        "Invalid or incomplete custom rules",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        userPrefs.allowBoardHint = setGpAllowHintCheckBox != null && setGpAllowHintCheckBox.isSelected();
        if (setGpAiThinkDelayBox != null) {
            int delayIdx = setGpAiThinkDelayBox.getSelectedIndex();
            if (delayIdx >= 0 && delayIdx < AI_THINK_DELAY_VALS.length) {
                userPrefs.aiThinkDelayMs = AI_THINK_DELAY_VALS[delayIdx];
            } else {
                userPrefs.aiThinkDelayMs = 1200;
            }
        }
        if (settingsThemeCombo != null) {
            Object sel = settingsThemeCombo.getSelectedItem();
            String lab;
            if (sel == null) {
                lab = "";
            } else {
                lab = String.valueOf(sel);
            }
            if ("Healing (Soft)".equals(lab)) {
                userPrefs.uiTheme = "HEALING_SOFT";
            } else if ("Dark mode".equals(lab)) {
                userPrefs.uiTheme = "DARK";
            } else {
                userPrefs.uiTheme = "OCEAN_DEFAULT";
            }
        }
        if (settingsMusicSlider != null) {
            userPrefs.musicVolumePct = settingsMusicSlider.getValue();
        }
        if (settingsSfxSlider != null) {
            userPrefs.sfxVolumePct = settingsSfxSlider.getValue();
        }
        if (settingsSfxOnCheck != null) {
            userPrefs.sfxEnabled = settingsSfxOnCheck.isSelected();
        }

        userPrefs.readFromSettingsUi();
        userPrefs.applyToHomeUi();
        BoggleSave.persistPrefs();
        refreshSetUi();
        if (wordsHintButton != null) {
            wordsHintButton.setEnabled(userPrefs.allowBoardHint);
        }
        BoggleAudio.refreshVolumes(userPrefs);
        refreshTheme();
    }

    /**
     * Builds the pre-game lobby panel.
     * @return lobby panel
     */
    public static JPanel buildLobbyPanel() {
        JPanel root = new JPanel(new BorderLayout());
        lobbyRoot = root;
        root.setBackground(activePalette.appBg);

        JPanel topPanel = new JPanel();
        lobbyTop = topPanel;
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        topPanel.setBackground(activePalette.appBg);

        JLabel title = new JLabel("Boggle Lobby", JLabel.CENTER);
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Configure players before starting", JLabel.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(activePalette.textMuted);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(title);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(subtitle);

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 18);
        Dimension fieldSize = new Dimension(380, 40);

        JPanel playersPanel = new JPanel(new GridBagLayout());
        lobbyPlayersCard = playersPanel;
        playersPanel.setOpaque(false);
        playersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.line, 2),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel playersLabel = new JLabel("Players:");
        playersLabel.setFont(labelFont);
        playersLabel.setForeground(activePalette.textMuted);
        playersPanel.add(playersLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;

        JPanel countPanel = new JPanel(new GridBagLayout());
        countPanel.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(0, 2, 0, 2);
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.gridy = 0;

        lobbyPlayerCountBox = new JComboBox<>(new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "Custom" });
        lobbyPlayerCountBox.setSelectedItem("3");
        styleComboBox(lobbyPlayerCountBox);
        lobbyPlayerCountBox.setFont(fieldFont);
        lobbyPlayerCountBox.setPreferredSize(fieldSize);
        cc.gridx = 0;
        cc.weightx = 0.6;
        countPanel.add(lobbyPlayerCountBox, cc);

        lobbyCustomPlayerCountField = new JTextField();
        lobbyCustomPlayerCountField.setColumns(4);
        lobbyCustomPlayerCountField.setFont(fieldFont);
        lobbyCustomPlayerCountField.setPreferredSize(new Dimension(90, 40));
        lobbyCustomPlayerCountField.setEnabled(false);
        lobbyCustomPlayerCountField.setVisible(false);
        lobbyCustomPlayerCountField.setForeground(activePalette.textMuted);
        lobbyCustomPlayerCountField.setBackground(activePalette.inputBg);
        lobbyCustomPlayerCountField.setCaretColor(activePalette.textMuted);
        cc.gridx = 1;
        cc.weightx = 0.4;
        countPanel.add(lobbyCustomPlayerCountField, cc);

        lobbySubmitPlayerCountBtn = new JButton("✓");
        lobbySubmitPlayerCountBtn.setMargin(new Insets(0, 4, 0, 4));
        lobbySubmitPlayerCountBtn.setFocusable(false);
        lobbySubmitPlayerCountBtn.setEnabled(false);
        lobbySubmitPlayerCountBtn.setPreferredSize(new Dimension(48, 40));
        lobbySubmitPlayerCountBtn.setFont(labelFont);
        cc.gridx = 2;
        cc.weightx = 0;
        countPanel.add(lobbySubmitPlayerCountBtn, cc);

        lobbyPlayerCountErrorLabel = new JLabel("* enter " + 2 + "–" + 9);
        lobbyPlayerCountErrorLabel.setForeground(Color.RED);
        lobbyPlayerCountErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lobbyPlayerCountErrorLabel.setVisible(false);
        cc.gridx = 3;
        countPanel.add(lobbyPlayerCountErrorLabel, cc);

        playersPanel.add(countPanel, gbc);

        lobbyTurnOrderBox = new JComboBox<>(new String[] {
                "As entered",
                "Randomize order"
        });
        lobbyTurnOrderBox.setSelectedItem("As entered");
        styleComboBox(lobbyTurnOrderBox);
        lobbyTurnOrderBox.setFont(fieldFont);
        lobbyTurnOrderBox.setPreferredSize(fieldSize);

        lobbyNamesPanel = new JPanel(new GridBagLayout());
        lobbyNamesPanel.setOpaque(false);

        JScrollPane namesScroll = new JScrollPane(lobbyNamesPanel);
        namesScroll.setOpaque(false);
        namesScroll.getViewport().setOpaque(false);
        namesScroll.setBorder(BorderFactory.createEmptyBorder());
        namesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        namesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        namesScroll.getVerticalScrollBar().setUnitIncrement(24);
        namesScroll.setPreferredSize(new Dimension(720, 320));
        namesScroll.setMinimumSize(new Dimension(480, 160));

        gbc.gridy = 1;
        gbc.gridx = 0;
        
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        playersPanel.add(namesScroll, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel turnOrderLabel = new JLabel("Turn order:");
        turnOrderLabel.setFont(labelFont);
        turnOrderLabel.setForeground(activePalette.textMuted);
        playersPanel.add(turnOrderLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        playersPanel.add(lobbyTurnOrderBox, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel firstTurnLabel = new JLabel("First turn:");
        firstTurnLabel.setFont(labelFont);
        firstTurnLabel.setForeground(activePalette.textMuted);
        playersPanel.add(firstTurnLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        lobbyFirstTurnBox = new JComboBox<>(new String[] { "Player 1", "Player 2", "Random" });
        lobbyFirstTurnBox.setSelectedItem("Random");
        styleComboBox(lobbyFirstTurnBox);
        lobbyFirstTurnBox.setFont(fieldFont);
        lobbyFirstTurnBox.setPreferredSize(fieldSize);
        playersPanel.add(lobbyFirstTurnBox, gbc);

        lobbySubmitPlayerCountBtn.addActionListener(e -> customFieldLock(false, "playerCount"));

        rebuildLobbyNameFields(3);

        lobbyPlayerCountBox.addActionListener(e -> {
            boolean custom = "Custom".equals(lobbyPlayerCountBox.getSelectedItem());
            lobbyCustomPlayerCountField.setVisible(custom);
            lobbyCustomPlayerCountField.setEnabled(custom);
            boolean lockCount = "Player vs Player".equals(selectedHomeMode)
                    || "Player vs AI".equals(selectedHomeMode)
                    || ("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode));
            lobbySubmitPlayerCountBtn.setEnabled(custom && !lockCount);
            lobbyPlayerCountErrorLabel.setVisible(false);
            lobbyPlayerCountLocked = false;
            lobbySubmitPlayerCountBtn.setText("✓");
            if (custom) {
                lobbyCustomPlayerCountField.setText("");
                SwingUtilities.invokeLater(() -> lobbyCustomPlayerCountField.requestFocusInWindow());
            } else {
                int count = Integer.parseInt(String.valueOf(lobbyPlayerCountBox.getSelectedItem()).trim());
                rebuildLobbyNameFields(count);
            }
            playersPanel.revalidate();
            playersPanel.repaint();
        });

        JButton startMatchBtn = new JButton("Start Game");
        lobbyStartBtn = startMatchBtn;
        
        stylePrimaryButton(startMatchBtn);
        startMatchBtn.setFont(new Font("SansSerif", Font.BOLD, 28));
        startMatchBtn.setPreferredSize(new Dimension(420, 76));
        startMatchBtn.setMaximumSize(new Dimension(420, 76));
        startMatchBtn.addActionListener(e -> onLobbyStartClicked());

        JButton backToMenuBtn = new JButton("Back to Menu");
        lobbyBackBtn = backToMenuBtn;
        styleSecondaryButton(backToMenuBtn);
        backToMenuBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        backToMenuBtn.setPreferredSize(new Dimension(260, 56));
        backToMenuBtn.setMaximumSize(new Dimension(260, 56));
        backToMenuBtn.addActionListener(e -> {
            frame.dispose();
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(new Dimension(1000, 740));
            frame.setLocationRelativeTo(null);
        frame.setVisible(true);
            cards.show(deck, "home");
            refreshHomeResumeButton();
BoggleAudio.startHomeMusic(userPrefs);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        bottomPanel.add(backToMenuBtn);
        bottomPanel.add(Box.createRigidArea(new Dimension(16, 0)));
        bottomPanel.add(startMatchBtn);

        root.add(topPanel, BorderLayout.NORTH);
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(24, 24, 24, 24);
        centerWrap.add(playersPanel, c);
        root.add(centerWrap, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
        return root;
    }

    /**
     * Rebuilds lobby player-name inputs for a chosen player count.
     * @param count target player count
     */
    public static void rebuildLobbyNameFields(int count) {
        // Tear down name rows and rebuild — player count or mode changed.
        lobbyNameFields.clear();
        lobbyNamesPanel.removeAll();
        int aiSlot = -1;
        if ("Player vs AI".equals(selectedHomeMode)) {
            aiSlot = 1;
        } else if ("Multiplayers + AI".equals(selectedHomeMode) && count >= 3) {
            aiSlot = count - 1;
        }
        boolean contest = ("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode));
        GridBagConstraints nbc = new GridBagConstraints();
        nbc.insets = new Insets(12, 12, 12, 12);
        nbc.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < count; i++) {
            nbc.gridy = i;
            nbc.gridx = 0;
            nbc.weightx = 0;
            boolean rowAi = contest ? (i == 0) : (i == aiSlot);
            String labelText;
            if (rowAi) {
                labelText = "Player " + (i + 1) + " (AI) name:";
            } else {
                labelText = "Player " + (i + 1) + " Name:";
            }
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("SansSerif", Font.BOLD, 18));
            label.setForeground(activePalette.textMuted);
            lobbyNamesPanel.add(label, nbc);
            nbc.gridx = 1;
            nbc.weightx = 1.0;
            JTextField tf = new JTextField();
            tf.setColumns(24);
            tf.setFont(new Font("SansSerif", Font.PLAIN, 18));
            tf.setMinimumSize(new Dimension(280, 40));
            tf.setPreferredSize(new Dimension(420, 40));
            tf.setForeground(activePalette.textStrong);
            tf.setBackground(activePalette.inputBg);
            tf.setCaretColor(activePalette.textStrong);
            if (contest) {
                if (i == 0) {
                    tf.setText(guestName == null || guestName.isBlank() ? "Your AI" : guestName.trim() + " AI");
                    tf.setEnabled(false);
                } else {
                    tf.setText("Opponent");
                    tf.setEnabled(true);
                }
            } else if (rowAi) {
                tf.setText("Computer");  // default AI name; player can rename
            }
            lobbyNameFields.add(tf);
            lobbyNamesPanel.add(tf, nbc);
        }
        lobbyNamesPanel.revalidate();
        lobbyNamesPanel.repaint();

        if (lobbyFirstTurnBox != null) {
            String prev = (String) lobbyFirstTurnBox.getSelectedItem();
            lobbyFirstTurnBox.removeAllItems();
            for (int i = 0; i < count; i++) {
                lobbyFirstTurnBox.addItem("Player " + (i + 1));
            }
            lobbyFirstTurnBox.addItem("Random");
            if (prev != null) {
                lobbyFirstTurnBox.setSelectedItem(prev);
            }
            if (lobbyFirstTurnBox.getSelectedItem() == null) {
                lobbyFirstTurnBox.setSelectedItem("Random");
            }
        }
    }

    /**
     * Handles Start action from the lobby screen.
     */
    public static void onLobbyStartClicked() {
        // Build the player list from lobby name fields, then validate counts for the chosen mode.
        if (("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode))) {
            applyContestModePreset();
            activeAiDifficulty = "Impossible";
        } else if (homeAIDifficultyBox == null) {
            activeAiDifficulty = "Easy";
        } else {
            activeAiDifficulty = String.valueOf(homeAIDifficultyBox.getSelectedItem());
        }
        clearAllSeats();
        boolean contest = ("AI vs AI Contest".equals(selectedHomeMode) || "AI vs AI Contest".equals(userPrefs.gameMode));
        if (contest) {
            String you = guestName == null || guestName.isBlank() ? "Your AI" : guestName.trim() + " AI";
            String oppName = "Opponent";
            if (!lobbyNameFields.isEmpty() && lobbyNameFields.get(1) != null) {
                String typed = lobbyNameFields.get(1).getText().trim();
                if (!typed.isEmpty()) {
                    oppName = typed;
                }
            }
            appendSeat(you, true);
            appendSeat(oppName, false);
        } else {
            int nSlots = lobbyNameFields.size();
            int aiSlot = -1;
            if ("Player vs AI".equals(selectedHomeMode)) {
                aiSlot = 1;  // seat 2 is always the computer in 1v1 AI
            } else if ("Multiplayers + AI".equals(selectedHomeMode) && nSlots >= 3) {
                aiSlot = nSlots - 1;  // last seat is the AI in multiplayer+AI
            }
            for (int i = 0; i < lobbyNameFields.size(); i++) {
                JTextField f = lobbyNameFields.get(i);  // one row per seat; aiSlot gets Computer default
                String name;
                if (f == null) {
                    name = "";
                } else {
                    name = f.getText().trim();
                }
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Enter all player names.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                appendSeat(name, i == aiSlot);
            }
        }
        // Mode-specific seat counts before we clear old save and start fresh.
        if (seatNames.size() < 2) {
            JOptionPane.showMessageDialog(frame, "Need at least " + 2 + " players.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (seatNames.size() > 9) {
            JOptionPane.showMessageDialog(frame,
                    "Too many players (" + 9 + " max for this layout). Reduce count or use Custom ≤ "
                            + 9 + ".",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("Player vs Player".equals(selectedHomeMode) && seatNames.size() != 2) {
            JOptionPane.showMessageDialog(frame, "Player vs Player must have exactly 2 players.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            rebuildLobbyNameFields(2);
            return;
        }
        if ("Player vs AI".equals(selectedHomeMode) && seatNames.size() != 2) {
            JOptionPane.showMessageDialog(frame, "Player vs AI requires exactly 2 players (you and the AI).", "Error",
                    JOptionPane.ERROR_MESSAGE);
            rebuildLobbyNameFields(2);
            return;
        }
        if (contest && seatNames.size() != 2) {
            JOptionPane.showMessageDialog(frame, "AI vs AI Contest requires exactly 2 players (your AI and opponent).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            rebuildLobbyNameFields(2);
            return;
        }
        if ("Multiplayers + AI".equals(selectedHomeMode) && seatNames.size() < 3) {
            JOptionPane.showMessageDialog(frame,
                    "Multiplayers + AI needs at least 3 players (include one AI as the last seat).",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (lobbyTurnOrderBox != null) {
            String mode = (String) lobbyTurnOrderBox.getSelectedItem();
            if ("Randomize order".equals(mode)) {
                shuffleSeatOrder();
            }
        }
        BoggleSave.save(null, true);  // drop any old in-progress save — this is a brand-new match
        pendingStartIndex = 0;
        if (lobbyFirstTurnBox != null) {
            String pick = (String) lobbyFirstTurnBox.getSelectedItem();
            if ("Random".equals(pick)) {
                pendingStartIndex = (int) (Math.random() * seatNames.size());
            } else if (pick != null && pick.startsWith("Player ")) {
                try {
                    int idx = Integer.parseInt(pick.substring("Player ".length()).trim()) - 1;
                    if (idx >= 0 && idx < seatNames.size()) {
                        pendingStartIndex = idx;
                    }
                } catch (NumberFormatException ignored) {
                    pendingStartIndex = 0;
                }
            }
        }
        resetMatchState();
        currentPlayerIndex = pendingStartIndex;
        userPrefs.applyRuntimeRules();
        if (contest) {
            if (pendingContestBoard == null && !loadContestBoard(contestBoardFile())) {
                JOptionPane.showMessageDialog(frame,
                        "contest_board.txt is missing or invalid.\n\n"
                                + "Paste the teacher's 5 lines into:\n"
                                + contestBoardFile().getAbsolutePath(),
                        "Contest board",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (pendingContestBoard != null) {
            for (int r = 0; r < BOARD_SIZE; r++) {
                board[r] = Arrays.copyOf(pendingContestBoard[r], BOARD_SIZE);
            }
            pendingContestBoard = null;
            repaintTrace();
        } else if (!contest) {
            generateBoggleBoard();
        }
        if (contest) {
            JOptionPane.showMessageDialog(frame,
                    "AI vs AI Contest\n\n"
                            + "Your AI plays automatically (Impossible, no timer).\n"
                            + "The opponent types their words on this app when it is their turn.",
                    "Contest mode",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        mountGameUi(true);
        // Full-screen game layout after lobby.
        frame.dispose();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        cards.show(deck, "game");
    }

    
    /**
     * Mounts the in-game UI card for a new or resumed match.
     * @param emitOpeningLogs whether to print opening log lines
     */
    public static void mountGameUi(boolean emitOpeningLogs) {
        // Builds board, logs, scores, and controls into gameMount. false when resuming a save.
        tracePath.clear();
        lastScoredWordPath.clear();
        traceDragging = false;
        pressR = -1;
        pressC = -1;
        pressDragged = false;
        gamePaused = false;
        gameMount.removeAll();
        if (emitOpeningLogs) {
            // New match: do not inject old log text from a previous session.
            pendingMatchLogBody = "";
            pendingPlayerLogBodies = null;
        }

        JPanel gamePanel = new JPanel(new BorderLayout());  // rebuild entire game UI
        gameLivePanel = gamePanel;
        gamePanel.setBackground(activePalette.appBg);

        allLog = new JTextPane();
        allLog.setEditable(false);
        allLog.setBackground(activePalette.editorBg);
        ensureLogStyles(allLog);
        playerLogs = new JTextPane[seatNames.size()];
        for (int i = 0; i < playerLogs.length; i++) {
            playerLogs[i] = new JTextPane();
            playerLogs[i].setEditable(false);
            playerLogs[i].setBackground(activePalette.editorBg);
            ensureLogStyles(playerLogs[i]);
        }

        if (emitOpeningLogs) {
            appendToLogPane(allLog, "Round " + currentRoundNumber + " / " + userPrefs.totalRounds + " started.", "plain");
            appendToLogPane(allLog, "Players: " + playerNamesLine() + ".", "plain");
            appendToLogPane(allLog, "First turn: " + seatNames.get(currentPlayerIndex) + ".", "plain");
        appendToLogPane(allLog, "Board generated.", "plain");
        if (DICTIONARY.isEmpty()) {
                appendToLogPane(allLog, "Warning: no dictionary — add src/resources/wordlist.txt.", "plain");
            }
        }

        gamePanel.add(buildGameNorth(), BorderLayout.NORTH);
        gamePanel.add(buildGameWestLogs(), BorderLayout.WEST);
        gamePanel.add(buildGameCenterBoard(), BorderLayout.CENTER);
        gamePanel.add(buildGameEastScores(), BorderLayout.EAST);
        gamePanel.add(buildGameSouthControls(), BorderLayout.SOUTH);

        gameMount.add(gamePanel, BorderLayout.CENTER);
        gameMount.revalidate();
        gameMount.repaint();

        refreshBoardButtons();
        for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
            scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
        }
        highlightCurrentPlayerBorder();
        if (shakeButton != null) {
            boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                    && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }
        if (timerLabel != null && userPrefs.timerSeconds <= 0) {
            timerLabel.setText("Timer: Off");
        }
        restartTurnTimer();
        showFeedbackLine("Click or drag adjacent letters to build a word, then Submit or press Enter.",
                "HINT");
        enablePlayControls();
        runAiTurn();
        BoggleAudio.startBackgroundMusic(userPrefs);
        if (!seatNames.isEmpty() && !gameOver) {
            BoggleSave.save(null, false);
        }
    }

    
    /**
     * Restores a saved match and mounts gameplay UI.
     */
    public static void resumeSavedMatch() {
        // Load save.txt for this guest, rebuild match state, then mount game UI without "new match" logs.
        java.io.File f = new File(BoggleSave.guestSaveGameDir(guestName), "save.txt");
        if (f == null || !f.exists()) {
            JOptionPane.showMessageDialog(frame,
                    "No saved match available.",
                    "Resume game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            HashMap<String, String> saveValues = new HashMap<>();
            HashMap<String, String> saveBlocks = new HashMap<>();
            if (!BoggleSave.parseSave(Files.readAllLines(f.toPath(), StandardCharsets.UTF_8), saveValues, saveBlocks)
                    || !BoggleSave.restoreGame(saveValues, saveBlocks)) {
                JOptionPane.showMessageDialog(frame,
                        "No saved match available.",
                        "Resume game", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "No saved match available.",
                    "Resume game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        mountGameUi(false);
        // BoggleSave.restoreGame left lastScoredWordPath from save; fade it out like a live submit.
        if (lastScoredPathTimer != null && lastScoredPathTimer.isRunning()) {
            lastScoredPathTimer.stop();
        }
        lastScoredPathTimer = null;
        if (!lastScoredWordPath.isEmpty()) {
            lastScoredPathTimer = new javax.swing.Timer(500, e -> {
                if (lastScoredPathTimer != null && lastScoredPathTimer.isRunning()) {
                    lastScoredPathTimer.stop();
                }
                lastScoredPathTimer = null;
                lastScoredWordPath.clear();
                repaintTrace();
            });
            lastScoredPathTimer.setRepeats(false);
            lastScoredPathTimer.start();
        }
        appendToLogPane(allLog, "Resumed previous match.", "plain");
        appendToLogPane(allLog, "Round " + currentRoundNumber + " / " + userPrefs.totalRounds
                + " — turn: " + seatNames.get(currentPlayerIndex) + ".", "plain");
        frame.dispose();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        cards.show(deck, "game");
    }

    
    /**
     * Builds the top gameplay status/controls bar.
     * @return north gameplay panel
     */
    public static JPanel buildGameNorth() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));
        topPanel.setBackground(activePalette.appBg);

        timerLabel = new JLabel("Timer: …", JLabel.CENTER);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
        timerLabel.setForeground(activePalette.textStrong);

        turnLabel = new JLabel("Now playing: …", JLabel.CENTER);
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        turnLabel.setForeground(activePalette.accent);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.PAGE_AXIS));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(timerLabel);
        col.add(Box.createVerticalStrut(4));
        col.add(turnLabel);
        topPanel.add(col, BorderLayout.CENTER);
        gameNorthPanel = topPanel;
        return topPanel;
    }


    /**
     * Builds the left-side gameplay log panel set.
     * @return west gameplay panel
     */
    public static JPanel buildGameWestLogs() {
        // Tab 1 = everyone; other tabs = one player each (styled lines, not plain JTextArea).
        JTabbedPane logTabs = new JTabbedPane();
        allLog.setBackground(activePalette.editorBg);
        allLog.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        if (allLog != null) {
            String allBody = pendingMatchLogBody;
            if (allBody == null) {
                allBody = "";
            }
            // Resume injects saved log text here; then pending buffer is cleared.
            StyledDocument allDoc = allLog.getStyledDocument();
            try {
                allDoc.remove(0, allDoc.getLength());
            } catch (BadLocationException ignored) {
            }
            ensureLogStyles(allLog);
            if (!allBody.isEmpty()) {
                try {
                    Style st = allDoc.getStyle("plain");
                    allDoc.insertString(0, allBody, st);
                } catch (BadLocationException ignored) {
                }
            }
            int allLen = allLog.getDocument().getLength();
            if (allLen > 0) {
                allLog.setCaretPosition(allLen);
            } else {
                allLog.setCaretPosition(0);
            }
        }
        pendingMatchLogBody = "";
        JScrollPane allScroll = new JScrollPane(allLog);
        allScroll.setBorder(BorderFactory.createLineBorder(activePalette.line));
        logTabs.addTab("All Players", allScroll);
        for (int i = 0; i < seatNames.size(); i++) {
            playerLogs[i].setBackground(activePalette.editorBg);
            playerLogs[i].setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            if (playerLogs[i] != null) {
                String plBody = "";
                if (pendingPlayerLogBodies != null && i < pendingPlayerLogBodies.length
                        && pendingPlayerLogBodies[i] != null) {
                    plBody = pendingPlayerLogBodies[i];
                }
                StyledDocument plDoc = playerLogs[i].getStyledDocument();
                try {
                    plDoc.remove(0, plDoc.getLength());
                } catch (BadLocationException ignored) {
                }
                ensureLogStyles(playerLogs[i]);
                if (!plBody.isEmpty()) {
                    try {
                        Style st = plDoc.getStyle("plain");
                        plDoc.insertString(0, plBody, st);
                    } catch (BadLocationException ignored) {
                    }
                }
                int plLen = playerLogs[i].getDocument().getLength();
                if (plLen > 0) {
                    playerLogs[i].setCaretPosition(plLen);
                } else {
                    playerLogs[i].setCaretPosition(0);
                }
            }
            JScrollPane ps = new JScrollPane(playerLogs[i]);
            ps.setBorder(BorderFactory.createLineBorder(activePalette.line));
            logTabs.addTab(seatNames.get(i), ps);
        }
        pendingPlayerLogBodies = null;
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrap.setBackground(activePalette.appBg);
        wrap.add(logTabs, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(340, 0));
        gameWestWrap = wrap;
        return wrap;
    }

    /**
     * Builds the center board panel and tile grid.
     * @return center gameplay panel
     */
    public static JPanel buildGameCenterBoard() {
        
        
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 22, 22)) {
            
            /**
             * Paint component.
             * @param g G
             */
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (boardGridPanel == null || cells == null) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Green line = last word that scored; orange line = word you're building now.
                paintTrace(g2, lastScoredWordPath, 8f, new Color(56, 142, 72, 210));
                if (traceDragging && tracePath.size() >= 2) {
                    paintTrace(g2, tracePath, 12f, new Color(activePalette.accent.getRed(), activePalette.accent.getGreen(), activePalette.accent.getBlue(), 170));
                }
                g2.dispose();
            }
        };
        boardGridPanel = boardPanel;
        boardPanel.setBorder(BorderFactory.createEmptyBorder(18, 18,
                18, 18));
        boardPanel.setPreferredSize(new Dimension(484, 484));
        boardPanel.setBackground(activePalette.appBg);
        cells = new JButton[BOARD_SIZE][BOARD_SIZE];
        Font cellFont = new Font("Arial Rounded MT Bold", Font.BOLD, 44);
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                JButton cell = new JButton(" ");
                cell.setFont(cellFont);
                cell.setForeground(activePalette.textStrong);
                cell.setFocusable(false);
                cell.setBackground(activePalette.tileBg);
                cell.setOpaque(true);
                cell.setContentAreaFilled(true);
                cell.setFocusPainted(false);
                cell.setBorder(BorderFactory.createLineBorder(activePalette.line, 2));
                cell.setMargin(new Insets(6, 6, 6, 6));
                Dimension cellSize = new Dimension(72, 72);
                cell.setMinimumSize(cellSize);
                cell.setPreferredSize(cellSize);
                cell.setToolTipText("Click letters or drag across the board to build a word.");
                final int rr = r;
                final int cc = c;
                cell.addMouseListener(new MouseAdapter() {
                    
                    /**
                     * Starts a drag trace from a board cell press.
                     * @param e E
                     */
                    public void mousePressed(MouseEvent e) {
                        if (wordField == null || !wordField.isEnabled()) {
                            pressR = -1;
                            pressC = -1;
                            pressDragged = false;
                            return;
                        }
                        // Remember start cell; drag vs click decided on release.
                        pressR = rr;
                        pressC = cc;
                        pressDragged = false;
                    }

                    
                    /**
                     * Extends the drag trace when entering adjacent cells.
                     * @param e E
                     */
                    public void mouseEntered(MouseEvent e) {
                        if (pressR < 0) {
                            return;
                        }
                        if (!pressDragged) {
                            // First enter after press starts a drag from the pressed cell.
                            pressDragged = true;
                            tracePath.clear();
                            traceDragging = true;
                            tracePath.add(new int[] {pressR, pressC});
                            if (wordField != null) {
                                StringBuilder sb = new StringBuilder();
                                for (int[] p : tracePath) {
                                    sb.append(Character.toLowerCase(board[p[0]][p[1]]));
                                }
                                wordField.setText(sb.toString());
                            }
                            repaintTrace();
                        }
                        if (tracePath.isEmpty()) {
                            return;
                        }
                        int[] last = tracePath.get(tracePath.size() - 1);
                        if (last[0] == rr && last[1] == cc) {
                            return;
                        }
                        // Backtrack: re-entering the previous cell removes the last step (undo one letter).
                        if (tracePath.size() >= 2) {
                            int[] prev = tracePath.get(tracePath.size() - 2);
                            if (prev[0] == rr && prev[1] == cc) {
                                tracePath.remove(tracePath.size() - 1);
                                if (wordField != null) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int[] p : tracePath) {
                                        sb.append(Character.toLowerCase(board[p[0]][p[1]]));
                                    }
                                    wordField.setText(sb.toString());
                                }
                                repaintTrace();
                                return;
                            }
                        }
                        if (Math.abs(last[0] - rr) > 1 || Math.abs(last[1] - cc) > 1) {
                            return;
                        }
                        // Cannot reuse a cell already in this word.
                        for (int[] p : tracePath) {
                            if (p[0] == rr && p[1] == cc) {
                                return;
                            }
                        }
                        tracePath.add(new int[] {rr, cc});
                        if (wordField != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int[] p : tracePath) {
                                sb.append(Character.toLowerCase(board[p[0]][p[1]]));
                            }
                            wordField.setText(sb.toString());
                        }
                        repaintTrace();
                    }

                    
                    /**
                     * Completes click or drag trace on cell release.
                     * @param e E
                     */
                    public void mouseReleased(MouseEvent e) {
                        int r = pressR;
                        int c = pressC;
                        boolean wasDrag = pressDragged;
                        int finalPathSize = tracePath.size();
                        traceDragging = false;
                        pressR = -1;
                        pressC = -1;
                        pressDragged = false;
                        if (!wasDrag && r >= 0 && c >= 0) {
                            // Single click without entering other cells: toggle/append one letter.
                            appendCellAsClick(r, c);
                            return;
                        }
                        // Drag release auto-submits once when path is long enough (Enter not required).
                        if (wasDrag && wordField != null && wordField.isEnabled()) {
                            if (finalPathSize >= userPrefs.minWordLength) {
                                if (dragAutoSubmit) {
                                    return;
                                }
                                dragAutoSubmit = true;
                                SwingUtilities.invokeLater(() -> {
                                    onSubmit();
                                    dragAutoSubmit = false;
                                });
                            }
                        }
                    }
                });
                cells[r][c] = cell;
                boardPanel.add(cell);
            }
        }

        JPanel squareHolder = new JPanel(new GridBagLayout());
        squareHolder.setOpaque(false);
        GridBagConstraints boardGbc = new GridBagConstraints();
        boardGbc.gridx = 0;
        boardGbc.gridy = 0;
        boardGbc.anchor = GridBagConstraints.CENTER;
        squareHolder.add(boardPanel, boardGbc);

        JPanel boardWrap = new JPanel(new BorderLayout());
        boardWrap.setOpaque(true);
        boardWrap.setBackground(activePalette.boardFrame);
        boardWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.textStrong, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        boardWrap.add(squareHolder, BorderLayout.CENTER);
        gameBoardWrap = boardWrap;

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        center.setBackground(activePalette.appBg);
        center.add(boardWrap, BorderLayout.CENTER);
        return center;
    }

    
    /**
     * Paints a polyline trace over board cell centers.
     * @param g2 graphics context
     * @param path ordered row/col positions
     * @param strokeW line width
     * @param color line color
     */
    public static void paintTrace(Graphics2D g2, ArrayList<int[]> path, float strokeW, Color color) {
        if (path == null || path.size() < 2 || cells == null || boardGridPanel == null) {
            return;
        }
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        for (int i = 1; i < path.size(); i++) {
            JButton ca = cells[path.get(i - 1)[0]][path.get(i - 1)[1]];
            JButton cb = cells[path.get(i)[0]][path.get(i)[1]];
            // Line between cell centers (drawn on the grid panel, not on buttons).
            if (ca == null || cb == null) {
                continue;
            }
            g2.drawLine(ca.getX() + ca.getWidth() / 2, ca.getY() + ca.getHeight() / 2, cb.getX() + cb.getWidth() / 2, cb.getY() + cb.getHeight() / 2);
        }
    }



    /**
     * Builds the right-side player score cards.
     * @return east gameplay panel
     */
    public static JPanel buildGameEastScores() {
        // Score cards on the right; Pause opens a modal that freezes timers and AI.
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        playerPanel.setBackground(activePalette.appBg);

        playerCards = new JPanel[seatNames.size()];
        scoreLabels = new JLabel[seatNames.size()];

        for (int i = 0; i < seatNames.size(); i++) {
            JPanel card = new JPanel();
            card.setOpaque(true);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(activePalette.line),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            card.setBackground(activePalette.panelBg);

            JLabel name = new JLabel(seatNames.get(i));
            name.setFont(new Font("SansSerif", Font.BOLD, 20));
            name.setForeground(activePalette.textStrong);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            scoreLabels[i] = new JLabel("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
            scoreLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 17));
            scoreLabels[i].setForeground(activePalette.textMuted);
            scoreLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(name);
            card.add(scoreLabels[i]);

            playerCards[i] = card;
            playerPanel.add(card);
            playerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        JButton menuButton = new JButton("Pause");
        gameMenuBtn = menuButton;
        styleGameSecondaryButton(menuButton);
        menuButton.addActionListener(e -> {
            if (gameOver || frame == null) {
                return;
            }
            pauseGameMatch();

            JDialog d = new JDialog(frame, "Pause", true);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
            root.setBackground(activePalette.appBg);
            JTextArea msg = new JTextArea("The match is paused. The turn timer and AI are frozen until you continue.");
            msg.setEditable(false);
            msg.setLineWrap(true);
            msg.setWrapStyleWord(true);
            msg.setOpaque(false);
            msg.setFont(new Font("SansSerif", Font.PLAIN, 15));
            root.add(msg, BorderLayout.NORTH);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            btnRow.setOpaque(false);
            JButton resumeBtn = new JButton("Resume");
            styleGamePrimaryButton(resumeBtn);
            resumeBtn.setPreferredSize(new Dimension(160, 44));
            JButton homeBtn = new JButton("Return to main menu");
            styleGameSecondaryButton(homeBtn);
            homeBtn.setPreferredSize(new Dimension(220, 44));

            resumeBtn.addActionListener(x -> {
                resumeGameMatch();
                d.dispose();
            });
            homeBtn.addActionListener(x -> {
                int pick = JOptionPane.showConfirmDialog(d,
                        "Return to the main menu. An in-progress match is saved so you can resume later.",
                        "Return to main menu",
                        JOptionPane.YES_NO_OPTION);
                if (pick != JOptionPane.YES_OPTION) {
                    return;
                }
                d.dispose();
                goToMenu(true);
            });
            btnRow.add(resumeBtn);
            btnRow.add(homeBtn);
            root.add(btnRow, BorderLayout.SOUTH);

            d.setContentPane(root);
            d.pack();
            d.setLocationRelativeTo(frame);
            d.addWindowListener(new WindowAdapter() {
                
                /**
                 * Handles dialog close cleanup.
                 * @param x X
                 */
                public void windowClosed(WindowEvent x) {
                    if (gamePaused) {
                        resumeGameMatch();
                    }
                }
            });
            d.setVisible(true);
        });

        wordsHintButton = new JButton("Hint");
        styleGameSecondaryButton(wordsHintButton);
        wordsHintButton.setToolTipText("All valid words still playable on this board (longest first; excludes words already played).");
        wordsHintButton.addActionListener(e -> showValidWordsHintDialog());
        wordsHintButton.setEnabled(userPrefs.allowBoardHint);

        JButton helpButton = new JButton("Help");
        gameHelpBtn = helpButton;
        styleGameSecondaryButton(helpButton);
        helpButton.setToolTipText("Open Game Rules (tabs: how to play, controls, settings).");
        helpButton.addActionListener(e -> showWelcomeRulesDialog());

        
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        navRow.setOpaque(false);
        Dimension navBtn = new Dimension(92, 40);
        menuButton.setPreferredSize(navBtn);
        wordsHintButton.setPreferredSize(navBtn);
        helpButton.setPreferredSize(navBtn);
        navRow.add(menuButton);
        navRow.add(wordsHintButton);
        navRow.add(helpButton);

        saveQuitButton = new JButton("Quit the game");
        styleGameSecondaryButton(saveQuitButton);
        saveQuitButton.setToolTipText("Save the match and exit the application. You can resume when you start the game again.");
        saveQuitButton.addActionListener(e -> {
            int pick = JOptionPane.showConfirmDialog(frame,
                    "Save the current match and quit the application. You can resume when you start the game again.",
                    "Quit the game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (pick != JOptionPane.YES_OPTION) {
                return;
            }
            if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
                aiMoveTimer.stop();
            }
            aiMoveTimer = null;
            stopTurnTimer();
            if (!seatNames.isEmpty() && !gameOver) {
                BoggleSave.save(null, false);
            } else {
                BoggleSave.persistPrefs();
            }
            System.exit(0);
        });

        quitWithoutSaveButton = new JButton("Menu");
        styleGameSecondaryButton(quitWithoutSaveButton);
        quitWithoutSaveButton.setToolTipText("Save the match and return to the main menu. You can resume from home or next launch.");
        quitWithoutSaveButton.addActionListener(e -> goToMenu(false));

        final int eastBarW = 288;
        Dimension fullBarBtn = new Dimension(eastBarW, 42);
        saveQuitButton.setPreferredSize(fullBarBtn);
        saveQuitButton.setMaximumSize(fullBarBtn);
        saveQuitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitWithoutSaveButton.setPreferredSize(fullBarBtn);
        quitWithoutSaveButton.setMaximumSize(fullBarBtn);
        quitWithoutSaveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel saveMatchBlock = new JPanel();
        saveMatchBlock.setLayout(new BoxLayout(saveMatchBlock, BoxLayout.PAGE_AXIS));
        saveMatchBlock.setOpaque(false);
        saveMatchBlock.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveMatchBlock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, activePalette.line),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)));
        gameSaveBlock = saveMatchBlock;
        saveMatchBlock.add(quitWithoutSaveButton);
        saveMatchBlock.add(Box.createRigidArea(new Dimension(0, 10)));
        saveMatchBlock.add(saveQuitButton);

        JPanel eastButtons = new JPanel();
        eastButtons.setLayout(new BoxLayout(eastButtons, BoxLayout.Y_AXIS));
        eastButtons.setOpaque(false);
        eastButtons.setBorder(BorderFactory.createEmptyBorder(10, 12, 18, 12));
        eastButtons.add(navRow);
        eastButtons.add(Box.createRigidArea(new Dimension(0, 16)));
        eastButtons.add(saveMatchBlock);

        JPanel east = new JPanel(new BorderLayout());
        east.setBackground(activePalette.appBg);
        JScrollPane scroll = new JScrollPane(playerPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        east.add(scroll, BorderLayout.CENTER);
        east.add(eastButtons, BorderLayout.SOUTH);
        east.setPreferredSize(new Dimension(320, 0));
        gameEastPanel = east;
        return east;
    }

    /**
     * Builds the bottom gameplay control area.
     * @return south gameplay panel
     */
    public static JPanel buildGameSouthControls() {
        wordField = new JTextField();
        wordField.setFont(new Font("SansSerif", Font.PLAIN, 24));
        wordField.setPreferredSize(new Dimension(0, 52));
        wordField.setMinimumSize(new Dimension(120, 52));
        wordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activePalette.line, 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        wordField.addActionListener(e -> onSubmit());

        submitButton = new JButton("Submit");
        styleGamePrimaryButton(submitButton);
        submitButton.addActionListener(e -> onSubmit());

        passButton = new JButton("Pass");
        styleGameSecondaryButton(passButton);
        passButton.addActionListener(e -> onPass());

        shakeButton = new JButton("Shake Board");
        styleGameSecondaryButton(shakeButton);
        shakeButton.setEnabled(false);
        shakeButton.addActionListener(e -> onShake());

        restartButton = new JButton("Restart");
        styleGameSecondaryButton(restartButton);
        restartButton.addActionListener(e -> {
            if (gamePaused) {
                return;
            }
            int pick = JOptionPane.showConfirmDialog(frame,
                    "Restart the match. Scores and words reset, new board.",
                    "Restart",
                    JOptionPane.YES_NO_OPTION);
            if (pick != JOptionPane.YES_OPTION) {
                return;
            }
            if (BoggleAudio.winningClip != null) {
                BoggleAudio.winningClip.stop();
            }
BoggleSave.save(null, true);
            clearAllLogs();
            resetMatchState();
            generateBoggleBoard();
            appendToLogPane(allLog, "--- Restart ---", "plain");
            appendToLogPane(allLog, "New board generated.", "plain");
            refreshBoardButtons();
            for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
                scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
            }
            highlightCurrentPlayerBorder();
            if (shakeButton != null) {
                boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                        && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
                shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
            }
            enablePlayControls();
            restartTurnTimer();
            showFeedbackLine("Click or drag adjacent letters to build a word, then Submit or press Enter.",
                    "HINT");
            runAiTurn();
            BoggleAudio.startBackgroundMusic(userPrefs);
        });

        Dimension compactPlay = new Dimension(168, 46);
        shakeButton.setPreferredSize(compactPlay);
        passButton.setPreferredSize(compactPlay);
        restartButton.setPreferredSize(compactPlay);

        playFeedbackLabel = new JLabel(" ");
        playFeedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playFeedbackLabel.setVerticalAlignment(SwingConstants.CENTER);
        playFeedbackLabel.setOpaque(true);
        playFeedbackLabel.setBackground(activePalette.bannerBg);
        playFeedbackLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, activePalette.accent),
                BorderFactory.createEmptyBorder(6, 16, 8, 16)));
        
        Dimension feedbackSize = new Dimension(0, 52);
        playFeedbackLabel.setPreferredSize(feedbackSize);
        playFeedbackLabel.setMinimumSize(feedbackSize);
        playFeedbackLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel wordPrompt = new JLabel("Word:");
        wordPrompt.setFont(new Font("SansSerif", Font.BOLD, 22));
        wordPrompt.setForeground(activePalette.textStrong);

        clearWordsButton = new JButton("Clear");
        styleGameSecondaryButton(clearWordsButton);
        clearWordsButton.setPreferredSize(new Dimension(110, 46));
        clearWordsButton.setToolTipText("Clear the current letter trace and word field.");
        clearWordsButton.addActionListener(e -> {
            if (gamePaused) {
                return;
            }
            tracePath.clear();
            traceDragging = false;
            pressR = -1;
            pressC = -1;
            pressDragged = false;
            if (wordField != null) {
                wordField.setText("");
            }
            repaintTrace();
            if (wordField != null && wordField.isEnabled()) {
                SwingUtilities.invokeLater(() -> {
                    if (wordField != null && wordField.isEnabled() && wordField.isDisplayable()) {
                        wordField.requestFocusInWindow();
                    }
                });
            }
        });

        JPanel wordMiddle = new JPanel(new BorderLayout(10, 0));
        wordMiddle.setOpaque(false);
        wordMiddle.add(wordPrompt, BorderLayout.WEST);
        wordMiddle.add(wordField, BorderLayout.CENTER);
        wordMiddle.add(clearWordsButton, BorderLayout.EAST);

        JPanel wordBar = new JPanel(new BorderLayout(16, 0));
        wordBar.setOpaque(false);
        wordBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        wordBar.add(wordMiddle, BorderLayout.CENTER);
        wordBar.add(submitButton, BorderLayout.EAST);
        submitButton.setPreferredSize(new Dimension(160, 52));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        actionRow.add(shakeButton);
        actionRow.add(passButton);
        actionRow.add(restartButton);

        JPanel southStack = new JPanel(new BorderLayout());
        southStack.setOpaque(false);
        southStack.add(playFeedbackLabel, BorderLayout.NORTH);
        southStack.add(wordBar, BorderLayout.CENTER);
        southStack.add(actionRow, BorderLayout.SOUTH);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(southStack, BorderLayout.CENTER);
        gameSouthPanel = south;
        return south;
    }

    /**
     * Applies primary in-game button styling.
     * @param b target button
     */
    public static void styleGamePrimaryButton(JButton b) {
        b.setBackground(activePalette.action);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setPreferredSize(new Dimension(220, 48));
    }

    /**
     * Applies secondary in-game button styling.
     * @param b target button
     */
    public static void styleGameSecondaryButton(JButton b) {
        b.setBackground(activePalette.panelBg);
        b.setForeground(activePalette.textStrong);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(activePalette.line));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setPreferredSize(new Dimension(220, 48));
    }

    /**
     * Shows a hint dialog listing remaining playable words.
     */
    public static void showValidWordsHintDialog() {
        // Same word search as the AI uses — can take a second on a full board, so we run it off the UI thread.
        if (!userPrefs.allowBoardHint) {
            JOptionPane.showMessageDialog(frame,
                    "Board hints are turned off in Settings → Gameplay.",
                    "Words hint", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (frame == null) {
            return;
        }
        if (gameOver) {
            JOptionPane.showMessageDialog(frame, "The match is over — no active board.", "Words hint",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (DICTIONARY.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Dictionary is still loading or missing. Add src/resources/wordlist.txt and restart.",
                    "Words hint", JOptionPane.WARNING_MESSAGE);
            return;
        }
        wordsHintButton.setEnabled(false);
        wordsHintButton.setText("Searching…");
        new Thread(() -> {
            ArrayList<String> words = boardModel.findAllPlayableWordsSorted(usedWords, userPrefs.minWordLength);
            // Dialog must be created on EDT after DFS finishes.
            SwingUtilities.invokeLater(() -> {
                wordsHintButton.setText("Hint");
                wordsHintButton.setEnabled(!gameOver && userPrefs.allowBoardHint);
                StringBuilder body = new StringBuilder();
                for (int i = 0; i < words.size(); i++) {
                    if (i > 0) {
                        body.append(", ");
                    }
                    body.append(words.get(i));
                }
                String areaText;
                if (body.length() == 0) {
                    areaText = "(No words left that fit the rules.)";
                } else {
                    areaText = body.toString();
                }
                JTextArea area = new JTextArea(areaText);
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(560, 420));
                JPanel panel = new JPanel(new BorderLayout(0, 10));
                JTextArea head = new JTextArea(
                        words.size() + " word(s) you can still enter - longest first, min length "
                                + userPrefs.minWordLength + ". Already-played words are excluded.");
                head.setEditable(false);
                head.setLineWrap(true);
                head.setWrapStyleWord(true);
                head.setOpaque(false);
                head.setFont(new Font("SansSerif", Font.PLAIN, 14));
                panel.add(head, BorderLayout.NORTH);
                panel.add(scroll, BorderLayout.CENTER);
                JOptionPane.showMessageDialog(frame, panel, "Words hint", JOptionPane.PLAIN_MESSAGE);
            });
        }, "boggle-hint-search").start();
    }

    /**
     * Handles submit action for the current typed/traced word.
     */
    public static void onSubmit() {
        if (gameOver || gamePaused) {
            return;
        }
        // Remember who was up before scoring — we restart the turn timer only if turn changed.
        int beforeIndex = currentPlayerIndex;
        String text = wordField.getText();
        String trimmed;
        if (text == null) {
            trimmed = "";
        } else {
            trimmed = text.trim();
        }

        // Keep drag path before logic clears trace; used to highlight letters on success.
        ArrayList<int[]> traceSnapshot = new ArrayList<>(tracePath.size());
        for (int[] cell : tracePath) {
            traceSnapshot.add(new int[] {cell[0], cell[1]});
        }

        wordField.setText("");

        String[] out = BoggleGameplay.submitWord(text);
        if ("REJECT".equals(out[2])) {
            showFeedbackLine(out[0], "ERROR");
            BoggleAudio.playReject(userPrefs);
        } else if ("OK".equals(out[2]) || "WIN".equals(out[2])) {
            showFeedbackLine(out[0], "SUCCESS");
            if (!trimmed.isEmpty()) {
                BoggleAudio.playScoreSFX(trimmed.length(), userPrefs);
            }
        }

        if (!"REJECT".equals(out[2])) {
            // Humans get green path flash; AI submit reuses path until next human scores.
            if (!(beforeIndex >= 0 && beforeIndex < seats.size() && seats.get(beforeIndex).isAi()) || lastScoredWordPath.isEmpty()) {
                lastScoredWordPath.clear();
                if (trimmed != null && !trimmed.isEmpty()) {
                    String wordLc = trimmed.toLowerCase(Locale.ROOT);
                    ArrayList<int[]> highlight = null;
                    if (traceSnapshot.size() == wordLc.length()) {
                        StringBuilder traceWord = new StringBuilder(traceSnapshot.size());
                        for (int[] p : traceSnapshot) {
                            traceWord.append(Character.toLowerCase(board[p[0]][p[1]]));
                        }
                        if (traceWord.toString().equals(wordLc)) {
                            highlight = traceSnapshot;
                        }
                    }
                    if (highlight == null) {
                        highlight = boardModel.findBoardPathForWord(trimmed);
                    }
                    if (highlight == null) {
                        highlight = boardModel.findBoardPathForWord(wordLc);
                    }
                    if (highlight != null) {
                        for (int[] p : highlight) {
                            lastScoredWordPath.add(new int[] {p[0], p[1]});
                        }
                    }
                }
                if (lastScoredPathTimer != null && lastScoredPathTimer.isRunning()) {
                    lastScoredPathTimer.stop();
                }
                lastScoredPathTimer = null;
                if (!lastScoredWordPath.isEmpty()) {
                    // Brief green highlight, then clear so the next trace is visible.
                    lastScoredPathTimer = new javax.swing.Timer(500, e -> {
                        if (lastScoredPathTimer != null && lastScoredPathTimer.isRunning()) {
                            lastScoredPathTimer.stop();
                        }
                        lastScoredPathTimer = null;
                        lastScoredWordPath.clear();
                        repaintTrace();
                    });
                    lastScoredPathTimer.setRepeats(false);
                    lastScoredPathTimer.start();
                }
            }
            wordField.setText("");
        }

        refreshBoardButtons();
        for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
            scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
        }
        highlightCurrentPlayerBorder();
        if (shakeButton != null) {
            boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                    && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }

        if (Boolean.parseBoolean(out[1]) && winnerSeat >= 0) {
            stopTurnTimer();
            seats.get(winnerSeat).winRound();
            syncSeats();
            if (currentRoundNumber < userPrefs.totalRounds) {
                // Hit target score but more rounds left — celebrate, then fresh board and zero round scores.
                appendToLogPane(allLog, seatNames.get(winnerSeat) + " wins round " + currentRoundNumber + "!", "winGold");
                if (playerLogs != null && winnerSeat >= 0 && winnerSeat < playerLogs.length && playerLogs[winnerSeat] != null) {
                    appendToLogPane(playerLogs[winnerSeat], "Won round " + currentRoundNumber + "!", "winGold");
                }
                showRoundCelebrationDialog(seatNames.get(winnerSeat), currentRoundNumber);
                currentRoundNumber++;
                resetForNextRound();
                refreshBoardButtons();
                for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
                    scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
                }
                highlightCurrentPlayerBorder();
                if (shakeButton != null) {
                    boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                            && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
                    shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
                }
                enablePlayControls();
                restartTurnTimer();
                runAiTurn();
            } else {
                if (wordField != null) {
                    wordField.setEnabled(false);
                }
                if (submitButton != null) {
                    submitButton.setEnabled(false);
                }
                if (passButton != null) {
                    passButton.setEnabled(false);
                }
                if (shakeButton != null) {
                    shakeButton.setEnabled(false);
                }
                gameOver();  // final round / match over
            }
        } else if (!gameOver) {
            if (currentPlayerIndex != beforeIndex) {
                restartTurnTimer();
            }
        }
        if (!gameOver) {
            enablePlayControls();
        }
        tracePath.clear();
        traceDragging = false;
        pressR = -1;
        pressC = -1;
        pressDragged = false;
        repaintTrace();
        runAiTurn();
        if (!seatNames.isEmpty() && !gameOver) {
            BoggleSave.save(null, false);
        } else {
            BoggleSave.save(null, true);
        }
    }

    /**
     * Handles pass action for the active player.
     */
    public static void onPass() {
        if (gameOver || gamePaused) {
            return;
        }
        // Skips scoring and hands the turn to the next seat; may unlock shake after two full pass rounds.
        String[] po = BoggleGameplay.passTurn();
        showFeedbackLine(po[0], "NEUTRAL");

        for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
            scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
        }
        highlightCurrentPlayerBorder();
        if (shakeButton != null) {
            boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                    && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }

        if (!gameOver) {
            restartTurnTimer();
            enablePlayControls();
        }
        tracePath.clear();
        traceDragging = false;
        pressR = -1;
        pressC = -1;
        pressDragged = false;
        repaintTrace();
        runAiTurn();
        // Auto-save after every pass so quit/resume never loses turn order.
        if (!seatNames.isEmpty() && !gameOver) {
            BoggleSave.save(null, false);
        } else {
            BoggleSave.save(null, true);
        }
    }

    /**
     * Builds the match standings panel from ranked seat indices.
     * @param ranked seat indices in ranked order
     * @return standings panel
     */
    public static JPanel buildMatchStandingsPanel(int[] ranked) {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        if (ranked.length >= 2) {
            JPanel row = new JPanel(new GridBagLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(8, 8, 16, 8));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridy = 0;
            gc.anchor = GridBagConstraints.SOUTH;
            gc.insets = new Insets(4, 12, 4, 12);

            // 2nd | 1st (tallest) | 3rd — each slot: seat index, label, color, pedestal height
            int[][] slots = {
                    {1, 68, 185, 190, 198},
                    {0, 92, 255, 209, 92},
                    {2, 52, 198, 142, 88},
            };
            String[] titles = {"2nd", "1st", "3rd"};
            for (int s = 0; s < 3; s++) {
                gc.gridx = s;
                int place = slots[s][0];
                if ((s == 0 || s == 2) && place >= ranked.length) {
                    row.add(Box.createHorizontalStrut(130), gc);
                    continue;
                }
                int seatIdx = ranked[place];
                int rw = seatRoundWins.get(seatIdx);
                int blockH = slots[s][1];
                Color blockColor = new Color(slots[s][2], slots[s][3], slots[s][4]);

                JPanel col = new JPanel();
                col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                col.setOpaque(false);
                col.setAlignmentX(Component.CENTER_ALIGNMENT);
                JPanel head = new JPanel();
                head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
                head.setOpaque(false);
                head.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel name = new JLabel(seatNames.get(seatIdx), JLabel.CENTER);
                name.setFont(new Font("SansSerif", Font.BOLD, 14));
                name.setAlignmentX(Component.CENTER_ALIGNMENT);
                String roundSuffix = rw == 1 ? "" : "s";
                JLabel rounds = new JLabel(rw + " round" + roundSuffix, JLabel.CENTER);
                rounds.setForeground(new Color(68, 68, 68));
                rounds.setFont(new Font("SansSerif", Font.PLAIN, 13));
                rounds.setAlignmentX(Component.CENTER_ALIGNMENT);
                head.add(name);
                head.add(rounds);
                col.add(head);
                col.add(Box.createRigidArea(new Dimension(0, 8)));
                JPanel pedestal = new JPanel(new BorderLayout());
                pedestal.setPreferredSize(new Dimension(132, blockH));
                pedestal.setMinimumSize(new Dimension(112, blockH));
                pedestal.setMaximumSize(new Dimension(170, blockH));
                pedestal.setBackground(blockColor);
                pedestal.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(60, 60, 70), 2),
                        BorderFactory.createEmptyBorder(6, 4, 6, 4)));
                JLabel rk = new JLabel(titles[s], JLabel.CENTER);
                rk.setForeground(new Color(35, 35, 42));
                rk.setFont(new Font("SansSerif", Font.BOLD, 15));
                pedestal.add(rk, BorderLayout.CENTER);
                pedestal.setAlignmentX(Component.CENTER_ALIGNMENT);
                col.add(pedestal);
                row.add(col, gc);
            }
            center.add(row);
            center.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        StringBuilder standingsText = new StringBuilder();
        for (int i = 0; i < ranked.length; i++) {
            int si = ranked[i];
            int rw = seatRoundWins.get(si);
            standingsText.append(i + 1).append(". ").append(seatNames.get(si)).append(" — ").append(rw)
                    .append(" round");
            if (rw != 1) {
                standingsText.append("s");
            }
            standingsText.append(" won\r\n");
        }
        JTextArea standings = new JTextArea(standingsText.toString());
        standings.setEditable(false);
        standings.setOpaque(false);
        standings.setFont(new Font("SansSerif", Font.PLAIN, 15));
        int standingRows = ranked.length + 1;
        if (standingRows < 3) {
            standingRows = 3;
        } else if (standingRows > 8) {
            standingRows = 8;
        }
        standings.setRows(standingRows);
        JScrollPane sp = new JScrollPane(standings);
        sp.setPreferredSize(new Dimension(540, 128));
        sp.setBorder(BorderFactory.createTitledBorder("Results"));
        center.add(sp);
        return center;
    }

    /**
     * Handles end-of-game flow and result presentation.
     */
    public static void gameOver() {
        // All rounds done — podium by round wins, then menu or play again.
        BoggleAudio.playWinningMusic(userPrefs);
        JDialog d = new JDialog(frame, "Match complete", true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.addWindowListener(new WindowAdapter() {
            
            /**
             * Handles dialog close cleanup.
             * @param e E
             */
            public void windowClosed(WindowEvent e) {
                if (BoggleAudio.winningClip != null) {
                    BoggleAudio.winningClip.stop();
                }
            }
        });
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.setBackground(activePalette.appBg);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel title = new JLabel("Match complete", JLabel.CENTER);
        title.setForeground(new Color(12, 74, 110));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Final standings", JLabel.CENTER);
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(subtitle);

        int n = seatNames.size();
        int[] ranked = new int[n];
        for (int si = 0; si < n; si++) {
            ranked[si] = si;
        }
        // ranked[] holds seat indices sorted by rounds won (descending).
        sortSeatsByRounds(ranked, n);

        JPanel center = buildMatchStandingsPanel(ranked);

        JLabel footer = new JLabel("Target per round: " + userPrefs.targetScore + " points", JLabel.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setForeground(activePalette.textMuted);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(footer);
        south.add(Box.createRigidArea(new Dimension(0, 12)));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        btnRow.setOpaque(false);
        JButton returnMenuBtn = new JButton("Return to menu");
        styleGameSecondaryButton(returnMenuBtn);
        returnMenuBtn.setPreferredSize(new Dimension(200, 48));
        returnMenuBtn.addActionListener(e -> {
            d.dispose();
            if (BoggleAudio.winningClip != null) {
                BoggleAudio.winningClip.stop();
            }
            BoggleSave.save(null, true);  // match is over — drop saved game so Resume stays off
            goToMenu(true);
        });
        JButton restartBtn = new JButton("Restart game");
        styleGamePrimaryButton(restartBtn);
        restartBtn.setPreferredSize(new Dimension(220, 48));
        restartBtn.addActionListener(e -> {
            d.dispose();
            if (BoggleAudio.winningClip != null) {
                BoggleAudio.winningClip.stop();
            }
BoggleSave.save(null, true);
            clearAllLogs();
            resetMatchState();
            generateBoggleBoard();
            appendToLogPane(allLog, "--- Restart ---", "plain");
            appendToLogPane(allLog, "New board generated.", "plain");
            refreshBoardButtons();
            for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
                scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
            }
            highlightCurrentPlayerBorder();
            if (shakeButton != null) {
                boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                        && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
                shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
            }
            enablePlayControls();
            restartTurnTimer();
            showFeedbackLine("Click or drag adjacent letters to build a word, then Submit or press Enter.",
                    "HINT");
            runAiTurn();
            BoggleAudio.startBackgroundMusic(userPrefs);
        });
        btnRow.add(returnMenuBtn);
        btnRow.add(restartBtn);
        south.add(btnRow);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setMinimumSize(new Dimension(560, 420));
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    /**
     * Shows round winner celebration dialog.
     * @param winnerName round winner name
     * @param roundNumber completed round number
     */
    public static void showRoundCelebrationDialog(String winnerName, int roundNumber) {
        // Blocking popup between rounds — player taps Continue before the next board appears.
        JDialog d = new JDialog(frame, "Round won", true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        p.setBackground(activePalette.appBg);
        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setOpaque(false);
        JLabel winner = new JLabel(winnerName, JLabel.CENTER);
        winner.setForeground(new Color(12, 74, 110));
        winner.setFont(new Font("SansSerif", Font.BOLD, 22));
        winner.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line2 = new JLabel("wins Round " + roundNumber, JLabel.CENTER);
        line2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        line2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line3 = new JLabel("Next round starting...", JLabel.CENTER);
        line3.setForeground(new Color(100, 116, 139));
        line3.setFont(new Font("SansSerif", Font.PLAIN, 13));
        line3.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.add(winner);
        msg.add(Box.createRigidArea(new Dimension(0, 10)));
        msg.add(line2);
        msg.add(Box.createRigidArea(new Dimension(0, 10)));
        msg.add(line3);
        p.add(msg, BorderLayout.CENTER);
        JButton ok = new JButton("Continue");
        styleGamePrimaryButton(ok);
        ok.setPreferredSize(new Dimension(220, 48));
        ok.addActionListener(e -> d.dispose());
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(ok);
        p.add(south, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    /**
     * Handles Shake Board action when available.
     */
    public static void onShake() {
        if (gamePaused) {
            return;
        }
        if (!shakeAvailable) {
            appendToLogPane(allLog, "Shake Board locked — two full pass rounds unlock it.", "plain");
            return;
        }
        // everyone keeps their score, but letters and played words reset.
        generateBoggleBoard();
        usedWords.clear();
        for (int si = 0; si < seats.size(); si++) {
            seats.get(si).clearWordsOnly();
        }
        syncSeats();
        consecutivePasses = 0;
        doublePassCycles = 0;
        shakeAvailable = false;
        refreshBoardButtons();
        if (shakeButton != null) {
            boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                    && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }
        appendToLogPane(allLog, "The board was shaken up (letters changed; used words cleared).", "plain");
    }

    /**
     * Returns to the main menu from gameplay. Skips the confirm dialog when {@code alreadyConfirmed}.
     * @param alreadyConfirmed whether confirmation was already completed
     */
    public static void goToMenu(boolean alreadyConfirmed) {
        if (!alreadyConfirmed) {
            if (gamePaused) {
                return;
            }
            int pick = JOptionPane.showConfirmDialog(frame,
                    "Return to the main menu. An in-progress match is saved so you can resume later.",
                    "Menu",
                    JOptionPane.YES_NO_OPTION);
            if (pick != JOptionPane.YES_OPTION) {
                return;
            }
        }
        gamePaused = false;
        if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
            aiMoveTimer.stop();
        }
        aiMoveTimer = null;
        stopTurnTimer();
        try {
            BoggleSave.persistPrefs();
        } catch (RuntimeException ignored) {
        }
        if (BoggleAudio.bgClip != null) {
            BoggleAudio.bgClip.stop();
        }
        if (BoggleAudio.winningClip != null) {
            BoggleAudio.winningClip.stop();
        }
        if (frame != null) {
            frame.dispose();
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(new Dimension(1000, 740));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        cards.show(deck, "home");
        BoggleUI.refreshHomeResumeButton();
        BoggleAudio.startHomeMusic(userPrefs);
    }

    /**
     * Updates the gameplay feedback/status line.
     * @param primary feedback text
     * @param tone style tone key
     */
    public static void showFeedbackLine(String primary, String tone) {
        // Short message under the word box — green good, red bad, blue neutral pass, etc.
        if (playFeedbackLabel == null) {
            return;
        }
        Color fg = new Color(55, 65, 75);
        if ("SUCCESS".equals(tone)) {
            fg = new Color(0, 115, 55);
        } else if ("ERROR".equals(tone)) {
            fg = new Color(185, 35, 35);
        } else if ("NEUTRAL".equals(tone)) {
            fg = new Color(35, 95, 165);
        } else if ("WARN".equals(tone)) {
            fg = new Color(165, 105, 0);
        } else if ("HINT".equals(tone)) {
            fg = new Color(72, 84, 98);
        }
        int mainPx = 15;
        if ("HINT".equals(tone)) {
            mainPx = 14;
        }
        playFeedbackLabel.setForeground(fg);
        playFeedbackLabel.setFont(new Font("SansSerif", Font.BOLD, mainPx));
        String feedbackText = "";
        if (primary != null) {
            feedbackText = primary;
        }
        playFeedbackLabel.setText(feedbackText);
    }

    /**
     * Ensures expected named text styles exist on a log pane.
     * @param pane target log pane
     */
    public static void ensureLogStyles(JTextPane pane) {
        if (pane == null) {
            return;
        }
        // Style names match appendToLogPane: plain, score, bad, passLine, winGold.
        StyledDocument doc = pane.getStyledDocument();
        if (doc.getStyle("plain") != null) {
            return;
        }
        Style plain = doc.addStyle("plain", null);
        if (userPrefs != null && "DARK".equals(userPrefs.uiTheme)) {
            StyleConstants.setForeground(plain, new Color(210, 220, 232));
        } else {
            StyleConstants.setForeground(plain, new Color(55, 65, 75));
        }
        StyleConstants.setFontSize(plain, 15);

        Style score = doc.addStyle("score", null);
        StyleConstants.setForeground(score, new Color(0, 105, 55));
        StyleConstants.setBold(score, true);
        StyleConstants.setFontSize(score, 16);
        if (userPrefs == null || !"DARK".equals(userPrefs.uiTheme)) {
            StyleConstants.setBackground(score, new Color(210, 245, 225));
        }

        Style bad = doc.addStyle("bad", null);
        StyleConstants.setForeground(bad, new Color(145, 25, 25));
        StyleConstants.setBold(bad, true);
        StyleConstants.setFontSize(bad, 16);
        if (userPrefs == null || !"DARK".equals(userPrefs.uiTheme)) {
            StyleConstants.setBackground(bad, new Color(255, 228, 228));
        }

        Style passLine = doc.addStyle("passLine", null);
        StyleConstants.setForeground(passLine, new Color(25, 85, 145));
        StyleConstants.setBold(passLine, true);
        StyleConstants.setFontSize(passLine, 15);
        if (userPrefs == null || !"DARK".equals(userPrefs.uiTheme)) {
            StyleConstants.setBackground(passLine, new Color(230, 242, 255));
        }

        Style winGold = doc.addStyle("winGold", null);
        StyleConstants.setForeground(winGold, new Color(92, 62, 0));
        StyleConstants.setBold(winGold, true);
        StyleConstants.setFontSize(winGold, 16);
        if (userPrefs == null || !"DARK".equals(userPrefs.uiTheme)) {
            StyleConstants.setBackground(winGold, new Color(255, 236, 153));
        }
    }

    /**
     * Appends one styled line to a log pane.
     * @param pane target log pane
     * @param line text to append
     * @param styleName style key
     */
    public static void appendToLogPane(JTextPane pane, String line, String styleName) {
        // Always append at the end and scroll caret down so latest line is visible.
        if (pane == null) {
            return;
        }
        StyledDocument doc = pane.getStyledDocument();
        Style st = doc.getStyle(styleName);
        if (st == null) {
            st = doc.getStyle("plain");
        }
        try {
            doc.insertString(doc.getLength(), line + "\n", st);
        } catch (BadLocationException ignored) {
        }
        pane.setCaretPosition(doc.getLength());
    }

    /**
     * Clears all match and per-player logs.
     */
    public static void clearAllLogs() {
        if (allLog != null) {
            StyledDocument doc = allLog.getStyledDocument();
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException ignored) {
            }
        }
        if (playerLogs != null) {
            for (JTextPane ta : playerLogs) {
                if (ta == null) {
                    continue;
                }
                StyledDocument doc = ta.getStyledDocument();
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException ignored) {
                }
            }
        }
    }

    /**
     * Refreshes board button labels and visual state.
     */
    public static void refreshBoardButtons() {
        // Push board[][] letters onto the 25 JButton labels after shake or new round.
        if (cells == null) {
            return;
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                cells[r][c].setText(String.valueOf(board[r][c]));
            }
        }
        repaintTrace();
    }


    /**
     * Extends current trace by clicking a board cell.
     * @param row clicked row
     * @param col clicked column
     */
    public static void appendCellAsClick(int row, int col) {
        // Click mode (no drag): tap letters one at a time; same adjacency rules as dragging.
        if (wordField == null || !wordField.isEnabled()) {
            return;
        }
        for (int[] p : tracePath) {
            if (p[0] == row && p[1] == col) {
                showFeedbackLine("That letter is already used in this word — try Clear to start over.",
                        "WARN");
                return;
            }
        }
        if (!tracePath.isEmpty()) {
            int[] last = tracePath.get(tracePath.size() - 1);
            // Click path uses same 8-neighbor rule as drag.
            if (Math.abs(last[0] - row) > 1 || Math.abs(last[1] - col) > 1) {
                showFeedbackLine("Letters must be adjacent on the board (including diagonals).",
                        "WARN");
                return;
            }
        }
        tracePath.add(new int[] {row, col});
        if (wordField != null) {
            StringBuilder sb = new StringBuilder();
            for (int[] p : tracePath) {
                sb.append(Character.toLowerCase(board[p[0]][p[1]]));
            }
            wordField.setText(sb.toString());
        }
        repaintTrace();
    }

    
    /**
     * Repaints board traces and highlighted word paths.
     */
    public static void repaintTrace() {
        if (cells == null) {
            return;
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                JButton cell = cells[r][c];
                cell.setBackground(activePalette.tileBg);
                cell.setBorder(BorderFactory.createLineBorder(activePalette.line, 2));
            }
        }
        // Green path = last successfully played word (fades after 500 ms).
        Color scoredBorder = new Color(34, 130, 62);
        Color scoredFill = new Color(201, 232, 209);
        int k = 0;
        for (int[] p : lastScoredWordPath) {
            boolean head = k == lastScoredWordPath.size() - 1;
            JButton cell = cells[p[0]][p[1]];
            cell.setBackground(scoredFill);
            int scoredBorderWidth = 2;
            if (head) {
                scoredBorderWidth = 3;
            }
            cell.setBorder(BorderFactory.createLineBorder(scoredBorder, scoredBorderWidth));
            k++;
        }
        // Orange accent on current drag/click path; head cell is brightest.
        Color headBorder = new Color(234, 88, 12);
        for (int i = 0; i < tracePath.size(); i++) {
            int[] p = tracePath.get(i);
            boolean head = (i == tracePath.size() - 1);
            JButton cell = cells[p[0]][p[1]];
            Color traceBg = activePalette.traceLine;
            if (head) {
                traceBg = activePalette.traceHot;
            }
            cell.setBackground(traceBg);
            Color traceBorder = activePalette.accent;
            if (head) {
                traceBorder = headBorder;
            }
            cell.setBorder(BorderFactory.createLineBorder(traceBorder, 2));
        }
        if (boardGridPanel != null) {
            boardGridPanel.repaint();
        }
    }




    /**
     * Highlights the currently active player's score card.
     */
    public static void highlightCurrentPlayerBorder() {
        if (playerCards == null) {
            return;
        }
        for (int i = 0; i < playerCards.length && i < seatNames.size(); i++) {
            if (i == currentPlayerIndex) {  // thick border on active seat card
                playerCards[i].setBackground(activePalette.line);
                playerCards[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(activePalette.textStrong, 3),
                        BorderFactory.createEmptyBorder(11, 11, 11, 11)));
            } else {
                playerCards[i].setBackground(activePalette.panelBg);
                playerCards[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(activePalette.line),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            }
            for (Component ch : playerCards[i].getComponents()) {
                if (ch instanceof JLabel jl) {
                    if (scoreLabels != null && i < scoreLabels.length && jl == scoreLabels[i]) {
                        jl.setForeground(activePalette.textMuted);
                    } else {
                        jl.setForeground(activePalette.textStrong);
                    }
                }
            }
        }
        if (turnLabel == null) {
            return;
        }
        if (gameOver || seatNames.isEmpty()
                || currentPlayerIndex < 0 || currentPlayerIndex >= seatNames.size()) {
            turnLabel.setText(" ");
            return;
        }
        turnLabel.setText("Now playing: " + seatNames.get(currentPlayerIndex));
    }

    /**
     * Restarts the per-turn countdown timer.
     */
    public static void restartTurnTimer() {
        // Fresh countdown whenever a new human turn starts (timer off in prefs = skip).
        stopTurnTimer();
        if (gameOver || gamePaused || timerLabel == null) {
            return;
        }
        if (userPrefs.timerSeconds <= 0) {
            timerLabel.setText("Timer: Off");
            return;
        }

        secondsLeft = userPrefs.timerSeconds;
        timerLabel.setText("Timer: " + secondsLeft + "s");
        attachTurnCountdown();  // fresh countdown for new turn
    }

    
    /**
     * Attaches timer tick behavior to turn countdown state.
     */
    public static void attachTurnCountdown() {
        if (timerLabel == null || gameOver || gamePaused) {
            return;
        }
        if (userPrefs.timerSeconds <= 0) {
            timerLabel.setText("Timer: Off");
            return;
        }
        if (secondsLeft <= 0) {
            timerLabel.setText("Timer: 0s");
            return;
        }
        if (turnTimer != null && turnTimer.isRunning()) {
            return;
        }
        timerLabel.setText("Timer: " + secondsLeft + "s");  // do not stack multiple 1s timers
        turnTimer = new javax.swing.Timer(1000, e -> {
            if (gamePaused || gameOver) {
                return;
            }
            secondsLeft--;
            timerLabel.setText("Timer: " + secondsLeft + "s");
            if (secondsLeft <= 0) {
                stopTurnTimer();
                // Let AI finish its think/search; home timer must not steal the turn mid-think.
                if ((currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi())) {
                    return;
                }
                String timedOut = seatNames.get(currentPlayerIndex);
                appendToLogPane(allLog, timedOut + " ran out of time — auto pass.", "passLine");
                BoggleGameplay.passTurn();
                showFeedbackLine(timedOut + " ran out of time — auto pass.", "WARN");
                tracePath.clear();
                traceDragging = false;
                pressR = -1;
                pressC = -1;
                pressDragged = false;
                repaintTrace();
                for (int i = 0; i < scoreLabels.length && i < seatNames.size(); i++) {
                    scoreLabels[i].setText("Score: " + seatScores.get(i) + " / " + userPrefs.targetScore);
                }
                highlightCurrentPlayerBorder();
                if (shakeButton != null) {
                    boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                            && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
                    shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
                }
                enablePlayControls();
                if (!gameOver) {
                    restartTurnTimer();
                }
                runAiTurn();
            }
        });
        turnTimer.start();
    }

    /**
     * Stops and clears the turn countdown timer.
     */
    public static void stopTurnTimer() {
        if (turnTimer != null && turnTimer.isRunning()) {
            turnTimer.stop();
        }
        turnTimer = null;
        if (timerLabel != null) {
            if (userPrefs.timerSeconds <= 0) {
                timerLabel.setText("Timer: Off");
            } else {
                timerLabel.setText("Timer: paused");
            }
        }
    }

    /**
     * Enables gameplay input controls.
     */
    public static void enablePlayControls() {
        if (gamePaused) {
            return;
        }
        boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
        // Human turn only: AI keeps word field and buttons off until runAiTurn finishes.
        boolean allowHumanInput = !gameOver && !aiTurn;
        if (wordField != null) {
            wordField.setEnabled(allowHumanInput);
        }
        if (submitButton != null) {
            submitButton.setEnabled(allowHumanInput);
        }
        if (passButton != null) {
            passButton.setEnabled(allowHumanInput);
        }
        if (shakeButton != null) {
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }
        if (allowHumanInput && wordField != null) {
            SwingUtilities.invokeLater(() -> {
                if (wordField != null && wordField.isEnabled() && wordField.isDisplayable()) {
                    wordField.requestFocusInWindow();
                }
            });
        }
    }


    /**
     * Pauses gameplay and freezes timers/inputs.
     */
    public static void pauseGameMatch() {
        if (gameOver) {
            return;
        }
        if (!gamePaused) {
            gamePaused = true;
            if (aiMoveTimer != null && aiMoveTimer.isRunning()) {
                aiMoveTimer.stop();
            }
            aiMoveTimer = null;
            if (turnTimer != null && turnTimer.isRunning()) {
                turnTimer.stop();
            }
            turnTimer = null;
            // Keep remaining seconds/AI wait values; pause should freeze, not reset.
        }

        if (timerLabel != null) {
            if (userPrefs.timerSeconds <= 0) {
                timerLabel.setText("Timer: Off");
            } else {
                timerLabel.setText("Timer: " + secondsLeft + "s");
            }
        }
        if (wordField != null) {
            wordField.setEnabled(false);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
        }
        if (passButton != null) {
            passButton.setEnabled(false);
        }
        if (shakeButton != null) {
            shakeButton.setEnabled(false);
        }
        if (restartButton != null) {
            restartButton.setEnabled(false);
        }
        if (clearWordsButton != null) {
            clearWordsButton.setEnabled(false);
        }
        if (wordsHintButton != null) {
            wordsHintButton.setEnabled(false);
        }
        if (gameHelpBtn != null) {
            gameHelpBtn.setEnabled(false);
        }
        if (gameMenuBtn != null) {
            gameMenuBtn.setEnabled(false);
        }
        if (saveQuitButton != null) {
            saveQuitButton.setEnabled(false);
        }
        if (quitWithoutSaveButton != null) {
            quitWithoutSaveButton.setEnabled(false);
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (cells[r][c] != null) {
                    cells[r][c].setEnabled(false);
                }
            }
        }
    }

    /**
     * Resumes gameplay after a pause.
     */
    public static void resumeGameMatch() {
        if (!gamePaused) {
            return;
        }
        gamePaused = false;
        // Re-enable board cells first, then per-turn controls below.
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (cells[r][c] != null) {
                    cells[r][c].setEnabled(true);
                }
            }
        }
        enablePlayControls();
        if (shakeButton != null) {
            boolean aiTurn = !seatNames.isEmpty() && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                    && (currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
            shakeButton.setEnabled(shakeAvailable && !gameOver && !aiTurn);
        }
        if (restartButton != null) {
            restartButton.setEnabled(!gameOver);
        }
        boolean allowHuman = !gameOver && currentPlayerIndex >= 0 && currentPlayerIndex < seatNames.size()
                && !(currentPlayerIndex >= 0 && currentPlayerIndex < seats.size() && seats.get(currentPlayerIndex).isAi());
        if (clearWordsButton != null) {
            clearWordsButton.setEnabled(allowHuman);
        }
        if (gameMenuBtn != null) {
            gameMenuBtn.setEnabled(!gameOver);
        }
        if (wordsHintButton != null) {
            wordsHintButton.setEnabled(!gameOver && userPrefs.allowBoardHint);
        }
        if (gameHelpBtn != null) {
            gameHelpBtn.setEnabled(!gameOver);
        }
        if (saveQuitButton != null) {
            saveQuitButton.setEnabled(!gameOver);
        }
        if (quitWithoutSaveButton != null) {
            quitWithoutSaveButton.setEnabled(!gameOver);
        }
        if (!gameOver && userPrefs.timerSeconds > 0 && secondsLeft > 0) {
            attachTurnCountdown();
        } else if (timerLabel != null && userPrefs.timerSeconds > 0) {
            timerLabel.setText("Timer: " + secondsLeft + "s");
        }
        runAiTurn();
    }

}
