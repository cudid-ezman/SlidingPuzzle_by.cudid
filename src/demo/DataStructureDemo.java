package demo;

import core.OptimizedPuzzleSolver;
import core.PuzzleState;
import datastructures.PuzzleGraph;
import datastructures.PuzzleTree;

import java.util.List;

/**
 * Demo class dengan visualisasi detail
 * Menunjukkan proses BFS/DFS dan struktur Graph/Tree
 */
public class DataStructureDemo {

    public static void main(String[] args) {
        System.out.println("╔" + repeat("═", 70) + "╗");
        System.out.println("║" + center("SLIDING PUZZLE - DATA STRUCTURE DEMO", 70) + "║");
        System.out.println("╚" + repeat("═", 70) + "╝\n");

        // Setup puzzle - lebih sederhana untuk demo
        int[][] startBoard = {
                {1, 2, 3},
                {4, 0, 5},
                {7, 8, 6}
        };

        int[][] goalBoard = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 0}
        };

        PuzzleState startState = new PuzzleState(startBoard, 3, 3);
        PuzzleState goalState = new PuzzleState(goalBoard, 3, 3);

        System.out.println("📋 Initial Configuration:");
        System.out.println(repeat("─", 70));
        System.out.println("Start State:");
        printBoardDemo(startBoard);
        System.out.println("\nGoal State:");
        printBoardDemo(goalBoard);
        System.out.println(repeat("─", 70) + "\n");

        // Demo menu
        demoMenu(startState, goalState);
    }

    private static void demoMenu(PuzzleState start, PuzzleState goal) {
        System.out.println("📚 Available Demos:");
        System.out.println("1. Tree Structure & Traversals");
        System.out.println("2. Graph Structure & Pathfinding");
        System.out.println("3. BFS vs DFS Comparison");
        System.out.println("4. Complete Solution Visualization");
        System.out.println("\n🚀 Running all demos...\n");

        pause();
        demoTreeStructure(start, goal);

        pause();
        demoGraphStructure(start, goal);

        pause();
        demoBFSvsDFS(start, goal);

        pause();
        demoCompleteSolution(start, goal);
    }

    /**
     * Demo Tree Structure dengan visualisasi
     */
    private static void demoTreeStructure(PuzzleState start, PuzzleState goal) {
        System.out.println("╔" + repeat("═", 70) + "╗");
        System.out.println("║" + center("DEMO 1: TREE STRUCTURE", 70) + "║");
        System.out.println("╚" + repeat("═", 70) + "╝\n");

        System.out.println("🌳 Building Solution Tree using BFS...\n");

        // Solve puzzle untuk dapat solution path
        OptimizedPuzzleSolver solver = new OptimizedPuzzleSolver(goal);
        solver.setVerboseMode(false); // Matikan verbose untuk demo

        System.out.println("📍 Solving puzzle...");
        List<PuzzleState> solution = solver.solveBFSOptimized(start);

        if (solution == null) {
            System.out.println("❌ No solution found!");
            return;
        }

        System.out.println("\n✅ Solution found! Building tree...\n");

        // Build tree dari solution
        PuzzleTree tree = new PuzzleTree(start);
        tree.buildFromSolutionPath(solution);

        System.out.println("📊 " + tree.getTreeStats());

        System.out.println("\n🛤️  Solution Path Visualization:");
        System.out.println(repeat("─", 70));

        List<PuzzleTree.TreeNode> path = tree.getRoot().getPathFromRoot();
        for (int i = 0; i < path.size(); i++) {
            PuzzleTree.TreeNode node = path.get(i);

            if (i == 0) {
                System.out.println("\n🏁 START");
            } else {
                System.out.println("\n➡️  " + node.getMoveFromParent() + " (Depth: " + node.getDepth() + ")");
            }

            printBoardDemo(node.getState().getBoard());

            if (i < path.size() - 1) {
                System.out.println("    ↓");
            }
        }

        System.out.println("\n🎯 GOAL REACHED!\n");

        // Demo Tree Traversals
        System.out.println("🔍 Tree Traversal Methods:");
        System.out.println(repeat("─", 70));
        System.out.println("├─ Pre-order nodes: " + tree.preOrderTraversal().size());
        System.out.println("├─ Post-order nodes: " + tree.postOrderTraversal().size());
        System.out.println("├─ Level-order levels: " + tree.levelOrderTraversal().size());
        System.out.println("└─ Leaf nodes: " + tree.getLeafNodes().size());
        System.out.println();
    }

    /**
     * Demo Graph Structure dengan visualisasi pathfinding
     */
    private static void demoGraphStructure(PuzzleState start, PuzzleState goal) {
        System.out.println("╔" + repeat("═", 70) + "╗");
        System.out.println("║" + center("DEMO 2: GRAPH STRUCTURE", 70) + "║");
        System.out.println("╚" + repeat("═", 70) + "╝\n");

        System.out.println("🕸️  Building Puzzle State Graph...\n");

        // Build graph dengan max depth 4
        PuzzleGraph graph = new PuzzleGraph();

        System.out.println("📊 Building graph (depth 4)...");
        graph.buildGraphBFS(start, 4);

        System.out.println("\n" + graph.getGraphStats());

        // Demo BFS path finding dengan visualisasi
        System.out.println("\n🔍 BFS Pathfinding:");
        System.out.println(repeat("─", 70));

        List<PuzzleState> bfsPath = graph.findShortestPathBFS(start, goal);
        if (bfsPath != null) {
            System.out.println("✅ BFS found path with " + (bfsPath.size() - 1) + " moves\n");

            for (int i = 0; i < bfsPath.size(); i++) {
                if (i == 0) {
                    System.out.println("🏁 START (Node " + i + ")");
                } else {
                    System.out.println("\n➡️  " + bfsPath.get(i).getMoveDescription() + " (Node " + i + ")");
                }
                printBoardDemo(bfsPath.get(i).getBoard());

                if (i < bfsPath.size() - 1) {
                    System.out.println("    ↓");
                }
            }
            System.out.println("\n🎯 GOAL!");
        } else {
            System.out.println("❌ Goal not reachable in graph (increase max depth)");
        }

        // Node degree analysis
        System.out.println("\n📈 Graph Analysis:");
        System.out.println(repeat("─", 70));
        PuzzleGraph.GraphNode startNode = graph.getNode(start.getStateKey());
        if (startNode != null) {
            System.out.println("├─ Start node degree: " + startNode.getNeighbors().size());
            System.out.println("└─ (Number of possible first moves)");
        }
        System.out.println();
    }

    /**
     * Demo BFS vs DFS dengan visualisasi
     */
    private static void demoBFSvsDFS(PuzzleState start, PuzzleState goal) {
        System.out.println("╔" + repeat("═", 70) + "╗");
        System.out.println("║" + center("DEMO 3: BFS vs DFS COMPARISON", 70) + "║");
        System.out.println("╚" + repeat("═", 70) + "╝\n");

        PuzzleGraph graph = new PuzzleGraph();
        graph.buildGraphBFS(start, 5);

        System.out.println("🔍 Running BFS...");
        long bfsStart = System.currentTimeMillis();
        List<PuzzleState> bfsPath = graph.findShortestPathBFS(start, goal);
        long bfsTime = System.currentTimeMillis() - bfsStart;

        System.out.println("🔍 Running DFS...");
        long dfsStart = System.currentTimeMillis();
        List<PuzzleState> dfsPath = graph.findPathDFS(start, goal);
        long dfsTime = System.currentTimeMillis() - dfsStart;

        System.out.println("\n📊 Comparison Results:");
        System.out.println(repeat("─", 70));
        System.out.println("│ Algorithm │ Path Length │ Time (ms) │ Optimal? │");
        System.out.println("├───────────┼─────────────┼───────────┼──────────┤");
        System.out.printf("│ BFS       │ %-11d │ %-9d │ %-8s │\n",
                bfsPath != null ? bfsPath.size() - 1 : -1,
                bfsTime,
                "✓ Yes");
        System.out.printf("│ DFS       │ %-11d │ %-9d │ %-8s │\n",
                dfsPath != null ? dfsPath.size() - 1 : -1,
                dfsTime,
                "✗ No");
        System.out.println("└───────────┴─────────────┴───────────┴──────────┘\n");

        System.out.println("💡 Key Insights:");
        System.out.println("  • BFS guarantees shortest path (optimal)");
        System.out.println("  • DFS is faster but may find longer path");
        System.out.println("  • For puzzles, BFS is preferred\n");
    }

    /**
     * Demo Complete Solution dengan step-by-step
     */
    private static void demoCompleteSolution(PuzzleState start, PuzzleState goal) {
        System.out.println("╔" + repeat("═", 70) + "╗");
        System.out.println("║" + center("DEMO 4: COMPLETE SOLUTION VISUALIZATION", 70) + "║");
        System.out.println("╚" + repeat("═", 70) + "╝\n");

        OptimizedPuzzleSolver solver = new OptimizedPuzzleSolver(goal);
        solver.setVerboseMode(true); // Enable detailed logging

        // Ini akan menampilkan proses pencarian detail
        List<PuzzleState> solution = solver.solveBFSOptimized(start);
    }

    // ========== HELPER METHODS ==========

    /**
     * Print board dengan format yang rapi
     */
    private static void printBoardDemo(int[][] board) {
        System.out.print("    ");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 0) {
                    System.out.print(" * ");
                } else {
                    System.out.printf("%2d ", board[i][j]);
                }
            }
            if (i < board.length - 1) {
                System.out.print("\n    ");
            }
        }
        System.out.println();
    }

    /**
     * Center text untuk header
     * Compatible dengan Java 8+
     */
    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }

        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();

        // Left padding
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }

        sb.append(text);

        // Right padding
        int rightPadding = width - text.length() - padding;
        for (int i = 0; i < rightPadding; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Create repeated string (replacement untuk String.repeat())
     * Compatible dengan Java 8+
     */
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Pause untuk user bisa baca
     */
    private static void pause() {
        System.out.println("\n" + repeat("─", 70));
        System.out.println("Press Enter to continue to next demo...");
        System.out.println(repeat("─", 70) + "\n");
        try {
            // Clear input buffer
            while (System.in.available() > 0) {
                System.in.read();
            }
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}