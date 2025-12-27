package core;

import javax.swing.*;
import java.util.List;

/**
 * Simple solver tanpa background worker (untuk debugging)
 * FIXED: Better error handling & null checks
 */
public class SimpleSolver {

    /**
     * Show hint dengan simple approach
     */
    public static void showHint(JFrame parent, PuzzleState currentState, PuzzleState goalState) {
        if (parent == null || currentState == null || goalState == null) {
            System.err.println("[HINT] ERROR: Null parameters!");
            return;
        }

        System.out.println("[HINT] Starting hint calculation...");
        System.out.println("[HINT] Current state key: " + currentState.getStateKey());
        System.out.println("[HINT] Goal state key: " + goalState.getStateKey());

        // Check if already solved
        if (currentState.getStateKey().equals(goalState.getStateKey())) {
            System.out.println("[HINT] Puzzle already solved!");
            JOptionPane.showMessageDialog(parent,
                    "Puzzle is already solved!",
                    "Hint",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show waiting cursor
        parent.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        try {
            OptimizedPuzzleSolver solver = new OptimizedPuzzleSolver(goalState);
            solver.setVerboseMode(true); // Enable untuk lihat proses!
            solver.setMaxIterations(2000000); // 2 million for harder puzzles!

            System.out.println("[HINT] Starting BFS solver...");
            List<PuzzleState> solution = solver.solveBFSOptimized(currentState);

            // Restore cursor
            parent.setCursor(java.awt.Cursor.getDefaultCursor());

            if (solution == null) {
                System.out.println("[HINT] No solution found!");
                JOptionPane.showMessageDialog(parent,
                        "Could not find solution!\n" +
                                "This might be due to:\n" +
                                "1. Puzzle is too complex\n" +
                                "2. Max iterations reached\n" +
                                "Try regenerating the puzzle.",
                        "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (solution.size() <= 1) {
                System.out.println("[HINT] Already at goal or only 1 state!");
                JOptionPane.showMessageDialog(parent,
                        "Puzzle is already solved or very close!",
                        "Hint",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            PuzzleState nextMove = solution.get(1);
            String hint = nextMove.getMoveDescription();
            if (hint == null || hint.isEmpty()) {
                hint = "Move the empty tile";
            }

            int minMoves = solution.size() - 1;

            System.out.println("[HINT] Next move: " + hint);
            System.out.println("[HINT] Minimum moves: " + minMoves);

            JOptionPane.showMessageDialog(parent,
                    "Next Move: " + hint + "\n" +
                            "Minimum moves to solve: " + minMoves,
                    "Hint",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            parent.setCursor(java.awt.Cursor.getDefaultCursor());
            System.err.println("[HINT] Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Error calculating hint:\n" + e.getMessage() + "\n\n" +
                            "Try:\n" +
                            "1. Generate a new puzzle\n" +
                            "2. Use a simpler difficulty level",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Auto solve dengan simple approach
     */
    public static void autoSolve(JFrame parent, PuzzleState currentState, PuzzleState goalState,
                                 AutoSolveCallback callback) {
        if (parent == null || currentState == null || goalState == null || callback == null) {
            System.err.println("[AUTO SOLVE] ERROR: Null parameters!");
            return;
        }

        System.out.println("[AUTO SOLVE] Starting auto solve...");
        System.out.println("[AUTO SOLVE] Current state key: " + currentState.getStateKey());
        System.out.println("[AUTO SOLVE] Goal state key: " + goalState.getStateKey());

        // Check if already solved
        if (currentState.getStateKey().equals(goalState.getStateKey())) {
            System.out.println("[AUTO SOLVE] Puzzle already solved!");
            JOptionPane.showMessageDialog(parent,
                    "Puzzle is already solved!",
                    "Auto Solve",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show waiting cursor
        parent.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        try {
            OptimizedPuzzleSolver solver = new OptimizedPuzzleSolver(goalState);
            solver.setVerboseMode(true); // Enable untuk lihat proses!
            solver.setMaxIterations(2000000); // 2 million for harder puzzles!

            System.out.println("[AUTO SOLVE] Starting BFS solver...");
            List<PuzzleState> solution = solver.solveBFSOptimized(currentState);

            // Restore cursor
            parent.setCursor(java.awt.Cursor.getDefaultCursor());

            if (solution == null) {
                System.out.println("[AUTO SOLVE] No solution found!");
                JOptionPane.showMessageDialog(parent,
                        "Could not find solution!\n" +
                                "This might be due to:\n" +
                                "1. Puzzle is too complex\n" +
                                "2. Max iterations reached (1,000,000)\n" +
                                "3. Puzzle might not be solvable\n\n" +
                                "Try generating a new puzzle or use easier difficulty.",
                        "Auto Solve",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("[AUTO SOLVE] Solution found with " + (solution.size() - 1) + " moves");

            // Call callback to animate
            callback.onSolutionFound(solution);

        } catch (Exception e) {
            parent.setCursor(java.awt.Cursor.getDefaultCursor());
            System.err.println("[AUTO SOLVE] Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Error solving puzzle:\n" + e.getMessage() + "\n\n" +
                            "Try:\n" +
                            "1. Generate a new puzzle\n" +
                            "2. Use a simpler difficulty level\n" +
                            "3. Check console for details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Callback interface untuk auto solve
     */
    public interface AutoSolveCallback {
        void onSolutionFound(List<PuzzleState> solution);
    }
}