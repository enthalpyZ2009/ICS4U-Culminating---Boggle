# MyBoggleApp — Word Hunt Game

A Word game built with Java Swing for my Grade 12 Computer Science (ICS4U) culminating project.

Basically, it's a multiplayer version of Word Hunt where players find words by connecting adjacent letters on a 5×5 board. Longer words earn more points, and the player with the highest score wins.

## Features

- Play against other players or an AI opponent
- Four AI difficulty levels: Easy, Medium, Hard, and Impossible
- Click or drag across letters to form words
- Customize game settings, including time limits, minimum word length, target score, and number of rounds
- Shake the board to generate new letters
- Optional hints showing words you haven't found yet
- Save and resume games
- Three colour themes, background music, and sound effects
- Track scores, words found, and round results

## How to Run

Make sure you have Java JDK 17 or later installed.

1. Clone or download the repository.
2. Open the project folder.
3. Compile the Java files:

   ```bash
   javac -d out src/*.java
   ```

4. Run the game:

   **Windows:**
   ```bash
   java -cp "out;src" MyBoggleApp
   ```

   **macOS/Linux:**
   ```bash
   java -cp "out:src" MyBoggleApp
   ```


