package core;

import java.util.*;
import java.awt.Point;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPLEMENTASI ALGORITMA A* (A-STAR)
 * ═══════════════════════════════════════════════════════════════════════════
 * Penerapan Struktur Data:
 * 1. PRIORITY QUEUE (Min-Heap): Menggantikan Queue biasa pada BFS.
 * Berguna untuk mengambil node dengan biaya (cost) terendah secara otomatis.
 * 2. HASHMAP: Untuk menyimpan 'visited' nodes dengan akses O(1).
 * 3. GRAPH: Representasi ruang keadaan (State Space).
 * * Keunggulan: Bisa menyelesaikan puzzle 4x4 (15-puzzle) dalam hitungan detik.
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class OptimizedPuzzleSolver {

    private final PuzzleState goalState;
    private int maxIterations;
    
    // Map untuk menyimpan koordinat tujuan setiap angka (Optimasi hitungan)
    private final Map<Integer, Point> goalPositions;

    public OptimizedPuzzleSolver(PuzzleState goalState) {
        this.goalState = goalState;
        // A* jauh lebih cepat, 200rb iterasi biasanya sudah cukup untuk 4x4
        this.maxIterations = 200000; 
        this.goalPositions = precomputeGoalPositions(goalState);
    }
    
    public void setMaxIterations(int max) {
        this.maxIterations = max;
    }

    /**
     * Node Wrapper untuk Priority Queue.
     * Menyimpan state puzzle beserta nilai 'Score'-nya.
     */
    private static class SearchNode implements Comparable<SearchNode> {
        PuzzleState state;
        int g; // Cost so far (langkah yang sudah diambil dari start)
        int h; // Heuristic (estimasi jarak ke goal - Manhattan Distance)
        int f; // Total Score (f = g + h)

        public SearchNode(PuzzleState state, int g, int h) {
            this.state = state;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        // Ini yang membuat PriorityQueue bekerja: Mengurutkan berdasarkan 'f' terkecil
        @Override
        public int compareTo(SearchNode other) {
            return Integer.compare(this.f, other.f);
        }
    }

    /**
     * Method Utama Solver. 
     * Menggunakan nama 'solveBFSOptimized' agar kompatibel dengan kode lama Anda,
     * tapi isinya sekarang adalah logika A* SEARCH.
     */
    public List<PuzzleState> solveBFSOptimized(PuzzleState startState) {
        // Cek solvability
        if (!startState.isSolvable()) {
            System.out.println("[A*] Puzzle logic says unsolvable.");
            return null;
        }

        System.out.println("[A*] Starting search (Manhattan Heuristic)...");

        // [STRUKTUR DATA] PriorityQueue (Min-Heap)
        // Menyimpan node yang akan dieksplorasi, diurutkan berdasarkan yang paling menjanjikan.
        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        
        // [STRUKTUR DATA] HashMap
        // Menyimpan cost terbaik (g-score) untuk mencapai state tertentu.
        // Berfungsi sekaligus sebagai 'Visited Set'.
        Map<String, Integer> gScoreMap = new HashMap<>();

        // Inisialisasi Awal
        int startH = calculateManhattanDistance(startState);
        SearchNode startNode = new SearchNode(startState, 0, startH);
        
        openSet.add(startNode);
        gScoreMap.put(startState.getStateKey(), 0);

        int iterations = 0;

        while (!openSet.isEmpty()) {
            iterations++;
            
            // Safety break
            if (iterations > maxIterations) {
                System.out.println("[A*] Limit reached (" + iterations + ")");
                return null; 
            }

            // Ambil node dengan prioritas terbaik (f terendah)
            SearchNode currentWrapper = openSet.poll();
            PuzzleState current = currentWrapper.state;

            // Cek apakah sudah sampai Goal?
            if (current.getStateKey().equals(goalState.getStateKey())) {
                System.out.println("[A*] Solution found in " + iterations + " iterations.");
                return reconstructPath(current);
            }

            // Generate Tetangga (Graph Expansion)
            for (PuzzleState neighbor : current.getNeighbors()) {
                int tentativeG = currentWrapper.g + 1; // Biaya langkah bertambah 1
                String neighborKey = neighbor.getStateKey();

                // Jika neighbor ini belum pernah dikunjungi, 
                // ATAU kita menemukan jalan yang lebih pendek ke neighbor ini
                if (!gScoreMap.containsKey(neighborKey) || tentativeG < gScoreMap.get(neighborKey)) {
                    
                    // Update cost terbaik
                    gScoreMap.put(neighborKey, tentativeG);
                    
                    // Hitung Heuristik (Kecerdasan A*)
                    int h = calculateManhattanDistance(neighbor);
                    
                    // Masukkan ke Priority Queue
                    openSet.add(new SearchNode(neighbor, tentativeG, h));
                }
            }
        }

        return null; // Tidak ada solusi ditemukan
    }

    /**
     * FUNGSI HEURISTIK: MANHATTAN DISTANCE      * Menghitung total jarak setiap kotak ke posisi seharusnya.
     * Menggunakan Array int[][] langsung agar AMAN dari error String parsing.
     */
    private int calculateManhattanDistance(PuzzleState state) {
        int distance = 0;
        int[][] board = state.getBoard();
        int rows = state.getRows();
        int cols = state.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int value = board[r][c];
                if (value != 0) { // Abaikan kotak kosong (0)
                    Point target = goalPositions.get(value);
                    if (target != null) {
                        // Rumus Manhattan: |x1 - x2| + |y1 - y2|
                        distance += Math.abs(r - target.x) + Math.abs(c - target.y);
                    }
                }
            }
        }
        return distance;
    }

    // Pre-compute posisi target agar pencarian cepat (O(1))
    private Map<Integer, Point> precomputeGoalPositions(PuzzleState goal) {
        Map<Integer, Point> map = new HashMap<>();
        int[][] board = goal.getBoard();
        for (int r = 0; r < goal.getRows(); r++) {
            for (int c = 0; c < goal.getCols(); c++) {
                map.put(board[r][c], new Point(r, c));
            }
        }
        return map;
    }

    // Rekonstruksi Jalur (Backtracking Tree)
    private List<PuzzleState> reconstructPath(PuzzleState state) {
        LinkedList<PuzzleState> path = new LinkedList<>();
        PuzzleState current = state;
        while (current != null) {
            path.addFirst(current); // Menambahkan ke depan list (Reverse order)
            current = current.getParent();
        }
        return path;
    }
}