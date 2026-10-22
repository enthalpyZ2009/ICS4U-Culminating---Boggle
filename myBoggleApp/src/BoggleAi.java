import java.util.ArrayList;

/**
 * Strategy base class for AI word selection by difficulty.
 */
public abstract class BoggleAi {
    private static final BoggleAi EASY = new EasyAi();
    private static final BoggleAi MEDIUM = new MediumAi();
    private static final BoggleAi HARD = new HardAi();
    private static final BoggleAi IMPOSSIBLE = new ImpossibleAi();

    /**
     * Returns the singleton AI strategy for a difficulty label.
     * @param difficulty difficulty name
     * @return matching AI strategy (defaults to easy)
     */
    public static BoggleAi forDifficulty(String difficulty) {
        if ("Medium".equals(difficulty)) {
            return MEDIUM;
        }
        if ("Hard".equals(difficulty)) {
            return HARD;
        }
        if ("Impossible".equals(difficulty)) {
            return IMPOSSIBLE;
        }
        return EASY;
    }

    /**
     * Chooses one playable word (or null to pass).
     * @param playable words currently playable on the board
     * @param minWordLength minimum allowed word length
     * @return chosen word, or null to pass
     */
    public abstract String chooseWord(ArrayList<String> playable, int minWordLength);
}

/**
 * Easiest AI profile: often passes and prefers short words.
 */
final class EasyAi extends BoggleAi {
    /** {@inheritDoc} */
    public String chooseWord(ArrayList<String> playable, int minWordLength) {
        if (playable.isEmpty() || (int) (Math.random() * 100) < 65) {
            return null;
        }
        if ((int) (Math.random() * 100) < 20) {
            String shortest = playable.get(0);
            for (String word : playable) {
                if (word.length() < shortest.length()) {
                    shortest = word;
                }
            }
            return shortest;
        }
        int bucket = (int) (Math.random() * 100);
        int targetLen;
        if (bucket < 80) {
            targetLen = minWordLength;
        } else if (bucket < 95) {
            targetLen = minWordLength + 1;
        } else {
            targetLen = minWordLength + 2;
        }
        for (String word : playable) {
            if (word.length() == targetLen) {
                return word;
            }
        }
        for (int len = targetLen + 1; len <= BoggleBoard.BOARD_SIZE * BoggleBoard.BOARD_SIZE; len++) {
            for (String word : playable) {
                if (word.length() == len) {
                    return word;
                }
            }
        }
        for (int len = targetLen - 1; len >= minWordLength; len--) {
            for (String word : playable) {
                if (word.length() == len) {
                    return word;
                }
            }
        }
        return playable.get(0);
    }
}

/**
 * Medium AI profile: targets short-to-mid length words.
 */
final class MediumAi extends BoggleAi {
    /** {@inheritDoc} */
    public String chooseWord(ArrayList<String> playable, int minWordLength) {
        if (playable.isEmpty() || (int) (Math.random() * 100) < 25) {
            return null;
        }
        for (String word : playable) {
            int len = word.length();
            if (len >= minWordLength + 1 && len <= minWordLength + 3) {
                return word;
            }
        }
        for (String word : playable) {
            if (word.length() == minWordLength) {
                return word;
            }
        }
        return playable.get(0);
    }
}

/**
 * Hard AI profile: usually plays and biases longer words.
 */
final class HardAi extends BoggleAi {
    /** {@inheritDoc} */
    public String chooseWord(ArrayList<String> playable, int minWordLength) {
        if (playable.isEmpty() || (int) (Math.random() * 100) < 10) {
            return null;
        }
        if ((int) (Math.random() * 100) < 25) {
            int longest = playable.get(0).length();
            for (String word : playable) {
                if (word.length() < longest) {
                    return word;
                }
            }
        }
        return playable.get(0);
    }
}

/**
 * Impossible AI profile: always picks the top available word.
 */
final class ImpossibleAi extends BoggleAi {
    /** {@inheritDoc} */
    public String chooseWord(ArrayList<String> playable, int minWordLength) {
        if (playable.isEmpty()) {
            return null;
        }
        return playable.get(0);
    }
}
