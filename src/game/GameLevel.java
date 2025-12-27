package game;

import core.PuzzleState;

import java.util.*;

/**
 * Class untuk mengatur level permainan
 * REFACTORED: Mengganti Enum dengan Class biasa
 * Mengganti HashMap dengan ArrayList
 */
public class GameLevel {

    /**
     * Difficulty Class - menggantikan Enum
     * Menggunakan static instances untuk membuat "pseudo-enum"
     */
    public static class Difficulty {
        private final String name;
        private final int rows;
        private final int cols;
        private final String description;

        // Static instances (seperti enum constants)
        public static final Difficulty EASY = new Difficulty("EASY", 3, 3, "Mudah - 3x3");
        public static final Difficulty MEDIUM = new Difficulty("MEDIUM", 4, 3, "Sedang - 4x3");
        public static final Difficulty HARD = new Difficulty("HARD", 4, 4, "Sulit - 4x4");

        // Array untuk menyimpan semua difficulties (seperti Enum.values())
        private static final Difficulty[] ALL_DIFFICULTIES = {EASY, MEDIUM, HARD};

        private Difficulty(String name, int rows, int cols, String description) {
            this.name = name;
            this.rows = rows;
            this.cols = cols;
            this.description = description;
        }

        public String getName() { return name; }
        public int getRows() { return rows; }
        public int getCols() { return cols; }
        public String getDescription() { return description; }
        public int getTotalTiles() { return rows * cols; }

        public static Difficulty[] values() {
            return ALL_DIFFICULTIES.clone();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Difficulty other)) return false;
            return this.name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Best Score Entry - untuk menyimpan score per difficulty
     * Menggantikan HashMap dengan ArrayList of entries
     */
    private static class ScoreEntry {
        Difficulty difficulty;
        int score;

        ScoreEntry(Difficulty difficulty, int score) {
            this.difficulty = difficulty;
            this.score = score;
        }
    }

    private final Difficulty currentDifficulty;
    private int currentLevelNumber;
    private int totalMoves;
    private int bestScore;
    private long startTime;
    private long elapsedTime;

    // Mengganti HashMap dengan ArrayList
    private static final ArrayList<ScoreEntry> bestScores = new ArrayList<>();

    public GameLevel(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        this.currentLevelNumber = 1;
        this.totalMoves = 0;
        this.bestScore = getBestScoreForDifficulty(difficulty);
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Get best score untuk difficulty tertentu dari ArrayList
     * Menggantikan HashMap.get()
     */
    private int getBestScoreForDifficulty(Difficulty difficulty) {
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                return entry.score;
            }
        }
        return Integer.MAX_VALUE; // Default jika tidak ditemukan
    }

    /**
     * Set best score untuk difficulty tertentu di ArrayList
     * Menggantikan HashMap.put()
     */
    private void setBestScoreForDifficulty(Difficulty difficulty, int score) {
        // Cari apakah sudah ada entry untuk difficulty ini
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                entry.score = score;
                return;
            }
        }
        // Jika belum ada, tambahkan baru
        bestScores.add(new ScoreEntry(difficulty, score));
    }

    /**
     * Generate puzzle state awal untuk level ini
     * FIXED: Ensure minimum distance from goal
     */
    public PuzzleState generateInitialState() {
        int rows = currentDifficulty.getRows();
        int cols = currentDifficulty.getCols();

        // Target MINIMUM moves from goal
        // Ini memastikan puzzle tidak terlalu dekat dengan goal
        int minMovesFromGoal;
        int maxScrambleMoves;

        if (currentDifficulty.equals(Difficulty.EASY)) {
            minMovesFromGoal = 8;  // At least 8 moves dari goal
            maxScrambleMoves = 15;
        } else if (currentDifficulty.equals(Difficulty.MEDIUM)) {
            minMovesFromGoal = 10; // At least 10 moves dari goal
            maxScrambleMoves = 20;
        } else {
            minMovesFromGoal = 12; // At least 12 moves dari goal
            maxScrambleMoves = 25;
        }

        System.out.println("[LEVEL] Generating puzzle...");
        System.out.println("[LEVEL] Target minimum moves from goal: " + minMovesFromGoal);
        System.out.println("[LEVEL] Difficulty: " + currentDifficulty.getDescription());

        PuzzleState bestState = null;
        int bestDistance = 0;
        int maxAttempts = 20; // Try 20 times

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Start dari goal dan scramble
            PuzzleState state = generateGoalState();
            PuzzleState prevState = null;

            // Random scramble moves
            int scrambleMoves = minMovesFromGoal + (int)(Math.random() * (maxScrambleMoves - minMovesFromGoal));

            for (int i = 0; i < scrambleMoves; i++) {
                List<PuzzleState> neighbors = state.getNeighbors();

                // Filter: jangan kembali ke state sebelumnya
                ArrayList<PuzzleState> validMoves = new ArrayList<>();
                for (PuzzleState neighbor : neighbors) {
                    if (prevState == null ||
                            !neighbor.getStateKey().equals(prevState.getStateKey())) {
                        validMoves.add(neighbor);
                    }
                }

                if (!validMoves.isEmpty()) {
                    prevState = state;
                    int randomIndex = (int)(Math.random() * validMoves.size());
                    state = validMoves.get(randomIndex);
                }
            }

            // Verify solvability
            if (!state.isSolvable()) {
                continue; // Skip this attempt
            }

            // Check if it's goal state
            PuzzleState goalState = generateGoalState();
            if (state.getStateKey().equals(goalState.getStateKey())) {
                continue; // Skip, too close to goal!
            }

            // Quick check: how far is this from goal?
            // Count misplaced tiles (simple heuristic)
            int misplacedTiles = countMisplacedTiles(state);

            if (misplacedTiles >= minMovesFromGoal / 2) {
                // This is far enough from goal!
                if (misplacedTiles > bestDistance) {
                    bestState = state;
                    bestDistance = misplacedTiles;
                }

                // If we found a good one, use it
                if (misplacedTiles >= minMovesFromGoal) {
                    System.out.println("[LEVEL] ✅ Good puzzle found!");
                    System.out.println("[LEVEL] Scramble moves: " + scrambleMoves);
                    System.out.println("[LEVEL] Misplaced tiles: " + misplacedTiles);
                    System.out.println("[LEVEL] Inversion count: " + state.countInversions());
                    return state;
                }
            }
        }

        // Use best state we found
        if (bestState != null) {
            System.out.println("[LEVEL] ✅ Using best found puzzle");
            System.out.println("[LEVEL] Misplaced tiles: " + bestDistance);
            System.out.println("[LEVEL] Inversion count: " + bestState.countInversions());
            return bestState;
        }

        // Last resort: do DEEP scramble
        System.out.println("[LEVEL] ⚠️  Doing deep scramble...");
        PuzzleState state = generateGoalState();
        PuzzleState prevState = null;

        for (int i = 0; i < maxScrambleMoves * 2; i++) {
            List<PuzzleState> neighbors = state.getNeighbors();
            if (!neighbors.isEmpty()) {
                ArrayList<PuzzleState> validMoves = new ArrayList<>();
                for (PuzzleState n : neighbors) {
                    if (prevState == null || !n.getStateKey().equals(prevState.getStateKey())) {
                        validMoves.add(n);
                    }
                }
                if (!validMoves.isEmpty()) {
                    prevState = state;
                    state = validMoves.get((int)(Math.random() * validMoves.size()));
                }
            }
        }

        if (!state.isSolvable()) {
            state.makeItSolvable();
        }

        System.out.println("[LEVEL] Deep scramble complete");
        System.out.println("[LEVEL] Inversion count: " + state.countInversions());

        return state;
    }

    /**
     * Count misplaced tiles (tiles not in goal position)
     */
    private int countMisplacedTiles(PuzzleState state) {
        int[][] board = state.getBoard();
        int rows = state.getRows();
        int cols = state.getCols();
        int misplaced = 0;

        int expectedValue = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Last position should be 0 (empty)
                if (i == rows - 1 && j == cols - 1) {
                    if (board[i][j] != 0) {
                        misplaced++;
                    }
                } else {
                    if (board[i][j] != expectedValue) {
                        misplaced++;
                    }
                    expectedValue++;
                }
            }
        }

        return misplaced;
    }

    /**
     * Generate goal state (solved state)
     */
    public PuzzleState generateGoalState() {
        int rows = currentDifficulty.getRows();
        int cols = currentDifficulty.getCols();
        int[][] board = new int[rows][cols];

        int value = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == rows - 1 && j == cols - 1) {
                    board[i][j] = 0; // Empty tile di akhir
                } else {
                    board[i][j] = value++;
                }
            }
        }

        return new PuzzleState(board, rows, cols);
    }

    /**
     * Generate puzzle dengan tingkat kesulitan tertentu
     */
    public PuzzleState generatePuzzleWithDifficulty(int minMoves) {
        PuzzleState current = generateGoalState();

        // Menggunakan ArrayList untuk menyimpan visited states
        // Menggantikan HashSet
        ArrayList<String> visited = new ArrayList<>();
        Random random = new Random();

        visited.add(current.getStateKey());

        for (int i = 0; i < minMoves * 2; i++) {
            List<PuzzleState> neighbors = current.getNeighbors();

            // Filter neighbors yang belum dikunjungi
            ArrayList<PuzzleState> unvisited = new ArrayList<>();
            for (PuzzleState n : neighbors) {
                boolean found = false;
                String key = n.getStateKey();

                // Linear search di ArrayList (menggantikan HashSet.contains())
                for (String visitedKey : visited) {
                    if (visitedKey.equals(key)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    unvisited.add(n);
                }
            }

            if (unvisited.isEmpty()) {
                current = neighbors.get(random.nextInt(neighbors.size()));
            } else {
                current = unvisited.get(random.nextInt(unvisited.size()));
            }

            visited.add(current.getStateKey());
        }

        return current;
    }

    // Game progress methods
    public void incrementMoves() {
        totalMoves++;
    }

    public void resetMoves() {
        totalMoves = 0;
        startTime = System.currentTimeMillis();
    }

    public void updateElapsedTime() {
        elapsedTime = System.currentTimeMillis() - startTime;
    }

    public void nextLevel() {
        currentLevelNumber++;
        resetMoves();
    }

    public void updateBestScore() {
        if (totalMoves < bestScore) {
            bestScore = totalMoves;
            setBestScoreForDifficulty(currentDifficulty, totalMoves);
        }
    }

    public String getFormattedTime() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Getters
    public Difficulty getCurrentDifficulty() { return currentDifficulty; }
    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public int getTotalMoves() { return totalMoves; }
    public int getBestScore() { return bestScore; }
    public long getElapsedTime() { return elapsedTime; }

    public String getLevelInfo() {
        return String.format("Level %d - %s", currentLevelNumber, currentDifficulty.getDescription());
    }

    public String getScoreInfo() {
        return String.format("Moves: %d | Best: %s | Time: %s",
                totalMoves,
                bestScore == Integer.MAX_VALUE ? "-" : String.valueOf(bestScore),
                getFormattedTime());
    }
}