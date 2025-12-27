package core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Class untuk merepresentasikan state/kondisi puzzle
 * Menggunakan struktur data: Array 2D, List, String
 */
public class PuzzleState {
    private int[][] board;
    private Point emptyPosition;
    private String stateKey;
    private PuzzleState parent;
    private String moveDescription;
    private int rows;
    private int cols;

    public PuzzleState(int[][] board, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];

        // Deep copy board
        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, cols);
        }

        // Find empty position (represented by 0)
        findEmptyPosition();
        this.stateKey = generateStateKey();
    }

    public PuzzleState(int[][] board, int rows, int cols, PuzzleState parent, String move) {
        this(board, rows, cols);
        this.parent = parent;
        this.moveDescription = move;
    }

    private void findEmptyPosition() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 0) {
                    emptyPosition = new Point(i, j);
                    return;
                }
            }
        }
    }

    private String generateStateKey() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Generate tetangga/neighbors menggunakan ArrayList
     */
    public List<PuzzleState> getNeighbors() {
        List<PuzzleState> neighbors = new ArrayList<>();

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        String[] moveNames = {"Geser Bawah", "Geser Atas", "Geser Kanan", "Geser Kiri"};

        int emptyRow = emptyPosition.x;
        int emptyCol = emptyPosition.y;

        for (int i = 0; i < directions.length; i++) {
            int newRow = emptyRow + directions[i][0];
            int newCol = emptyCol + directions[i][1];

            if (isValidPosition(newRow, newCol)) {
                int[][] newBoard = copyBoard();
                // Swap
                newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                newBoard[newRow][newCol] = 0;

                neighbors.add(new PuzzleState(newBoard, rows, cols, this, moveNames[i]));
            }
        }

        return neighbors;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    private int[][] copyBoard() {
        int[][] copy = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    /**
     * Hitung jumlah inversion untuk mengecek solvability
     */
    public int countInversions() {
        int[] flatArray = new int[rows * cols - 1]; // exclude 0
        int idx = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] != 0) {
                    flatArray[idx++] = board[i][j];
                }
            }
        }

        int inversions = 0;
        for (int i = 0; i < flatArray.length; i++) {
            for (int j = i + 1; j < flatArray.length; j++) {
                if (flatArray[i] > flatArray[j]) {
                    inversions++;
                }
            }
        }

        return inversions;
    }

    /**
     * Cek apakah puzzle solvable (inversion genap)
     */
    public boolean isSolvable() {
        return countInversions() % 2 == 0;
    }

    /**
     * Perbaiki puzzle jika tidak solvable
     */
    public void makeItSolvable() {
        if (isSolvable()) return;

        // Swap dua angka non-zero pertama
        outerLoop:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] != 0) {
                    for (int x = i; x < rows; x++) {
                        for (int y = 0; y < cols; y++) {
                            if (board[x][y] != 0 && !(i == x && j == y)) {
                                int temp = board[i][j];
                                board[i][j] = board[x][y];
                                board[x][y] = temp;
                                break outerLoop;
                            }
                        }
                    }
                }
            }
        }

        this.stateKey = generateStateKey();
    }

    // Getters
    public int[][] getBoard() { return board; }
    public Point getEmptyPosition() { return emptyPosition; }
    public String getStateKey() { return stateKey; }
    public PuzzleState getParent() { return parent; }
    public String getMoveDescription() { return moveDescription; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getValueAt(int row, int col) {
        return board[row][col];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzleState)) return false;
        PuzzleState other = (PuzzleState) obj;
        return this.stateKey.equals(other.stateKey);
    }

    @Override
    public int hashCode() {
        return stateKey.hashCode();
    }
}