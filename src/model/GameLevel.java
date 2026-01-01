package model;

import util.PuzzleTree;
import java.util.ArrayList;
import java.util.List;

public class GameLevel {
    public record Difficulty(String name, int rows, int cols, String description) {
        public static final Difficulty EASY = new Difficulty("EASY", 3, 3, "EASY - 3x3");
        public static final Difficulty MEDIUM = new Difficulty("MEDIUM", 4, 3, "MEDIUM - 4x3");
        public static final Difficulty HARD = new Difficulty("HARD", 4, 4, "HARD - 4x4");
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
        this.bestScore = ambilScoreTerbaik(difficulty);
        this.startTime = System.currentTimeMillis();
    }

    private int ambilScoreTerbaik(Difficulty difficulty) {
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                return entry.score;
            }
        }
        return Integer.MAX_VALUE;
    }

    private void simpanScoreTerbaik(Difficulty difficulty, int score) {
        for (ScoreEntry entry : bestScores) {
            if (entry.difficulty.equals(difficulty)) {
                entry.score = score;
                return;
            }
        }
        bestScores.add(new ScoreEntry(difficulty, score));
    }

   public PuzzleTree StateAwal() {
        int rows = currentDifficulty.rows();
        int cols = currentDifficulty.cols();

        System.out.println("[LEVEL] Generating puzzle with Random Walk...");

        PuzzleTree current = stateTujuan();
        PuzzleTree previous = null;
        
        int scrambleSteps;
        if (currentDifficulty.equals(Difficulty.EASY)) {
            scrambleSteps = 15;
        } else if (currentDifficulty.equals(Difficulty.MEDIUM)) {
            scrambleSteps = 30;
        } else {
            scrambleSteps = 50;
        }

        System.out.println("[LEVEL] Scrambling " + scrambleSteps + " steps...");

        for (int i = 0; i < scrambleSteps; i++) {
            List<PuzzleTree> neighbors = current.getNeighbors();
            List<PuzzleTree> candidates = new ArrayList<>();

            for (PuzzleTree neighbor : neighbors) {
                if (previous == null || !neighbor.getStateKey().equals(previous.getStateKey())) {
                    candidates.add(neighbor);
                }
            }

            if (!candidates.isEmpty()) {
                previous = current;
                int randomIndex = (int) (Math.random() * candidates.size());
                current = candidates.get(randomIndex);
            }
        }

        PuzzleTree finalState = new PuzzleTree(current.getBoard(), rows, cols);
        
        System.out.println("[LEVEL] Scramble complete. Start Key: " + finalState.getStateKey());
        
        return finalState;
    }

    public PuzzleTree stateTujuan() {
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

        return new PuzzleTree(board, rows, cols);
    }

    public void tambahLangkah() {
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
            simpanScoreTerbaik(currentDifficulty, totalMoves);
        }
    }

    public String ambilWaktu() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public Difficulty getCurrentDifficulty() { return currentDifficulty; }

    public int getTotalMoves() { return totalMoves; }

    public String getLevelInfo() {
        return String.format("Game %d - %s", currentLevelNumber, currentDifficulty.description());
    }

    public String getScoreInfo() {
        return String.format("Moves: %d | Best: %s | Time: %s",
                totalMoves,
                bestScore == Integer.MAX_VALUE ? "-" : String.valueOf(bestScore),
                ambilWaktu());
    }
}