package util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class PuzzleTree {
    private final PuzzleTree parent;
    private final String deskripsiMove;
    
    private final int[][] board;
    private final Point emptyPosition;
    private final String stateKey;
    private final int rows;
    private final int cols;

    public PuzzleTree(int[][] board, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, cols);
        }
        this.emptyPosition = cariPosisiKosong();
        this.stateKey = buatKunciState();
        this.parent = null;
        this.deskripsiMove = "START";
    }

    public PuzzleTree(int[][] board, int rows, int cols, PuzzleTree parent, String move) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, cols);
        }
        this.emptyPosition = cariPosisiKosong();
        this.stateKey = buatKunciState();
        this.parent = parent;
        this.deskripsiMove = move;
    }

    public List<PuzzleTree> getNeighbors() {
        List<PuzzleTree> neighbors = new ArrayList<>();

        int[][] arah = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        String[] moveNames = {"Geser Bawah", "Geser Atas", "Geser Kanan", "Geser Kiri"};

        int emptyRow = emptyPosition.x;
        int emptyCol = emptyPosition.y;

        for (int i = 0; i < arah.length; i++) {
            int newRow = emptyRow + arah[i][0];
            int newCol = emptyCol + arah[i][1];

            if (isValidPosition(newRow, newCol)) {
                int[][] newBoard = copyBoard();
                newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                newBoard[newRow][newCol] = 0;

                neighbors.add(new PuzzleTree(newBoard, rows, cols, this, moveNames[i]));
            }
        }

        return neighbors;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzleTree other)) return false;
        return this.stateKey.equals(other.stateKey);
    }
    
    private Point cariPosisiKosong() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 0) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    private String buatKunciState() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(board[i][j]).append(",");
            }
        }
        return sb.toString();
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
    
    public int hitungInversi() {
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

    public boolean isSolvable() {
        int inversions = hitungInversi();
        
        if (cols % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            int blankRow = emptyPosition.x;
            int blankRowFromBottom = rows - blankRow;
            return (inversions + blankRowFromBottom) % 2 == 1;
        }
    }
    
    public int[][] getBoard() { return board; }
    public Point getEmptyPosition() { return emptyPosition; }
    public String getStateKey() { return stateKey; }
    
    public PuzzleTree getParent() { return parent; }
    
    public String getDeskripsiMove() { return deskripsiMove; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int ambilNilaidi(int row, int col) {
        return board[row][col];
    }
}