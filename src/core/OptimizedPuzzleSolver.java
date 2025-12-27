package core;

import java.util.*;

/**
 * Optimized Puzzle Solver dengan Visualisasi Detail
 * Menampilkan proses pencarian di console
 */
public class OptimizedPuzzleSolver {

    private final PuzzleState goalState;
    private int maxIterations;
    private boolean verboseMode; // Mode detail untuk debugging

    public OptimizedPuzzleSolver(PuzzleState goalState) {
        this.goalState = goalState;
        this.maxIterations = 2000000; // Increase to 2 million for 4x4!
        this.verboseMode = true; // Enable verbose by default
    }

    public void setVerboseMode(boolean verbose) {
        this.verboseMode = verbose;
    }

    public void setMaxIterations(int max) {
        this.maxIterations = max;
    }

    /**
     * Solve dengan visualisasi detail proses BFS
     */
    public List<PuzzleState> solveBFSOptimized(PuzzleState startState) {
        if (!startState.isSolvable()) {
            System.out.println("[OPTIMIZED BFS] ❌ Puzzle is not solvable!");
            return null;
        }

        System.out.println("\n" + repeat("=", 60));
        System.out.println("🔍 STARTING BFS SEARCH");
        System.out.println(repeat("=", 60));
        System.out.println("📊 Start State:");
        printBoard(startState.getBoard());
        System.out.println("🎯 Goal State:");
        printBoard(goalState.getBoard());
        System.out.println("⚙️  Max Iterations: " + maxIterations);
        System.out.println(repeat("=", 60) + "\n");

        long startTime = System.currentTimeMillis();

        Queue<PuzzleState> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        queue.add(startState);
        visited.add(startState.getStateKey());

        int iterations = 0;
        int maxQueueSize = 0;
        int lastDisplayedDepth = -1;

        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;
            maxQueueSize = Math.max(maxQueueSize, queue.size());

            PuzzleState current = queue.poll();
            int currentDepth = getDepth(current);

            // Verbose logging dengan visualisasi board
            if (verboseMode) {
                // Tampilkan setiap depth baru yang dieksplorasi
                if (currentDepth > lastDisplayedDepth) {
                    System.out.println("\n" + repeat("─", 60));
                    System.out.println("📍 Exploring Depth: " + currentDepth);
                    System.out.println("├─ Iterations so far: " + iterations);
                    System.out.println("├─ Queue size: " + queue.size());
                    System.out.println("└─ Visited states: " + visited.size());
                    lastDisplayedDepth = currentDepth;
                }

                // Tampilkan beberapa state pertama di setiap depth
                if (iterations % 100 == 0) {
                    System.out.println("\n🔹 Iteration " + iterations + " (Depth " + currentDepth + "):");
                    System.out.println("Exploring state:");
                    printBoardCompact(current.getBoard());
                    System.out.println("├─ Move from parent: " +
                            (current.getParent() != null ? current.getMoveDescription() : "START"));
                    System.out.println("└─ Queue size: " + queue.size() + " | Visited: " + visited.size());
                }
            } else {
                // Non-verbose: hanya tampilkan milestone
                if (iterations % 10000 == 0) {
                    System.out.println("\n📊 Milestone: " + iterations + " iterations");
                    System.out.println("├─ Current depth: " + currentDepth);
                    System.out.println("├─ Queue size: " + queue.size());
                    System.out.println("└─ Visited: " + visited.size());
                }
            }

            // Cek apakah sudah goal
            if (current.getStateKey().equals(goalState.getStateKey())) {
                long elapsed = System.currentTimeMillis() - startTime;
                List<PuzzleState> solution = reconstructPath(current);

                System.out.println("\n" + repeat("=", 60));
                System.out.println("✅ SOLUTION FOUND!");
                System.out.println(repeat("=", 60));
                System.out.println("📊 Statistics:");
                System.out.println("├─ Total Iterations: " + iterations);
                System.out.println("├─ Solution Depth: " + currentDepth + " moves");
                System.out.println("├─ Solution Length: " + (solution.size() - 1) + " moves");
                System.out.println("├─ States Explored: " + visited.size());
                System.out.println("├─ Max Queue Size: " + maxQueueSize);
                System.out.println("├─ Time Elapsed: " + elapsed + "ms");

                // FIXED: Cek division by zero
                if (elapsed > 0) {
                    long speed = (iterations * 1000L) / elapsed;
                    System.out.println("└─ Avg Speed: " + speed + " states/sec");
                } else {
                    System.out.println("└─ Avg Speed: Very fast!");
                }

                System.out.println(repeat("=", 60));

                // Print solution path
                printSolutionPath(solution);

                return solution;
            }

            // Explore neighbors
            List<PuzzleState> neighbors = current.getNeighbors();

            for (PuzzleState neighbor : neighbors) {
                String neighborKey = neighbor.getStateKey();

                if (!visited.contains(neighborKey)) {
                    visited.add(neighborKey);
                    queue.add(neighbor);

                    // Verbose: tampilkan neighbor yang di-add
                    if (verboseMode && iterations % 500 == 0 && visited.size() < 1000) {
                        System.out.println("  └─ Added neighbor: " + neighbor.getMoveDescription());
                    }
                }
            }
        }

        // Jika tidak ketemu
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("\n" + repeat("=", 60));
        System.out.println("❌ NO SOLUTION FOUND");
        System.out.println(repeat("=", 60));
        System.out.println("📊 Statistics:");
        System.out.println("├─ Total Iterations: " + iterations);
        System.out.println("├─ Max Depth Reached: " + getDepth(queue.isEmpty() ? startState : queue.peek()));
        System.out.println("├─ States Explored: " + visited.size());
        System.out.println("├─ Max Queue Size: " + maxQueueSize);
        System.out.println("├─ Time Elapsed: " + elapsed + "ms");
        System.out.println("└─ Reason: " + (iterations >= maxIterations ?
                "Max iterations reached" : "Queue exhausted"));
        System.out.println(repeat("=", 60));
        System.out.println("💡 Tip: Try increasing maxIterations or regenerate puzzle");
        System.out.println();

        return null;
    }

    /**
     * Print board dengan format yang rapi
     */
    private void printBoard(int[][] board) {
        String topBottom = repeat("─", board[0].length * 4 - 1);
        System.out.println("   ┌" + topBottom + "┐");
        for (int[] ints : board) {
            System.out.print("   │");
            for (int j = 0; j < ints.length; j++) {
                if (ints[j] == 0) {
                    System.out.print(" * ");
                } else {
                    System.out.printf("%2d ", ints[j]);
                }
                if (j < ints.length - 1) System.out.print(" ");
            }
            System.out.println("│");
        }
        System.out.println("   └" + topBottom + "┘");
    }

    /**
     * Print board dengan indentasi
     */
    private void printBoardIndented(int[][] board, String indent) {
        for (int[] ints : board) {
            System.out.print(indent);
            for (int anInt : ints) {
                if (anInt == 0) {
                    System.out.print(" * ");
                } else {
                    System.out.printf("%2d ", anInt);
                }
            }
            System.out.println();
        }
    }

    /**
     * Print board compact (inline)
     */
    private void printBoardCompact(int[][] board) {
        System.out.print("   [");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 0) {
                    System.out.print(" *");
                } else {
                    System.out.printf("%2d", board[i][j]);
                }
                if (i < board.length - 1 || j < board[i].length - 1) {
                    System.out.print(" ");
                }
            }
            if (i < board.length - 1) {
                System.out.print(" | ");
            }
        }
        System.out.println("]");
    }

    /**
     * Print solution path dengan detail
     */
    private void printSolutionPath(List<PuzzleState> solution) {
        System.out.println("\n🛤️  SOLUTION PATH:");
        System.out.println(repeat("=", 60));

        for (int i = 0; i < solution.size(); i++) {
            PuzzleState state = solution.get(i);

            if (i == 0) {
                System.out.println("\n📍 START (Step 0)");
            } else {
                System.out.println("\n📍 Step " + i + ": " + state.getMoveDescription());
            }

            printBoard(state.getBoard());

            if (i < solution.size() - 1) {
                System.out.println("   ↓");
            }
        }

        System.out.println("\n🎯 GOAL REACHED!");
        System.out.println(repeat("=", 60) + "\n");
    }

    /**
     * Helper: Create repeated string (Java 8 compatible)
     */
    private String repeat(String str, int count) {
        return String.valueOf(str).repeat(Math.max(0, count));
    }

    /**
     * Get depth dari state (jumlah moves dari start)
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

    /**
     * Rekonstruksi path
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
     * Get minimum moves
     */
    public int getMinimumMoves(PuzzleState startState) {
        List<PuzzleState> solution = solveBFSOptimized(startState);
        if (solution == null) return -1;
        return solution.size() - 1;
    }
}