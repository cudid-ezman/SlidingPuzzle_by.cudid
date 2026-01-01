package app;

import util.MoveHistory;
import util.PuzzleTree;
import util.HandlePuzzleSolver;
import model.GameLevel;
import model.Button;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("ALL")
public class SlidingPuzzle extends JFrame {

    private JPanel gridPanel;
    private JPanel controlPanel;
    private JPanel infoPanel;
    private Button[][] buttons;

    private GameLevel gameLevel;
    private PuzzleTree stateSekarang;
    private PuzzleTree stateTujuan;
    private MoveHistory moveHistory;

    private JLabel levelLabel;
    private JLabel movesLabel;
    private JLabel timeLabel;
    private Timer gameTimer;

    private BufferedImage puzzleImage;
    private boolean useImage = false;
    private BufferedImage currentOriginalImage = null;

    private static final int TILE_SIZE = 80;
    private static final Color BG_COLOR = new Color(245, 245, 245);

    public SlidingPuzzle() {
        inisialisasiGame(GameLevel.Difficulty.EASY);
    }

    private void inisialisasiGame(GameLevel.Difficulty difficulty) {

        gameLevel = new GameLevel(difficulty);
        moveHistory = new MoveHistory(100);

        stateTujuan = gameLevel.stateTujuan();
        stateSekarang = gameLevel.StateAwal();

        if (stateSekarang == null || stateTujuan == null) {
            return;
        }
        System.out.println("[INIT] Goal state: " + stateTujuan.getStateKey());
        System.out.println("[INIT] Start state: " + stateSekarang.getStateKey());
        System.out.println("[INIT] States are different: " + !stateSekarang.getStateKey().equals(stateTujuan.getStateKey()));

        setupUI();

        if (currentOriginalImage != null) {
            applyImageToCurrentLevel();
        }

        updateBoard();
        startGameTimer();
    }

    private void setupUI() {
        setTitle("Sliding Puzzle Game UAS PM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);

        try {
            BufferedImage icon = ImageIO.read(new File("logo.png"));
            setIconImage(icon);
        } catch (IOException e) {
            System.out.println("Warning: logo.png not found for app icon.");
        }

        System.out.println("[UI] Setting up UI for " + gameLevel.getLevelInfo());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        createGridPanel();
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        updateInfo();
        System.out.println("[UI] UI setup complete");
    }

    private void createInfoPanel() {
        infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(BG_COLOR);

        levelLabel = new JLabel("Level: Loading...", SwingConstants.CENTER);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));

        movesLabel = new JLabel("Moves: 0", SwingConstants.CENTER);
        movesLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        timeLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        infoPanel.add(levelLabel);
        infoPanel.add(movesLabel);
        infoPanel.add(timeLabel);
    }

    private void createGridPanel() {
        int rows = gameLevel.getCurrentDifficulty().rows();
        int cols = gameLevel.getCurrentDifficulty().cols();

        gridPanel = new JPanel(new GridLayout(rows, cols, 3, 3));
        gridPanel.setBackground(Color.DARK_GRAY);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 5));

        buttons = new Button[rows][cols];

        Dimension buttonSize = new Dimension(TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = stateSekarang.ambilNilaidi(i, j);

                Button btn = new Button(value);
                btn.setPreferredSize(buttonSize);

                final int row = i;
                final int col = j;

                btn.addActionListener(e -> handleTileClick(row, col));

                buttons[i][j] = btn;
                gridPanel.add(btn);
            }
        }
    }

    private void createControlPanel() {
        controlPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        controlPanel.setBackground(BG_COLOR);

        JButton newGameBtn = createButton("New Game", new Color(52, 152, 219));
        JButton undoBtn = createButton("Undo", new Color(155, 89, 182));
        JButton hintBtn = createButton("Petunjuk", new Color(241, 196, 15));

        JButton easyBtn = createButton("3x3", new Color(46, 204, 113));
        JButton mediumBtn = createButton("4x3", new Color(230, 126, 34));
        JButton hardBtn = createButton("4x4", new Color(231, 76, 60));

        JButton autoSolveBtn = createButton("Auto Solve", new Color(52, 73, 94));
        JButton loadImageBtn = createButton("Load Image", new Color(26, 188, 156));
        JButton exitBtn = createButton("Exit", new Color(189, 195, 199));


        newGameBtn.addActionListener(e -> newGame());
        undoBtn.addActionListener(e -> undoMove());
        hintBtn.addActionListener(e -> showHint());

        easyBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.EASY));
        mediumBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.MEDIUM));
        hardBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.HARD));

        autoSolveBtn.addActionListener(e -> autoSolve());
        loadImageBtn.addActionListener(e -> loadImage());
        exitBtn.addActionListener(e -> System.exit(0));

        controlPanel.add(newGameBtn);
        controlPanel.add(undoBtn);
        controlPanel.add(hintBtn);
        controlPanel.add(easyBtn);
        controlPanel.add(mediumBtn);
        controlPanel.add(hardBtn);
        controlPanel.add(autoSolveBtn);
        controlPanel.add(loadImageBtn);
        controlPanel.add(exitBtn);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        return btn;
    }

    private void handleTileClick(int row, int col) {
        Point emptyPos = stateSekarang.getEmptyPosition();
        int emptyRow = emptyPos.x;
        int emptyCol = emptyPos.y;

        boolean canMove = (Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow);

        if (!canMove) {
            return;
        }

        moveHistory.push(stateSekarang);

        int[][] newBoard = copyBoard(stateSekarang.getBoard());
        newBoard[emptyRow][emptyCol] = newBoard[row][col];
        newBoard[row][col] = 0;

        stateSekarang = new PuzzleTree(newBoard,
                gameLevel.getCurrentDifficulty().rows(),
                gameLevel.getCurrentDifficulty().cols());

        gameLevel.tambahLangkah();
        updateBoard();
        updateInfo();

        checkWin();
    }

    private void updateBoard() {
        int rows = gameLevel.getCurrentDifficulty().rows();
        int cols = gameLevel.getCurrentDifficulty().cols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = stateSekarang.ambilNilaidi(i, j);

                if (useImage && puzzleImage != null) {
                    if (value == 0) {
                        buttons[i][j].makeEmpty();
                    } else {
                        Image tileImage = createTileImage(value - 1);
                        buttons[i][j].setValueWithImage(value, tileImage);
                    }
                } else {
                    buttons[i][j].setValue(value, value == 0 ? "" : String.valueOf(value));
                }
            }
        }
    }

    private void updateInfo() {
        levelLabel.setText(gameLevel.getLevelInfo());
        movesLabel.setText(gameLevel.getScoreInfo());
        gameLevel.updateElapsedTime();
        timeLabel.setText("Time: " + gameLevel.ambilWaktu());
    }

    private void checkWin() {
        if (stateSekarang.getStateKey().equals(stateTujuan.getStateKey())) {
            gameTimer.stop();
            gameLevel.updateBestScore();

            int option = JOptionPane.showConfirmDialog(this,
                    "Selamat! Puzzle selesai!\n" +
                            "Moves: " + gameLevel.getTotalMoves() + "\n" +
                            "Time: " + gameLevel.ambilWaktu() + "\n\n" +
                            "Main level berikutnya?",
                    "Puzzle Completed!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                gameLevel.nextLevel();
                newGame();
            }
        }
    }

    private void newGame() {
        System.out.println("[NEW GAME] Starting new game...");

        moveHistory.clear();
        stateSekarang = gameLevel.StateAwal();
        gameLevel.resetMoves();

        System.out.println("[NEW GAME] New state: " + stateSekarang.getStateKey());
        System.out.println("[NEW GAME] Goal state: " + stateTujuan.getStateKey());
        System.out.println("[NEW GAME] State generated, updating board...");

        updateBoard();
        updateInfo();
        startGameTimer();

        System.out.println("[NEW GAME] Game started!");
    }

    private void undoMove() {
        if (moveHistory.canUndo()) {
            stateSekarang = moveHistory.pop();
            updateBoard();
            updateInfo();
        } else {
            JOptionPane.showMessageDialog(this, "No moves to undo!", "Undo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showHint() {
        System.out.println("[HINT] Starting hint calculation...");
        System.out.println("[HINT] Current state: " + stateSekarang.getStateKey());
        System.out.println("[HINT] Goal state: " + stateTujuan.getStateKey());

        HandlePuzzleSolver.showHint(this, stateSekarang, stateTujuan);
    }

    private void autoSolve() {
    System.out.println("[AUTO SOLVE] Request received.");

    if (stateSekarang.getStateKey().equals(stateTujuan.getStateKey())) {
        JOptionPane.showMessageDialog(this,
            "Puzzle sudah selesai! Tidak perlu di-solve lagi.",
            "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

        SwingWorker<java.util.List<PuzzleTree>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<PuzzleTree> doInBackground() {
                System.out.println("[AUTO SOLVE WORKER] Running solver in background...");
                final AtomicReference<java.util.List<PuzzleTree>> result = new AtomicReference<>(null);

                HandlePuzzleSolver.autoSolve(SlidingPuzzle.this, stateSekarang, stateTujuan, solution -> {
                    System.out.println("[AUTO SOLVE WORKER] Solution received!");
                    result.set(solution);
                });

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                return result.get();
            }

            @Override
            protected void done() {
                try {
                    java.util.List<PuzzleTree> solution = get();

                    if (solution == null || solution.isEmpty()) {
                        System.out.println("[AUTO SOLVE] No solution found!");
                        return;
                    }

                    System.out.println("[AUTO SOLVE] Solution received with " + solution.size() + " states");
                    System.out.println("[AUTO SOLVE] Starting animation on EDT...");

                    gameTimer.stop();

                    // Start animation
                    animasiSolve(solution);

                } catch (Exception ex) {
                    System.err.println("[AUTO SOLVE] Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void animasiSolve(java.util.List<PuzzleTree> solution) {
         System.out.println("[ANIMATE] Starting animation with " + solution.size() + " states");

        final int totalSteps = solution.size() - 1;
        final int[] currentStep = {0};

        Timer animationTimer = new Timer(500, null);

        animationTimer.addActionListener(e -> {
            currentStep[0]++;

            if (currentStep[0] < solution.size()) {
                System.out.println("[ANIMATE] Step " + currentStep[0] + "/" + totalSteps);

                PuzzleTree nextState = solution.get(currentStep[0]);
                System.out.println("[ANIMATE] Applying: " + nextState.getStateKey());
                System.out.println("[ANIMATE] Move: " + nextState.getDeskripsiMove());

                stateSekarang = nextState;

                SwingUtilities.invokeLater(() -> {
                    updateBoard();
                    gridPanel.revalidate();
                    gridPanel.repaint();
                    System.out.println("[ANIMATE] UI updated for step " + currentStep[0]);
                });

            } else {
                System.out.println("[ANIMATE] Animation complete!");
                animationTimer.stop();

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(SlidingPuzzle.this,
                            "Auto solve complete!\nPuzzle solved in " + totalSteps + " moves.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    checkWin();
                });
            }
        });

        System.out.println("[ANIMATE] Starting timer...");
        animationTimer.start();
        System.out.println("[ANIMATE] Timer running: " + animationTimer.isRunning());
    }

    private void changeDifficulty(GameLevel.Difficulty newDifficulty) {
        System.out.println("[LEVEL] Changing difficulty to: " + newDifficulty.name());

        if (newDifficulty.equals(gameLevel.getCurrentDifficulty())) {
            System.out.println("[LEVEL] Same difficulty, starting new game...");
            newGame();
            return;
        }

        if (gameTimer != null) {
            gameTimer.stop();
            System.out.println("[LEVEL] Timer stopped");
        }

        System.out.println("[LEVEL] Rebuilding UI...");

        getContentPane().removeAll();
        inisialisasiGame(newDifficulty);
        revalidate();
        repaint();
        setVisible(true);

        System.out.println("[LEVEL] Level changed successfully!");
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                currentOriginalImage = ImageIO.read(file);
                applyImageToCurrentLevel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to load image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private void applyImageToCurrentLevel() {
        if (currentOriginalImage == null) return;

        int rows = gameLevel.getCurrentDifficulty().rows();
        int cols = gameLevel.getCurrentDifficulty().cols();

        int targetWidth = cols * TILE_SIZE;
        int targetHeight = rows * TILE_SIZE;

        puzzleImage = resizeImage(currentOriginalImage, targetWidth, targetHeight);
        useImage = true;
        updateBoard();
    }

    private Image createTileImage(int tileIndex) {
        int rows = gameLevel.getCurrentDifficulty().rows();
        int cols = gameLevel.getCurrentDifficulty().cols();

        int row = tileIndex / cols;
        int col = tileIndex % cols;

        int tileWidth = puzzleImage.getWidth() / cols;
        int tileHeight = puzzleImage.getHeight() / rows;

        return createImage(new FilteredImageSource(puzzleImage.getSource(),
                new CropImageFilter(col * tileWidth, row * tileHeight, tileWidth, tileHeight)));
    }

    private void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        System.out.println("[TIMER] Starting game timer...");

        gameTimer = new Timer(1000, e -> {
            gameLevel.updateElapsedTime();
            timeLabel.setText("Time: " + gameLevel.ambilWaktu());
        });
        gameTimer.start();
    }

    private int[][] copyBoard(int[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        int[][] copy = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, cols);
        }

        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SlidingPuzzle game = new SlidingPuzzle();
            game.setVisible(true);
        });
    }
}