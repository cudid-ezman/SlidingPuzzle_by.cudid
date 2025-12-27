package App;

import core.*;
import game.GameLevel;
import game.PuzzleButton;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Main class untuk Sliding Puzzle Game
 * Game dengan multiple level dan fitur lengkap
 */
public class PuzzleGame extends JFrame {

    private JPanel gridPanel;
    private JPanel controlPanel;
    private JPanel infoPanel;
    private PuzzleButton[][] buttons;

    private GameLevel gameLevel;
    private PuzzleState currentState;
    private PuzzleState goalState;
    private MoveHistory moveHistory;

    private JLabel levelLabel;
    private JLabel movesLabel;
    private JLabel timeLabel;
    private JLabel hintLabel;
    private Timer gameTimer;

    private BufferedImage puzzleImage;
    private boolean useImage = false;

    private static final int TILE_SIZE = 80;
    private static final Color BG_COLOR = new Color(245, 245, 245);

    public PuzzleGame() {
        initializeGame(GameLevel.Difficulty.EASY);
    }

    private void initializeGame(GameLevel.Difficulty difficulty) {
        // Initialize game components
        gameLevel = new GameLevel(difficulty);
        moveHistory = new MoveHistory(100);

        // Generate initial and goal states
        currentState = gameLevel.generateInitialState();
        goalState = gameLevel.generateGoalState();

        // Initialize both solvers
        // Regular solver
        PuzzleSolver solver = new PuzzleSolver(goalState);
        // Fast solver
        OptimizedPuzzleSolver optimizedSolver = new OptimizedPuzzleSolver(goalState);

        // Setup UI
        setupUI();
        updateBoard();
        startGameTimer();
    }

    private void setupUI() {
        setTitle("Sliding Puzzle Game - Multi Level");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);

        System.out.println("[UI] Setting up UI for " + gameLevel.getLevelInfo());

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Info Panel (Top)
        createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Grid Panel (Center)
        createGridPanel();
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // Control Panel (Bottom)
        createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // Update info setelah UI ready
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
        int rows = gameLevel.getCurrentDifficulty().getRows();
        int cols = gameLevel.getCurrentDifficulty().getCols();

        gridPanel = new JPanel(new GridLayout(rows, cols, 3, 3));
        gridPanel.setBackground(Color.DARK_GRAY);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 5));

        buttons = new PuzzleButton[rows][cols];

        Dimension buttonSize = new Dimension(TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = currentState.getValueAt(i, j);
                Point position = new Point(i, j);

                // Cari original position dari value ini
                Point originalPosition = findOriginalPosition(value);

                PuzzleButton btn = new PuzzleButton(value, position, originalPosition);
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

        // Row 1: Game controls
        JButton newGameBtn = createButton("New Game", new Color(52, 152, 219));
        JButton undoBtn = createButton("Undo", new Color(155, 89, 182));
        JButton hintBtn = createButton("Hint", new Color(241, 196, 15));

        // Row 2: Difficulty selection
        JButton easyBtn = createButton("Easy 3x3", new Color(46, 204, 113));
        JButton mediumBtn = createButton("Medium 4x3", new Color(230, 126, 34));
        JButton hardBtn = createButton("Hard 4x4", new Color(231, 76, 60));

        // Row 3: Special features
        JButton autoSolveBtn = createButton("Auto Solve", new Color(52, 73, 94));
        JButton loadImageBtn = createButton("Load Image", new Color(26, 188, 156));
        JButton exitBtn = createButton("Exit", new Color(189, 195, 199));

        // Add action listeners
        newGameBtn.addActionListener(e -> newGame());
        undoBtn.addActionListener(e -> undoMove());
        hintBtn.addActionListener(e -> showHint());

        easyBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.EASY));
        mediumBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.MEDIUM));
        hardBtn.addActionListener(e -> changeDifficulty(GameLevel.Difficulty.HARD));

        autoSolveBtn.addActionListener(e -> autoSolve());
        loadImageBtn.addActionListener(e -> loadImage());
        exitBtn.addActionListener(e -> System.exit(0));

        // Add buttons to panel
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
        Point emptyPos = currentState.getEmptyPosition();
        int emptyRow = emptyPos.x;
        int emptyCol = emptyPos.y;

        // Check if clicked tile is adjacent to empty
        boolean canMove = (Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow);

        if (!canMove) {
            return;
        }

        // Save current state to history
        moveHistory.push(currentState);

        // Create new state with swapped tiles
        int[][] newBoard = copyBoard(currentState.getBoard());
        newBoard[emptyRow][emptyCol] = newBoard[row][col];
        newBoard[row][col] = 0;

        currentState = new PuzzleState(newBoard,
                gameLevel.getCurrentDifficulty().getRows(),
                gameLevel.getCurrentDifficulty().getCols());

        // Update game state
        gameLevel.incrementMoves();
        updateBoard();
        updateInfo();

        // Check if won
        checkWin();
    }

    private void updateBoard() {
        int rows = gameLevel.getCurrentDifficulty().getRows();
        int cols = gameLevel.getCurrentDifficulty().getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = currentState.getValueAt(i, j);

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

                buttons[i][j].setPosition(new Point(i, j));
            }
        }
    }

    private void updateInfo() {
        levelLabel.setText(gameLevel.getLevelInfo());
        movesLabel.setText(gameLevel.getScoreInfo());
        gameLevel.updateElapsedTime();
        timeLabel.setText("Time: " + gameLevel.getFormattedTime());
    }

    private void checkWin() {
        if (currentState.getStateKey().equals(goalState.getStateKey())) {
            gameTimer.stop();
            gameLevel.updateBestScore();

            int option = JOptionPane.showConfirmDialog(this,
                    "Selamat! Puzzle selesai!\n" +
                            "Moves: " + gameLevel.getTotalMoves() + "\n" +
                            "Time: " + gameLevel.getFormattedTime() + "\n\n" +
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
        currentState = gameLevel.generateInitialState();
        gameLevel.resetMoves();

        System.out.println("[NEW GAME] State generated, updating board...");
        updateBoard();
        updateInfo();
        startGameTimer();

        System.out.println("[NEW GAME] Game started!");
    }

    private void undoMove() {
        if (moveHistory.canUndo()) {
            currentState = moveHistory.pop();
            updateBoard();
            // Don't decrement moves for undo
            updateInfo();
        } else {
            JOptionPane.showMessageDialog(this, "No moves to undo!", "Undo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showHint() {
        System.out.println("[HINT] Starting hint calculation...");

        // Use simple solver (more reliable)
        SimpleSolver.showHint(this, currentState, goalState);
    }

    private void autoSolve() {
        System.out.println("[AUTO SOLVE] Starting auto solve...");

        // Use simple solver with callback
        SimpleSolver.autoSolve(this, currentState, goalState, solution -> {
            System.out.println("[AUTO SOLVE] Starting animation...");
            gameTimer.stop();

            Timer solveTimer = new Timer(300, null);
            final int[] index = {1};

            solveTimer.addActionListener(e -> {
                if (index[0] < solution.size()) {
                    currentState = solution.get(index[0]);
                    updateBoard();
                    System.out.println("[AUTO SOLVE] Step " + index[0] + "/" + (solution.size() - 1));
                    index[0]++;
                } else {
                    solveTimer.stop();
                    System.out.println("[AUTO SOLVE] Animation complete!");
                    JOptionPane.showMessageDialog(this,
                            "Auto solve complete!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    checkWin();
                }
            });

            solveTimer.start();
        });
    }

    private void changeDifficulty(GameLevel.Difficulty newDifficulty) {
        System.out.println("[LEVEL] Changing difficulty to: " + newDifficulty.getName());

        if (newDifficulty.equals(gameLevel.getCurrentDifficulty())) {
            System.out.println("[LEVEL] Same difficulty, starting new game...");
            newGame();
            return;
        }

        // Stop timer dulu
        if (gameTimer != null) {
            gameTimer.stop();
            System.out.println("[LEVEL] Timer stopped");
        }

        // Show loading
        System.out.println("[LEVEL] Rebuilding UI...");

        getContentPane().removeAll();
        initializeGame(newDifficulty);
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
                BufferedImage original = ImageIO.read(file);

                int size = TILE_SIZE * Math.max(
                        gameLevel.getCurrentDifficulty().getRows(),
                        gameLevel.getCurrentDifficulty().getCols());

                puzzleImage = resizeImage(original, size, size);
                useImage = true;
                updateBoard();

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

    private Image createTileImage(int tileIndex) {
        int rows = gameLevel.getCurrentDifficulty().getRows();
        int cols = gameLevel.getCurrentDifficulty().getCols();

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
            timeLabel.setText("Time: " + gameLevel.getFormattedTime());
        });
        gameTimer.start();
    }

    private Point findOriginalPosition(int value) {
        int rows = gameLevel.getCurrentDifficulty().getRows();
        int cols = gameLevel.getCurrentDifficulty().getCols();

        if (value == 0) {
            return new Point(rows - 1, cols - 1);
        }

        int index = value - 1;
        return new Point(index / cols, index % cols);
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
            PuzzleGame game = new PuzzleGame();
            game.setVisible(true);
        });
    }
}