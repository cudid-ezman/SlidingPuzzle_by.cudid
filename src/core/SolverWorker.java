package core;

import javax.swing.*;
import java.util.List;

/**
 * Background worker untuk solving puzzle
 * Mencegah UI freeze saat computation berat
 */
public class SolverWorker extends SwingWorker<List<PuzzleState>, String> {

    private PuzzleState startState;
    private PuzzleState goalState;
    private OptimizedPuzzleSolver solver;
    private String taskType;
    private JDialog progressDialog;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JFrame parent;

    public SolverWorker(PuzzleState startState, PuzzleState goalState, String taskType, JFrame parent) {
        this.startState = startState;
        this.goalState = goalState;
        this.solver = new OptimizedPuzzleSolver(goalState);
        this.taskType = taskType;
        this.parent = parent;
    }

    /**
     * Buat dan tampilkan progress dialog
     */
    public void createProgressDialog() {
        progressDialog = new JDialog(parent, "Processing...", false); // NON-MODAL!
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(350, 150);
        progressDialog.setLocationRelativeTo(parent);
        progressDialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        progressLabel = new JLabel(taskType.equals("solve") ?
                "Finding solution path..." : "Calculating hint...");
        progressLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);

        JLabel tipLabel = new JLabel("Please wait, this may take a moment...");
        tipLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        tipLabel.setFont(tipLabel.getFont().deriveFont(10f));

        panel.add(progressLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(tipLabel);

        progressDialog.add(panel);
    }

    /**
     * Background computation
     */
    @Override
    protected List<PuzzleState> doInBackground() throws Exception {
        System.out.println("[WORKER] Starting background computation...");
        publish("Starting optimized BFS algorithm...");

        // Matikan verbose untuk hint/auto solve (terlalu banyak output)
        solver.setVerboseMode(false);

        // Solve menggunakan OPTIMIZED BFS
        List<PuzzleState> solution = solver.solveBFSOptimized(startState);

        if (solution != null) {
            publish("Solution found! Path length: " + (solution.size() - 1));
            System.out.println("[WORKER] Solution found with " + (solution.size() - 1) + " moves");
        } else {
            publish("No solution found!");
            System.out.println("[WORKER] No solution found!");
        }

        return solution;
    }

    /**
     * Update progress (called by publish())
     */
    @Override
    protected void process(List<String> chunks) {
        if (progressLabel != null && !chunks.isEmpty()) {
            String latestMessage = chunks.get(chunks.size() - 1);
            progressLabel.setText(latestMessage);

            // Print ke console juga
            System.out.println("[SOLVER] " + latestMessage);
        }
    }

    /**
     * Selesai computation
     */
    @Override
    protected void done() {
        System.out.println("[WORKER] Computation done!");
        if (progressDialog != null) {
            progressDialog.setVisible(false);
            progressDialog.dispose();
        }
    }

    /**
     * Get progress dialog
     */
    public JDialog getProgressDialog() {
        return progressDialog;
    }
}