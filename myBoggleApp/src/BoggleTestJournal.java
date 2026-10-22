import java.io.*;
import java.util.*;

/**
 * Test runner for Boggle.
 * Generates a random 5x5 board from the 25 official dice (same as the game),
 * finds all valid words, and appends results to src/resources/test_journal.txt.
 *
 * Run from boggle-java-gui/:
 *   javac -d out src/*.java
 *   java -cp out BoggleTestJournal
 */
public class BoggleTestJournal {

    /**
     * Generates one board analysis entry and appends it to the test journal.
     */
    public static void main(String[] args) throws IOException {

        // locate src/resources
        String base = System.getProperty("user.dir");
        File resourcesDir = new File(base + "/src/resources");
        if (!resourcesDir.isDirectory()) {
            resourcesDir = new File(base + "/boggle-java-gui/src/resources");
        }

        BoggleBoard board = new BoggleBoard();
        board.generateBoggleBoard();
        board.loadDictionary();
        HashSet<String> usedWords = new HashSet<>();
        int minWordLength = 3;

        // --- find all valid words ---
        ArrayList<String> words = board.findAllPlayableWordsSorted(usedWords, minWordLength);

        // --- count words per length ---
        int[] countByLen = new int[26];
        for (int i = 0; i < words.size(); i++) {
            int len = words.get(i).length();
            countByLen[len]++;
        }

        // --- print summary to console ---
        System.out.println("Total playable words (min length 3): " + words.size());
        for (int len = 25; len >= 3; len--) {
            if (countByLen[len] > 0) {
                System.out.println("  length " + len + ": " + countByLen[len] + " words");
            }
        }

        // --- append to journal file ---
        File journalFile = new File(resourcesDir, "test_journal.txt");
        FileWriter fw = new FileWriter(journalFile, true);

        fw.write("============================================================\n");
        fw.write("Date: " + new Date() + "\n");
        fw.write("Min word length: " + minWordLength + "\n");

        fw.write("\nBoard:\n");
        for (int r = 0; r < BoggleBoard.BOARD_SIZE; r++) {
            for (int c = 0; c < BoggleBoard.BOARD_SIZE; c++) {
                if (c > 0) fw.write(' ');
                fw.write(board.grid()[r][c]);
            }
            fw.write('\n');
        }

        fw.write("\nWords by length (largest to smallest):\n");
        for (int len = 25; len >= 3; len--) {
            if (countByLen[len] > 0) {
                fw.write("  length " + len + ": " + countByLen[len] + " words\n");
            }
        }
        fw.write("Total: " + words.size() + " words\n");

        fw.write("\nWord list (longest first):\n");
        for (int i = 0; i < words.size(); i++) {
            fw.write(words.get(i) + "\n");
        }

        fw.write("\n");
        fw.close();

        System.out.println("Journal appended to: " + journalFile.getAbsolutePath());
    }
}
