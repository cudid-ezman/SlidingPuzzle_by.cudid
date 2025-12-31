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
        // 1. AMBIL UKURAN DARI DIFFICULTY (Agar variable rows & cols dikenali)
        int rows = currentDifficulty.rows();
        int cols = currentDifficulty.cols();

        System.out.println("[LEVEL] Generating puzzle with Random Walk...");

        // 2. Mulai dari Goal State
        PuzzleState current = generateGoalState();
        PuzzleState previous = null;
        
        // 3. Tentukan jumlah langkah acak berdasarkan kesulitan
        int scrambleSteps;
        if (currentDifficulty.equals(Difficulty.EASY)) {
            scrambleSteps = 15;
        } else if (currentDifficulty.equals(Difficulty.MEDIUM)) {
            scrambleSteps = 30;
        } else {
            scrambleSteps = 50; // HARD
        }

        System.out.println("[LEVEL] Scrambling " + scrambleSteps + " steps...");

        // 4. Lakukan Pengacakan (Random Walk)
        for (int i = 0; i < scrambleSteps; i++) {
            List<PuzzleState> neighbors = current.getNeighbors();
            List<PuzzleState> candidates = new ArrayList<>();

            for (PuzzleState neighbor : neighbors) {
                // Cegah gerak maju-mundur (jangan balik ke state sebelumnya)
                if (previous == null || !neighbor.getStateKey().equals(previous.getStateKey())) {
                    candidates.add(neighbor);
                }
            }

            if (!candidates.isEmpty()) {
                previous = current;
                // Pilih langkah acak selanjutnya
                int randomIndex = (int) (Math.random() * candidates.size());
                current = candidates.get(randomIndex);
            }
        }

        // 5. PENTING: Putus rantai parent (History) agar ini jadi START murni
        // Kita buat object PuzzleState baru dari posisi terakhir
        PuzzleState finalState = new PuzzleState(current.getBoard(), rows, cols);
        
        System.out.println("[LEVEL] Scramble complete. Start Key: " + finalState.getStateKey());
        
        return finalState;
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