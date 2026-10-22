import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Central audio utility for music and sound effects playback.
 */
public class BoggleAudio {
    public static final String[] SCORE_MUSIC = {
            "/resources/music/score_c.wav",
            "/resources/music/score_c_sharp.wav",
            "/resources/music/score_d.wav",
            "/resources/music/score_d_sharp.wav",
            "/resources/music/score_e.wav",
            "/resources/music/score_f.wav",
    };

    public static Clip homePageClip;
    public static Clip bgClip;
    public static Clip rejectClip;
    public static Clip winningClip;

    public static final Clip[] scoreClips = new Clip[SCORE_MUSIC.length];

    /**
     * Loads an audio clip from a classpath resource path.
     * @param path resource path
     * @return loaded clip, or {@code null} when unavailable
     */
    public static Clip loadClip(String path) {
        try {
            Clip clip = AudioSystem.getClip();
            String resourcePath = path;
            if (path.startsWith("/")) {
                resourcePath = path.substring(1);
            }
            java.net.URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
            if (resourceUrl == null) {
                return null;
            }
            clip.open(AudioSystem.getAudioInputStream(resourceUrl));
            return clip;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Applies a percentage volume to a clip.
     * @param clip target clip
     * @param volumePct volume percentage in [0, 100]
     */
    public static void setVolume(Clip clip, int volumePct) {
        if (clip == null) {
            return;
        }
        float vol = volumePct;
        if (vol < 0f) {
            vol = 0f;
        }
        if (vol > 100f) {
            vol = 100f;
        }
        float t = vol / 100f;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                fc.setValue(fc.getMinimum() + t * (fc.getMaximum() - fc.getMinimum()));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Reapplies current user volume settings to all cached clips.
     * @param userPrefs current user preferences
     */
    public static void refreshVolumes(UserPreferences userPrefs) {
        int music = userPrefs.musicVolumePct;
        int sfx = userPrefs.sfxVolumePct;
        if (homePageClip != null) {
            setVolume(homePageClip, music);
        }
        if (bgClip != null) {
            setVolume(bgClip, music);
        }
        if (winningClip != null) {
            setVolume(winningClip, music);
        }
        if (rejectClip != null) {
            setVolume(rejectClip, sfx);
        }
        for (int i = 0; i < scoreClips.length; i++) {
            setVolume(scoreClips[i], sfx);
        }
    }

    /**
     * Plays score SFX chosen by awarded point count.
     * @param points points earned by a word
     * @param userPrefs current user preferences
     */
    public static void playScoreSFX(int points, UserPreferences userPrefs) {
        int index = points - userPrefs.minWordLength;
        if (index < 0) {
            index = 0;
        }
        if (index >= SCORE_MUSIC.length) {
            index = SCORE_MUSIC.length - 1;
        }
        Clip clip = scoreClips[index];
        if (clip == null) {
            clip = loadClip(SCORE_MUSIC[index]);
            scoreClips[index] = clip;
        }
        if (clip == null) {
            return;
        }
        setVolume(clip, userPrefs.sfxVolumePct);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Starts looping home-screen music.
     * @param userPrefs current user preferences
     */
    public static void startHomeMusic(UserPreferences userPrefs) {
        if (homePageClip == null) {
            homePageClip = loadClip("/resources/music/home_ui.wav");
        }
        if (homePageClip == null) {
            return;
        }
        setVolume(homePageClip, userPrefs.musicVolumePct);
        if (homePageClip.isActive()) {
            return;
        }
        homePageClip.stop();
        homePageClip.setFramePosition(0);
        homePageClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Starts looping in-match background music.
     * @param userPrefs current user preferences
     */
    public static void startBackgroundMusic(UserPreferences userPrefs) {
        if (homePageClip != null) {
            homePageClip.stop();
        }
        if (bgClip == null) {
            bgClip = loadClip("/resources/music/background.wav");
        }
        if (bgClip == null) {
            return;
        }
        setVolume(bgClip, userPrefs.musicVolumePct);
        bgClip.stop();
        bgClip.setFramePosition(0);
        bgClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Plays the invalid-word sound effect once.
     * @param userPrefs current user preferences
     */
    public static void playReject(UserPreferences userPrefs) {
        if (!userPrefs.sfxEnabled || userPrefs.sfxVolumePct <= 0) {
            return;
        }
        if (rejectClip == null) {
            rejectClip = loadClip("/resources/music/reject.wav");
        }
        if (rejectClip == null) {
            return;
        }
        setVolume(rejectClip, userPrefs.sfxVolumePct);
        rejectClip.stop();
        rejectClip.setFramePosition(0);
        rejectClip.start();
    }

    /**
     * Plays the win music and stops other music tracks.
     * @param userPrefs current user preferences
     */
    public static void playWinningMusic(UserPreferences userPrefs) {
        if (homePageClip != null) {
            homePageClip.stop();
        }
        if (bgClip != null) {
            bgClip.stop();
        }
        if (winningClip == null) {
            winningClip = loadClip("/resources/music/winning.wav");
        }
        if (winningClip == null) {
            return;
        }
        setVolume(winningClip, userPrefs.musicVolumePct);
        winningClip.stop();
        winningClip.setFramePosition(0);
        winningClip.start();
    }
}
