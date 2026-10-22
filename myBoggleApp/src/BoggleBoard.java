import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * Owns board letters, dictionary lookup, and word-search traversal logic.
 */
public class BoggleBoard {
    public static final int BOARD_SIZE = 5;
    public static final String[] DICE = {
            "AAAFRS", "AAEEEE", "AAFIRS", "ADENNN", "AEEEEM",
            "AEEGMU", "AEGMNN", "AFIRSY", "BJKQXZ", "CCNSTW",
            "CEIILT", "CEILPT", "CEIPST", "DDLNOR", "DHHLOR",
            "DHHNOT", "DHLNOR", "EIIITT", "EMOTTT", "ENSSSU",
            "FIPRSY", "GORRVW", "HIPRRY", "NOOTUW", "OOOTTU"
    };

    private char[][] grid = new char[BOARD_SIZE][BOARD_SIZE];
    private final ArrayList<String> dictionary = new ArrayList<>();

    /**
     * Exposes the loaded dictionary list.
     * @return dictionary words
     */
    public ArrayList<String> words() {
        return dictionary;
    }

    /**
     * Returns the current board grid.
     * @return board letters
     */
    public char[][] grid() {
        return grid;
    }

    /**
     * Replaces the active board grid reference.
     * @param grid board letters
     */
    public void useGrid(char[][] grid) {
        this.grid = grid;
    }

    /**
     * Loads dictionary words from packaged resources.
     */
    public void loadDictionary() {
        dictionary.clear();
        String base = System.getProperty("user.dir");
        String[] candidates = {
            base + "/src/resources/wordlist.txt",
            base + "/boggle-java-gui/src/resources/wordlist.txt",
        };
        File file = null;
        for (String path : candidates) {
            File f = new File(path);
            if (f.isFile()) {
                file = f;
                break;
            }
        }
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String w = line.trim().toLowerCase();
                    if (w.length() > 0) {
                        boolean lettersOnly = true;
                        for (int i = 0; i < w.length(); i++) {
                            if (!Character.isLetter(w.charAt(i))) {
                                lettersOnly = false;
                                break;
                            }
                        }
                        if (lettersOnly) {
                            dictionary.add(w);
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Shuffles dice and generates a random board.
     */
    public void generateBoggleBoard() {
        ArrayList<String> diceList = new ArrayList<>();
        for (String d : DICE) {
            diceList.add(d);
        }
        for (int i = diceList.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            String t = diceList.get(i);
            diceList.set(i, diceList.get(j));
            diceList.set(j, t);
        }
        int index = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String die = diceList.get(index++);
                grid[row][col] = die.charAt((int) (Math.random() * die.length()));
            }
        }
    }

    /**
     * Checks whether any dictionary word starts with a prefix.
     * @param prefix candidate prefix
     * @return {@code true} when a dictionary word can continue from it
     */
    public boolean canStartWord(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        int index = firstDictionaryIndexAtOrAfter(prefix);
        return index < dictionary.size() && dictionary.get(index).startsWith(prefix);
    }

    /**
     * Checks whether a full word exists in the dictionary.
     * @param word candidate word
     * @return {@code true} when the word exists
     */
    public boolean isKnownWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String target = word.toLowerCase();
        int index = firstDictionaryIndexAtOrAfter(target);
        return index < dictionary.size() && dictionary.get(index).equals(target);
    }

    /**
     * Finds the first dictionary index not less than text.
     * @param text lookup key
     * @return lower-bound insertion index
     */
    private int firstDictionaryIndexAtOrAfter(String text) {
        int low = 0;
        int high = dictionary.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (text.compareTo(dictionary.get(mid)) <= 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Checks whether a word can be formed on the board.
     * @param word candidate word
     * @return {@code true} when the board contains a valid path
     */
    public boolean isWordOnBoard(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String upper = word.toUpperCase(Locale.ROOT);
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
                if (searchFromCell(upper, 0, row, col, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Depth-first search from one cell for word existence.
     * @param word target word (uppercase expected)
     * @param index current character index
     * @param row current row
     * @param col current column
     * @param visited visit-state matrix
     * @return {@code true} when a full path is found
     */
    public boolean searchFromCell(String word, int index, int row, int col, boolean[][] visited) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }
        if (visited[row][col]) {
            return false;
        }
        if (grid[row][col] != word.charAt(index)) {
            return false;
        }
        if (index == word.length() - 1) {
            return true;
        }

        visited[row][col] = true;

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;
                }
                if (searchFromCell(word, index + 1, row + dRow, col + dCol, visited)) {
                    visited[row][col] = false;
                    return true;
                }
            }
        }

        visited[row][col] = false;
        return false;
    }

    /**
     * Finds one path of coordinates for a playable word.
     * @param word candidate word
     * @return ordered path, or {@code null} when absent
     */
    public ArrayList<int[]> findBoardPathForWord(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }
        String upper = word.toUpperCase(Locale.ROOT);
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
                ArrayList<int[]> path = new ArrayList<>();
                if (dfsCollectBoardPath(upper, 0, row, col, visited, path)) {
                    return path;
                }
            }
        }
        return null;
    }

    /**
     * DFS helper that records the coordinate path.
     * @param word target word (uppercase expected)
     * @param index current character index
     * @param row current row
     * @param col current column
     * @param visited visit-state matrix
     * @param path output path
     * @return {@code true} when a full path is found
     */
    public boolean dfsCollectBoardPath(String word, int index, int row, int col, boolean[][] visited,
            ArrayList<int[]> path) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }
        if (visited[row][col]) {
            return false;
        }
        if (grid[row][col] != word.charAt(index)) {
            return false;
        }

        path.add(new int[] {row, col});
        if (index == word.length() - 1) {
            return true;
        }
        visited[row][col] = true;

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;
                }
                if (dfsCollectBoardPath(word, index + 1, row + dRow, col + dCol, visited, path)) {
                    visited[row][col] = false;
                    return true;
                }
            }
        }

        visited[row][col] = false;
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Sorts words by length descending (longest first). Same-length words keep discovery order.
     * @param words word list to sort in place
     */
    public static void insertionSortWordsLongestFirst(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String key = words.get(i);
            int j = i - 1;
            while (j >= 0 && words.get(j).length() < key.length()) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, key);
        }
    }

    /**
     * Collects all currently playable dictionary words.
     * @param usedWords words already used this round
     * @param minLen minimum allowed word length
     * @return sorted playable words
     */
    public ArrayList<String> findAllPlayableWordsSorted(HashSet<String> usedWords, int minLen) {
        HashSet<String> found = new HashSet<>();
        int maxLen = BOARD_SIZE * BOARD_SIZE;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                boolean[][] vis = new boolean[BOARD_SIZE][BOARD_SIZE];
                dfsCollectWords(r, c, new StringBuilder(), vis, found, usedWords, minLen, maxLen);
            }
        }
        ArrayList<String> list = new ArrayList<>(found);
        insertionSortWordsLongestFirst(list);
        return list;
    }

    /**
     * DFS collector used for full-board playable-word enumeration.
     * @param row current row
     * @param col current column
     * @param path current path text
     * @param visited visit-state matrix
     * @param found unique found words
     * @param usedWords words already used this round
     * @param minLen minimum allowed word length
     * @param maxLen maximum traversal length
     */
    private void dfsCollectWords(int row, int col, StringBuilder path, boolean[][] visited,
            HashSet<String> found, HashSet<String> usedWords, int minLen, int maxLen) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || visited[row][col]) {
            return;
        }
        visited[row][col] = true;
        path.append(Character.toLowerCase(grid[row][col]));
        String w = path.toString();
        if (!canStartWord(w)) {
            path.deleteCharAt(path.length() - 1);
            visited[row][col] = false;
            return;
        }
        if (w.length() >= minLen && !usedWords.contains(w) && isKnownWord(w)) {
            found.add(w);
        }
        if (path.length() < maxLen) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) {
                        continue;
                    }
                    dfsCollectWords(row + dr, col + dc, path, visited, found, usedWords, minLen, maxLen);
                }
            }
        }
        path.deleteCharAt(path.length() - 1);
        visited[row][col] = false;
    }
}
