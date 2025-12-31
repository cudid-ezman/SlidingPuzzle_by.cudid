package core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * TREE NODE STRUCTURE - PuzzleState
 * ═══════════════════════════════════════════════════════════════════════════
 * Class ini merepresentasikan sebuah NODE dalam SEARCH TREE.
 * Struktur Tree terbentuk melalui:
 * - `parent` pointer: Menghubungkan child node ke parent node
 * - Root node: Node dengan parent == null (initial state)
 * - Leaf nodes: Node tanpa children yang di-expand
 * - Path: Traversal dari goal node ke root menggunakan parent pointers
 * Setiap node menyimpan:
 * - State puzzle (board configuration)
 * - Reference ke parent node (untuk path reconstruction)
 * - Deskripsi move yang dilakukan dari parent
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class PuzzleState {
    
    // =========================================================================
    // TREE NODE ATTRIBUTES
    // =========================================================================
    
    /**
     * 🌳 TREE STRUCTURE: Parent pointer untuk membentuk Search Tree.
     * Setiap node (kecuali root) memiliki parent yang menunjuk ke state sebelumnya.
     * Ini membentuk implicit tree structure di memory.
     */
    private final PuzzleState parent;
    
    /**
     * Deskripsi move yang dilakukan untuk mencapai state ini dari parent.
     * Contoh: "Geser Atas", "Geser Kiri"
     */
    private final String moveDescription;
    
    // =========================================================================
    // PUZZLE STATE ATTRIBUTES
    // =========================================================================
    
    private final int[][] board;
    private final Point emptyPosition;
    private final String stateKey;
    private final int rows;
    private final int cols;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    /**
     * Constructor untuk ROOT NODE (initial state).
     * Parent = null menandakan ini adalah root dari search tree.
     */
    public PuzzleState(int[][] board, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, cols);
        }

        this.emptyPosition = findEmptyPosition();
        this.stateKey = generateStateKey();
        
        // 🌳 ROOT NODE: Tidak memiliki parent
        this.parent = null;
        this.moveDescription = "START";
    }

    /**
     * Constructor untuk CHILD NODE.
     * Menerima parent reference, membentuk edge dalam search tree.
     * 
     * @param board Board configuration
     * @param rows Jumlah baris
     * @param cols Jumlah kolom
     * @param parent Parent node dalam search tree (🌳 TREE EDGE)
     * @param move Deskripsi move dari parent ke node ini
     */
    public PuzzleState(int[][] board, int rows, int cols, PuzzleState parent, String move) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, cols);
        }

        this.emptyPosition = findEmptyPosition();
        this.stateKey = generateStateKey();
        
        // 🌳 TREE STRUCTURE: Link ke parent node
        this.parent = parent;
        this.moveDescription = move;
    }

    // =========================================================================
    // GRAPH OPERATIONS - NEIGHBOR GENERATION
    // =========================================================================
    
    /**
     * 📊 GRAPH OPERATION: Generate semua neighbors (adjacent nodes) dari node ini.
     * Dalam konteks Graf:
     * - Current state = Vertex
     * - Possible moves = Edges
     * - Neighbors = Adjacent vertices yang bisa dicapai dengan 1 move
     * Method ini mengimplementasikan ADJACENCY FUNCTION dalam Graf implisit.
     * Graf tidak disimpan seluruhnya di memory, tapi di-generate on-the-fly.
     * 
     * @return List of neighbor states (adjacent vertices)
     */
    public List<PuzzleState> getNeighbors() {
        List<PuzzleState> neighbors = new ArrayList<>();

        // Definisi 4 arah pergerakan (up, down, left, right)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        String[] moveNames = {"Geser Bawah", "Geser Atas", "Geser Kanan", "Geser Kiri"};

        int emptyRow = emptyPosition.x;
        int emptyCol = emptyPosition.y;

        // 📊 GRAPH EDGE GENERATION: Coba semua possible moves
        for (int i = 0; i < directions.length; i++) {
            int newRow = emptyRow + directions[i][0];
            int newCol = emptyCol + directions[i][1];

            if (isValidPosition(newRow, newCol)) {
                // Valid move = valid edge dalam graf
                int[][] newBoard = copyBoard();
                newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                newBoard[newRow][newCol] = 0;

                // 🌳 CREATE CHILD NODE dengan parent pointer ke current node
                neighbors.add(new PuzzleState(newBoard, rows, cols, this, moveNames[i]));
            }
        }

        return neighbors;
    }

    // =========================================================================
    // HASH & EQUALS - UNTUK HASHSET (GRAPH VISITED SET)
    // =========================================================================
    
    /**
     * 📊 GRAPH DATA STRUCTURE SUPPORT: hashCode() untuk HashSet.
     * HashSet menggunakan hash table untuk menyimpan visited nodes.
     * Complexity: O(1) untuk add/contains operations.
     * Dua states dianggap sama jika board configuration-nya identik,
     * terlepas dari path yang diambil untuk mencapainya.
     */
    @Override
    public int hashCode() {
        return Objects.hash(stateKey);
    }

    /**
     * 📊 GRAPH DATA STRUCTURE SUPPORT: equals() untuk HashSet.
     * Method ini menentukan apakah dua nodes dalam Graf adalah node yang sama.
     * Dalam sliding puzzle, dua states sama jika board configuration identik.
     * Ini penting untuk cycle detection dalam Graf.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzleState other)) return false;
        return this.stateKey.equals(other.stateKey);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private Point findEmptyPosition() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 0) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Generate unique key untuk state ini.
     * Key ini digunakan untuk:
     * 1. HashSet membership test (visited check)
     * 2. Goal state comparison
     */
    private String generateStateKey() {
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

    // =========================================================================
    // SOLVABILITY CHECK
    // =========================================================================
    
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

    public boolean isSolvable() {
        int inversions = countInversions();
        
        if (cols % 2 == 1) {
            // Odd width: solvable if inversions is even
            return inversions % 2 == 0;
        } else {
            // Even width: consider blank position
            int blankRow = emptyPosition.x;
            int blankRowFromBottom = rows - blankRow;
            return (inversions + blankRowFromBottom) % 2 == 1;
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public int[][] getBoard() { return board; }
    public Point getEmptyPosition() { return emptyPosition; }
    public String getStateKey() { return stateKey; }
    
    /**
     * 🌳 TREE STRUCTURE ACCESS: Get parent node untuk path reconstruction
     */
    public PuzzleState getParent() { return parent; }
    
    public String getMoveDescription() { return moveDescription; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getValueAt(int row, int col) {
        return board[row][col];
    }
}