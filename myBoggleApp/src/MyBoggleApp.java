/**
 * Name: Angela Zhang, Milia Guo
 * Date: May 20, 2026
 * Description: Desktop Boggle application built with Java Swing. Players enter words by clicking or
 * dragging across adjacent letters on a 5x5 board; words must appear in the bundled dictionary and
 * follow valid paths. Supports player vs player and multiplayer modes with an optional AI seat
 * (Easy, Medium, Hard), configurable rules (minimum word length, target score, round count, per-turn
 * timer), board shake, and an optional hint list of remaining playable words (longest first). Match
 * state, scores, logs, and guest preferences persist under src/resources/saves so games can be
 * resumed after quit or restart. Includes themed UI (ocean, healing, dark), background music and SFX,
 * styled match logs, round celebrations, final standings, and save-and-quit / return-to-menu flows.
 */


import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


/**
 * Main application and entry points for the Boggle app.
 */
public class MyBoggleApp {
    public static final BoggleBoard boardModel = new BoggleBoard();
    public static final int BOARD_SIZE = BoggleBoard.BOARD_SIZE;

    public static boolean dragAutoSubmit;

    public static BoggleTheme.Palette activePalette = BoggleTheme.activePalette;

    public static final ArrayList<JPanel> settingBlocks = new ArrayList<>();

    public static JPanel homeHeaderPanel;
    public static JLabel homeTitleLabel;
    public static JLabel homeSubtitleLabel;
    public static JLabel homeUserLabel;
    public static JPanel homeBodyPanel;
    public static JButton homeStartBtn;
    public static JButton homeSettingsBtn;
    public static JButton homeHelpBtn;
    public static JPanel panel;
    public static JLabel settingsTitleLabel;
    public static JLabel settingsSubtitleLabel;
    public static JTabbedPane settingsTabs;
    public static JPanel settingsSouth;
    public static JPanel lobbyRoot;
    public static JPanel lobbyTop;
    public static JPanel lobbyPlayersCard;
    public static JPanel gameLivePanel;
    public static JPanel gameBoardWrap;
    public static JLabel wordPrompt;
    public static JComboBox<String> settingsThemeCombo;
    public static JPanel settingsThemeTab;
    public static JPanel settingsThemeCard;
    public static JPanel settingsSoundTab;
    public static JPanel settingsSoundCard;
    public static JSlider settingsMusicSlider;
    public static JSlider settingsSfxSlider;
    public static JCheckBox settingsSfxOnCheck;
    public static JScrollPane settingsGameplayScroll;
    public static JPanel settingsGameplayColumn;
    public static JButton settingsBackBtn;
    protected static boolean setBulk;
    public static JPanel homeQuickPanel;
    public static JButton lobbyStartBtn;
    public static JButton lobbyBackBtn;
    public static JPanel gameNorthPanel;
    public static JPanel gameWestWrap;
    public static JPanel gameEastPanel;
    public static JPanel gameSouthPanel;
    public static JPanel gameSaveBlock;
    public static JButton gameMenuBtn;
    public static JButton gameHelpBtn;


    public static final ArrayList<String> DICTIONARY = boardModel.words();

    protected static char[][] pendingContestBoard = null;

    public static JFrame frame;
    public static CardLayout cards;
    public static JPanel deck;
    public static JPanel gameMount;

    public static String guestName = "Player";

    // Live match data — everything indexed by seat (player) number.
    public static char[][] board = boardModel.grid();
    public static final ArrayList<String> seatNames = new ArrayList<>();
    public static final ArrayList<Boolean> seatIsAi = new ArrayList<>();
    public static final ArrayList<PlayerSeat> seats = new ArrayList<>();
    public static int aiSeatIndex = -1;  // first AI seat index, or -1 if none

    public static final ArrayList<Integer> seatScores = new ArrayList<>();
    public static final ArrayList<ArrayList<String>> seatWordLists = new ArrayList<>();
    public static final ArrayList<Integer> seatPasses = new ArrayList<>();
    public static final ArrayList<Integer> seatRoundWins = new ArrayList<>();
    public static int currentPlayerIndex;
    public static final HashSet<String> usedWords = new HashSet<>();  // already played this round/board
    public static boolean gameOver;
    
    public static boolean gamePaused;
    public static int winnerSeat = -1;
    public static int consecutivePasses;   // passes in a row toward "everyone passed"
    public static int doublePassCycles;    // how many full pass rounds happened (shake unlocks at 2)
    public static boolean shakeAvailable;

    public static JLabel timerLabel;
    public static JLabel turnLabel;
    public static JTextField wordField;
    public static JButton shakeButton;
    public static JButton submitButton;
    public static JButton passButton;
    public static JButton clearWordsButton;
    public static JButton saveQuitButton;
    
    public static JButton quitWithoutSaveButton;
    public static JButton restartButton;
    public static JButton wordsHintButton;
    
    public static JButton homeResumeButton;

    
    public static final ArrayList<int[]> tracePath = new ArrayList<>();

    
    public static final ArrayList<int[]> lastScoredWordPath = new ArrayList<>();
    public static javax.swing.Timer lastScoredPathTimer;
    
    // Board input: click builds path via appendCellAsClick; drag uses tracePath + press* state.
    public static boolean traceDragging = false;
    public static int pressR = -1;
    public static int pressC = -1;
    public static boolean pressDragged = false;
    
    public static JLabel playFeedbackLabel;
    public static JButton[][] cells;
    
    public static JPanel boardGridPanel;
    public static JPanel[] playerCards;
    public static JLabel[] scoreLabels;
    public static JTextPane allLog;
    public static JTextPane[] playerLogs;
    protected static String pendingMatchLogBody = "";
    protected static String[] pendingPlayerLogBodies;
    
    public static javax.swing.Timer turnTimer;
    public static int secondsLeft;

    public static JComboBox<String> homeModeBox;
    public static JComboBox<String> homeTimeBox;
    public static JTextField homeCustomTimeField;
    public static JButton homeSubmitTimeBtn;
    public static JLabel homeTimeErrorLabel;
    public static boolean homeTimeLocked;
    public static JComboBox<String> homeAIDifficultyBox;
    public static JComboBox<String> homeMinWordBox;
    public static JTextField homeCustomMinWordField;
    public static JButton homeSubmitMinWordBtn;
    public static JLabel homeMinWordErrorLabel;
    public static boolean homeMinWordLocked;
    public static JComboBox<String> homeTargetBox;
    public static JTextField homeCustomTargetField;
    public static JButton homeSubmitTargetBtn;
    public static JLabel homeTargetErrorLabel;
    public static boolean homeTargetLocked;
    public static JComboBox<String> homeRoundsBox;
    public static JTextField homeCustomRoundsField;
    public static JButton homeSubmitRoundsBtn;
    public static JLabel homeRoundsErrorLabel;
    public static boolean homeRoundsLocked;

    
    public static UserPreferences userPrefs = new UserPreferences();

    public static JComboBox<String> setGpModeBox;
    public static JComboBox<String> setGpTimeBox;
    public static JTextField setGpCustomTimeField;
    public static JButton setGpSubmitTimeBtn;
    public static JLabel setGpTimeErrLabel;
    public static boolean setGpTimeLocked;
    public static JComboBox<String> setGpAiDifficultyBox;
    public static JComboBox<String> setGpMinWordBox;
    public static JTextField setGpCustomMinWordField;
    public static JButton setGpSubmitMinWordBtn;
    public static JLabel setGpMinWordErrLabel;
    public static boolean setGpMinWordLocked;
    public static JComboBox<String> setGpTargetBox;
    public static JTextField setGpCustomTargetField;
    public static JButton setGpSubmitTargetBtn;
    public static JLabel setGpTargetErrLabel;
    public static boolean setGpTargetLocked;
    public static JComboBox<String> setGpRoundsBox;
    public static JTextField setGpCustomRoundsField;
    public static JButton setGpSubmitRoundsBtn;
    public static JLabel setGpRoundsErrLabel;
    public static boolean setGpRoundsLocked;
    public static JComboBox<String> setGpAiThinkDelayBox;
    public static JCheckBox setGpAllowHintCheckBox;
    public static JButton setGpRestoreDefaultButton;
    public static JButton setGpSaveDefaultButton;
    public static JButton setGpBuiltInFactoryButton;


    public static int currentRoundNumber = 1;

    public static JComboBox<String> lobbyFirstTurnBox;
    public static JComboBox<String> lobbyPlayerCountBox;
    public static JTextField lobbyCustomPlayerCountField;
    public static JButton lobbySubmitPlayerCountBtn;
    public static JLabel lobbyPlayerCountErrorLabel;
    public static boolean lobbyPlayerCountLocked;
    public static JPanel lobbyNamesPanel;
    public static final ArrayList<JTextField> lobbyNameFields = new ArrayList<>();
    public static JComboBox<String> lobbyTurnOrderBox;
    public static int pendingStartIndex;

    public static String selectedHomeMode = "Player vs Player";

    
    public static String activeAiDifficulty = "Easy";

    // AI think delay: one repeating timer ticks aiWaitMs down; pause/save stores the remainder there.
    protected static javax.swing.Timer aiMoveTimer;
    protected static int aiWaitMs;
    // Bumped when a new AI move starts; stale background threads compare against this before acting on EDT.
    protected static int aiMoveSerial;

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        // Name picks the save folder — each guest gets their own prefs and resume file.
        guestName = JOptionPane.showInputDialog(null,
            "Enter your name:",
            "myBoggle App",
            JOptionPane.PLAIN_MESSAGE);
        if (guestName == null || guestName.trim().isEmpty()) {
            System.exit(0);
            return;
        }
        guestName = guestName.trim();
        userPrefs = new UserPreferences();
        userPrefs.load(guestName);
        activePalette = BoggleTheme.select(userPrefs.uiTheme);
        BoggleUI.showWelcomeRulesDialog();
        BoggleUI.mainWindow();
        // Brand-new profile: factory rules; returning guest: push saved combos into widgets.
        if (!new File(BoggleSave.guestSaveGameDir(guestName), "save.txt").isFile()
                && !UserPreferences.hasSavedQuickMode(guestName)) {
            BoggleUI.resetToFactoryGameplay();
        } else if (homeModeBox != null) {
            userPrefs.applyToHomeUi();
            userPrefs.applyToSettingsUi();
        }
        frame.setVisible(true);
        boardModel.loadDictionary();
    }
}