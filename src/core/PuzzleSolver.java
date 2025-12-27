package core;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Class untuk menyelesaikan puzzle menggunakan algoritma BFS
 * REFACTORED: Mengganti HashSet dengan ArrayList
 */
public class PuzzleSolver {

    private final PuzzleState goalState;
    private final int maxIterations;

    public PuzzleSolver(PuzzleState goalState) {
        this.goalState = goalState;
        this.maxIterations = 100000;
    }

    /**
     * Solve puzzle menggunakan BFS (Breadth-First Search)
     * Mengganti HashSet dengan ArrayList untuk visited states
     */
    public List<PuzzleState> solveBFS(PuzzleState startState) {
        if (!startState.isSolvable()) {
            System.out.println("[BFS] Puzzle is not solvable!");
            return null;
        }

        System.out.println("[BFS] Starting BFS algorithm...");
        long startTime = System.currentTimeMillis();

        Queue<PuzzleState> queue = new LinkedList<>();
        ArrayList<String> visited = new ArrayList<>(); // Mengganti HashSet

        queue.add(startState);
        visited.add(startState.getStateKey());

        int iterations = 0;
        int lastReportedProgress = 0;

        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;

            // Progress reporting setiap 1000 iterations
            if (iterations % 1000 == 0) {
                int progress = (iterations * 100) / maxIterations;
                if (progress != lastReportedProgress) {
                    System.out.println("[BFS] Progress: " + progress + "% | Visited: " + visited.size() + " | Queue: " + queue.size());
                    lastReportedProgress = progress;
                }
            }

            PuzzleState current = queue.poll();

            // Cek apakah sudah goal
            if (current.getStateKey().equals(goalState.getStateKey())) {
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("[BFS] Solution found!");
                System.out.println("[BFS] Iterations: " + iterations);
                System.out.println("[BFS] Time: " + elapsed + "ms");
                System.out.println("[BFS] States explored: " + visited.size());
                return reconstructPath(current);
            }

            // Explore neighbors
            for (PuzzleState neighbor : current.getNeighbors()) {
                String neighborKey = neighbor.getStateKey();

                // Linear search di ArrayList (menggantikan HashSet.contains())
                if (!containsKey(visited, neighborKey)) {
                    visited.add(neighborKey);
                    queue.add(neighbor);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("[BFS] No solution found after " + iterations + " iterations");
        System.out.println("[BFS] Time: " + elapsed + "ms");
        return null;
    }

    /**
     * Helper method untuk mengecek apakah ArrayList contains key
     * Menggantikan HashSet.contains() dengan linear search
     */
    private boolean containsKey(ArrayList<String> list, String key) {
        for (String item : list) {
            if (item.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rekonstruksi path dari goal ke start menggunakan parent pointer
     */
    private List<PuzzleState> reconstructPath(PuzzleState goalState) {
        LinkedList<PuzzleState> path = new LinkedList<>();
        PuzzleState current = goalState;

        while (current != null) {
            path.addFirst(current);
            current = current.getParent();
        }

        return path;
    }

    /**
     * Get jumlah langkah minimum untuk solve
     */
    public int getMinimumMoves(PuzzleState startState) {
        List<PuzzleState> solution = solveBFS(startState);
        if (solution == null) return -1;
        return solution.size() - 1;
    }

    /**
     * Cek apakah dua state bertetangga
     */
    public boolean areNeighbors(PuzzleState state1, PuzzleState state2) {
        Point empty1 = state1.getEmptyPosition();
        Point empty2 = state2.getEmptyPosition();

        int rowDiff = Math.abs(empty1.x - empty2.x);
        int colDiff = Math.abs(empty1.y - empty2.y);

        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
    }

    /**
     * Solve dengan DFS (menggunakan Stack)
     * Alternative algorithm untuk comparison
     */
    public List<PuzzleState> solveDFS(PuzzleState startState, int maxDepth) {
        if (!startState.isSolvable()) {
            return null;
        }

        Stack<PuzzleState> stack = new Stack<>();
        ArrayList<String> visited = new ArrayList<>();

        stack.push(startState);
        visited.add(startState.getStateKey());

        int iterations = 0;

        while (!stack.isEmpty() && iterations < maxIterations) {
            iterations++;
            PuzzleState current = stack.pop();

            // Cek depth limit
            int depth = getDepth(current);
            if (depth > maxDepth) {
                continue;
            }

            // Cek apakah sudah goal
            if (current.getStateKey().equals(goalState.getStateKey())) {
                return reconstructPath(current);
            }

            // Explore neighbors (reverse order untuk DFS)
            List<PuzzleState> neighbors = current.getNeighbors();
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                PuzzleState neighbor = neighbors.get(i);
                String neighborKey = neighbor.getStateKey();

                if (!containsKey(visited, neighborKey)) {
                    visited.add(neighborKey);
                    stack.push(neighbor);
                }
            }
        }

        return null;
    }

    /**
     * Get depth of state (jumlah parent sampai root)
     */
    private int getDepth(PuzzleState state) {
        int depth = 0;
        PuzzleState current = state;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }
}