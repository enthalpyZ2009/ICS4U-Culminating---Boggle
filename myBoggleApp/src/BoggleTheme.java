import java.awt.Color;

/**
 * Defines named UI color palettes and theme selection logic.
 */
public class BoggleTheme {

    /**
     * Immutable color set consumed by UI components.
     */
    public static final class Palette {
        public final Color textStrong;
        public final Color accent;
        public final Color appBg;
        public final Color panelBg;
        public final Color line;
        public final Color action;
        public final Color textMuted;
        public final Color traceLine;
        public final Color traceHot;
        public final Color headerBg;
        public final Color headerFgMuted;
        public final Color boardFrame;
        public final Color tileBg;
        public final Color formBg;
        public final Color scrollTrackBg;
        public final Color textQuiet;
        public final Color footerRule;
        public final Color buttonMuted;
        public final Color bannerBg;
        public final Color homeOutline;
        public final Color homeConfirm;
        public final Color homeSoft;
        public final Color chipNeutral;
        public final Color chromeUi;
        public final Color chromeDim;
        public final Color chromeBright;
        public final Color textPlain;
        public final Color inputBg;
        public final Color editorBg;
        public final Color listBg;
        public final Color listHiBg;
        public final Color listHiFg;

        /**
         * Creates an immutable theme palette.
         * @param textStrong strong foreground text color
         * @param accent accent color
         * @param appBg application background color
         * @param panelBg panel background color
         * @param line border/separator color
         * @param action action color
         * @param textMuted muted foreground text color
         * @param traceLine board trace line color
         * @param traceHot active trace highlight color
         * @param headerBg header background color
         * @param headerFgMuted muted header text color
         * @param boardFrame board frame color
         * @param tileBg tile background color
         * @param formBg form background color
         * @param scrollTrackBg scroll track background color
         * @param textQuiet quiet foreground text color
         * @param footerRule footer separator color
         * @param buttonMuted muted button color
         * @param bannerBg banner background color
         * @param homeOutline home button outline color
         * @param homeConfirm confirm color
         * @param homeSoft soft neutral color
         * @param chipNeutral neutral chip color
         * @param chromeUi chrome UI background color
         * @param chromeDim dim chrome color
         * @param chromeBright bright chrome color
         * @param textPlain plain text color
         * @param inputBg input background color
         * @param editorBg editor background color
         * @param listBg list background color
         * @param listHiBg list selection background color
         * @param listHiFg list selection foreground color
         */
        public Palette(
                Color textStrong, Color accent, Color appBg, Color panelBg,
                Color line, Color action, Color textMuted, Color traceLine,
                Color traceHot, Color headerBg, Color headerFgMuted, Color boardFrame,
                Color tileBg, Color formBg, Color scrollTrackBg, Color textQuiet,
                Color footerRule, Color buttonMuted, Color bannerBg, Color homeOutline,
                Color homeConfirm, Color homeSoft, Color chipNeutral, Color chromeUi,
                Color chromeDim, Color chromeBright, Color textPlain, Color inputBg,
                Color editorBg, Color listBg, Color listHiBg, Color listHiFg) {
            this.textStrong = textStrong;
            this.accent = accent;
            this.appBg = appBg;
            this.panelBg = panelBg;
            this.line = line;
            this.action = action;
            this.textMuted = textMuted;
            this.traceLine = traceLine;
            this.traceHot = traceHot;
            this.headerBg = headerBg;
            this.headerFgMuted = headerFgMuted;
            this.boardFrame = boardFrame;
            this.tileBg = tileBg;
            this.formBg = formBg;
            this.scrollTrackBg = scrollTrackBg;
            this.textQuiet = textQuiet;
            this.footerRule = footerRule;
            this.buttonMuted = buttonMuted;
            this.bannerBg = bannerBg;
            this.homeOutline = homeOutline;
            this.homeConfirm = homeConfirm;
            this.homeSoft = homeSoft;
            this.chipNeutral = chipNeutral;
            this.chromeUi = chromeUi;
            this.chromeDim = chromeDim;
            this.chromeBright = chromeBright;
            this.textPlain = textPlain;
            this.inputBg = inputBg;
            this.editorBg = editorBg;
            this.listBg = listBg;
            this.listHiBg = listHiBg;
            this.listHiFg = listHiFg;
        }
    }

    public static final Palette OCEAN = new Palette(
            new Color(12, 74, 110), new Color(56, 189, 248), new Color(245, 252, 255),
            new Color(236, 251, 255), new Color(186, 230, 253), new Color(255, 125, 94),
            new Color(120, 130, 140), new Color(180, 220, 255), new Color(255, 200, 120),
            new Color(74, 134, 184), new Color(215, 225, 235), new Color(228, 243, 252),
            Color.WHITE, new Color(236, 244, 252), new Color(236, 242, 250),
            new Color(85, 105, 130), new Color(210, 220, 232), new Color(118, 152, 188),
            new Color(248, 251, 255), new Color(154, 188, 208), new Color(82, 159, 122),
            new Color(110, 110, 110), new Color(74, 134, 184), new Color(245, 252, 255),
            new Color(220, 240, 255), new Color(245, 252, 255), Color.DARK_GRAY,
            new Color(245, 252, 255), Color.WHITE, new Color(220, 240, 255),
            new Color(170, 215, 255), Color.DARK_GRAY);

    public static final Palette HEALING = new Palette(
            new Color(45, 85, 72), new Color(120, 175, 160), new Color(248, 252, 245),
            new Color(238, 248, 240), new Color(200, 228, 210), new Color(232, 150, 140),
            new Color(95, 110, 100), new Color(200, 230, 215), new Color(240, 200, 140),
            new Color(78, 130, 110), new Color(230, 242, 235), new Color(225, 240, 232),
            Color.WHITE, new Color(236, 244, 239), new Color(232, 242, 236),
            new Color(95, 110, 100), new Color(210, 225, 215), new Color(118, 150, 135),
            new Color(248, 251, 248), new Color(140, 175, 155), new Color(95, 145, 115),
            new Color(100, 110, 105), new Color(78, 130, 110), new Color(248, 252, 245),
            new Color(220, 238, 228), new Color(250, 252, 250), Color.DARK_GRAY,
            new Color(250, 252, 250), Color.WHITE, new Color(220, 238, 228),
            new Color(170, 215, 190), Color.DARK_GRAY);

    public static final Palette DARK_PALETTE = new Palette(
            new Color(228, 234, 244), new Color(110, 190, 255), new Color(34, 36, 42),
            new Color(46, 50, 58), new Color(82, 90, 104), new Color(255, 140, 110),
            new Color(200, 208, 220), new Color(62, 82, 108), new Color(220, 170, 110),
            new Color(48, 54, 68), new Color(200, 208, 220), new Color(54, 60, 72),
            new Color(58, 64, 76), new Color(38, 42, 50), new Color(36, 40, 48),
            new Color(172, 182, 198), new Color(68, 76, 90), new Color(108, 128, 162),
            new Color(48, 54, 66), new Color(92, 112, 142), new Color(78, 168, 118),
            new Color(108, 116, 128), new Color(62, 82, 112), new Color(34, 36, 42),
            new Color(50, 56, 68), new Color(46, 50, 58), new Color(228, 232, 242),
            new Color(50, 56, 66), new Color(42, 46, 56), new Color(56, 62, 74),
            new Color(88, 118, 158), new Color(228, 232, 242));

    public static Palette activePalette = OCEAN;

    /**
     * Activates a palette based on a theme key.
     * @param themeName selected theme key
     * @return active palette
     */
    public static Palette select(String themeName) {
        if ("HEALING_SOFT".equals(themeName)) {
            activePalette = HEALING;
        } else if ("DARK".equals(themeName)) {
            activePalette = DARK_PALETTE;
        } else {
            activePalette = OCEAN;
        }
        return activePalette;
    }
}
