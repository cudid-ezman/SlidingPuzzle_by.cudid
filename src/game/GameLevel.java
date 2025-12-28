package game;

import core.PuzzleState;

import java.util.*;

public class GameLevel {

    public record Difficulty(String name, int rows, int cols, String description) {
        public static final Difficulty EASY = new Difficulty("EASY", 3, 3, "Mudah - 3x3");
        public static final Difficulty MEDIUM = new Difficulty("MEDIUM", 4, 3, "Sedang - 4x3");
        public static final Difficulty HARD = new Difficulty("HARD", 4, 4, "Sulit - 4x4");
    }

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

    private static final ArrayList<ScoreEntry> bestScores = new ArrayList<>();

    public GameLevel(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        this.currentLevelNumber = 1;
        this.totalMoves = 0;
        this.bestScore = getBestScoreForDifficulty(difficulty);
        this.startTime = System.currentTimeMillis();
    }

    private int getBestScoreForDifficulty(Difficulty difficulty) {
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                return entry.score;
            }
        }
        return Integer.MAX_VALUE;
    }

    private void setBestScoreForDifficulty(Difficulty difficulty, int score) {
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                entry.score = score;
                return;
            }
        }
        bestScores.add(new ScoreEntry(difficulty, score));
    }

    public PuzzleState generateInitialState() {

        int minMovesFromGoal;
        int maxScrambleMoves;

        if (currentDifficulty.equals(Difficulty.EASY)) {
            minMovesFromGoal = 8;
            maxScrambleMoves = 15;
        } else if (currentDifficulty.equals(Difficulty.MEDIUM)) {
            minMovesFromGoal = 10;
            maxScrambleMoves = 20;
        } else {
            minMovesFromGoal = 12;
            maxScrambleMoves = 25;
        }

        System.out.println("[LEVEL] Generating puzzle...");
        System.out.println("[LEVEL] Target minimum moves from goal: " + minMovesFromGoal);
        System.out.println("[LEVEL] Difficulty: " + currentDifficulty.description());

        PuzzleState bestState = null;
        int bestDistance = 0;
        int maxAttempts = 20;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            PuzzleState state = generateGoalState();
            PuzzleState prevState = null;

            int scrambleMoves = minMovesFromGoal + (int)(Math.random() * (maxScrambleMoves - minMovesFromGoal));

            for (int i = 0; i < scrambleMoves; i++) {
                List<PuzzleState> neighbors = state.getNeighbors();

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

            if (!state.isSolvable()) {
                continue;
            }

            PuzzleState goalState = generateGoalState();
            if (state.getStateKey().equals(goalState.getStateKey())) {
                continue;
            }

            int misplacedTiles = countMisplacedTiles(state);

            if (misplacedTiles >= minMovesFromGoal / 2) {
                if (misplacedTiles > bestDistance) {
                    bestState = state;
                    bestDistance = misplacedTiles;
                }

                if (misplacedTiles >= minMovesFromGoal) {
                    System.out.println("[LEVEL]  Good puzzle found!");
                    System.out.println("[LEVEL] Scramble moves: " + scrambleMoves);
                    System.out.println("[LEVEL] Misplaced tiles: " + misplacedTiles);
                    System.out.println("[LEVEL] Inversion count: " + state.countInversions());
                    return state;
                }
            }
        }

        if (bestState != null) {
            System.out.println("[LEVEL]  Using best found puzzle");
            System.out.println("[LEVEL] Misplaced tiles: " + bestDistance);
            System.out.println("[LEVEL] Inversion count: " + bestState.countInversions());
            return bestState;
        }

        System.out.println("[LEVEL]   Doing deep scramble...");
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

    private int countMisplacedTiles(PuzzleState state) {
        int[][] board = state.getBoard();
        int rows = state.getRows();
        int cols = state.getCols();
        int misplaced = 0;

        int expectedValue = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
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

    public PuzzleState generateGoalState() {
        int rows = currentDifficulty.rows();
        int cols = currentDifficulty.cols();
        int[][] board = new int[rows][cols];

        int value = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == rows - 1 && j == cols - 1) {
                    board[i][j] = 0;
                } else {
                    board[i][j] = value++;
                }
            }
        }

        return new PuzzleState(board, rows, cols);
    }

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

    public Difficulty getCurrentDifficulty() { return currentDifficulty; }

    public int getTotalMoves() { return totalMoves; }

    public String getLevelInfo() {
        return String.format("Level %d - %s", currentLevelNumber, currentDifficulty.description());
    }

    public String getScoreInfo() {
        return String.format("Moves: %d | Best: %s | Time: %s",
                totalMoves,
                bestScore == Integer.MAX_VALUE ? "-" : String.valueOf(bestScore),
                getFormattedTime());
    }
}