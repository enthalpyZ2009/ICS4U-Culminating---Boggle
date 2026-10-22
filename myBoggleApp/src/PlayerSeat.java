import java.util.ArrayList;
import java.util.Collection;

/**
 * Mutable score/pass/word state for one player or AI seat.
 */
public class PlayerSeat {
    private String name;
    private boolean ai;
    private int score;
    private final ArrayList<String> words = new ArrayList<>();
    private int passCount;
    private int roundsWon;

    /**
     * Creates a seat state holder.
     * @param name display name
     * @param isAi whether this seat is AI-controlled
     */
    public PlayerSeat(String name, boolean isAi) {
        this.name = name;
        this.ai = isAi;
    }

    /**
     * Gets the seat display name.
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Reports whether this seat is AI-controlled.
     * @return {@code true} when AI-controlled
     */
    public boolean isAi() {
        return ai;
    }

    /**
     * Gets the current round score.
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets words scored in the current round.
     * @return mutable words list
     */
    public ArrayList<String> getWords() {
        return words;
    }

    /**
     * Gets number of passes in the current round.
     * @return pass count
     */
    public int getPassCount() {
        return passCount;
    }

    /**
     * Gets number of rounds won in the match.
     * @return round-win count
     */
    public int getRoundsWon() {
        return roundsWon;
    }

    /**
     * Adds a scored word and updates score.
     * @param word accepted word
     * @return updated score
     */
    public int scoreWord(String word) {
        words.add(word);
        score += word.length();
        return score;
    }

    /**
     * Increments pass count for this round.
     */
    public void passTurn() {
        passCount++;
    }

    /**
     * Increments total rounds won.
     */
    public void winRound() {
        roundsWon++;
    }

    /**
     * Resets seat state for a new match.
     */
    public void resetForMatch() {
        score = 0;
        words.clear();
        passCount = 0;
        roundsWon = 0;
    }

    /**
     * Resets seat state for a new round.
     */
    public void resetForRound() {
        score = 0;
        words.clear();
        passCount = 0;
    }

    /**
     * Clears only the round word list.
     */
    public void clearWordsOnly() {
        words.clear();
    }

    /**
     * Restores seat state from persisted values.
     * @param score restored score
     * @param passCount restored pass count
     * @param roundsWon restored rounds-won count
     * @param savedWords restored words
     */
    public void setSavedState(int score, int passCount, int roundsWon, Collection<String> savedWords) {
        this.score = score;
        this.passCount = passCount;
        this.roundsWon = roundsWon;
        words.clear();
        if (savedWords != null) {
            words.addAll(savedWords);
        }
    }
}
