package core;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("ALL")
public class SimpleSolver {

    private static List<PuzzleState> solvePuzzle(JFrame parent, PuzzleState currentState, PuzzleState goalState, String actionType) {
        if (parent == null || currentState == null || goalState == null) {
            System.err.println("[" + actionType + "] ERROR: Null parameters!");
            return null;
        }

        System.out.println("[" + actionType + "] Starting " + actionType.toLowerCase() + "...");
        System.out.println("[" + actionType + "] Current state key: " + currentState.getStateKey());
        System.out.println("[" + actionType + "] Goal state key: " + goalState.getStateKey());

        if (currentState.getStateKey().equals(goalState.getStateKey())) {
            System.out.println("[" + actionType + "] Puzzle already solved!");
            JOptionPane.showMessageDialog(parent,
                    "Puzzle is already solved!",
                    actionType,
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        parent.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        try {
            OptimizedPuzzleSolver solver = new OptimizedPuzzleSolver(goalState);
            solver.setMaxIterations(2000000);

            System.out.println("[" + actionType + "] Starting BFS solver...");
            List<PuzzleState> solution = solver.solveBFSOptimized(currentState);

            parent.setCursor(java.awt.Cursor.getDefaultCursor());

            if (solution == null) {
                System.out.println("[" + actionType + "] No solution found!");
                JOptionPane.showMessageDialog(parent,
                        """
                                Could not find solution!
                                This might be due to:
                                1. Puzzle is too complex
                                2. Max iterations reached
                                Try regenerating the puzzle.""",
                        actionType,
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return solution;

        } catch (Exception e) {
            parent.setCursor(java.awt.Cursor.getDefaultCursor());
            System.err.println("[" + actionType + "] Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Error during " + actionType.toLowerCase() + ":\n" + e.getMessage() + "\n\n" +
                            "Try:\n" +
                            "1. Generate a new puzzle\n" +
                            "2. Use a simpler difficulty level",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static void showHint(JFrame parent, PuzzleState currentState, PuzzleState goalState) {
        List<PuzzleState> solution = solvePuzzle(parent, currentState, goalState, "HINT");
        
        if (solution == null) {
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
    }

    public static void autoSolve(JFrame parent, PuzzleState currentState, PuzzleState goalState,
                                 AutoSolveCallback callback) {
        if (callback == null) {
            System.err.println("[AUTO SOLVE] ERROR: Callback is null!");
            return;
        }

        List<PuzzleState> solution = solvePuzzle(parent, currentState, goalState, "AUTO SOLVE");
        
        if (solution == null) {
            return;
        }

        System.out.println("[AUTO SOLVE] Solution found with " + (solution.size() - 1) + " moves");

        callback.onSolutionFound(solution);
    }

    public interface AutoSolveCallback {
        void onSolutionFound(List<PuzzleState> solution);
    }
}