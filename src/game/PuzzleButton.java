package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom button untuk tile puzzle
 */
public class PuzzleButton extends JButton {

    private boolean isEmpty;
    private int value;
    private Point position; // Posisi di grid
    private final Point originalPosition; // Posisi asli (untuk validasi)
    private static final Color BORDER_NORMAL = new Color(180, 180, 180);
    private static final Color BORDER_HOVER = new Color(255, 215, 0);
    private static final Color EMPTY_COLOR = new Color(240, 240, 240);

    public PuzzleButton(int value, Point position, Point originalPosition) {
        super();
        this.value = value;
        this.position = position;
        this.originalPosition = originalPosition;
        this.isEmpty = (value == 0);

        initUI();
    }

    public PuzzleButton(Image image, int value, Point position, Point originalPosition) {
        super(new ImageIcon(image));
        this.value = value;
        this.position = position;
        this.originalPosition = originalPosition;
        this.isEmpty = (value == 0);

        initUI();
    }

    private void initUI() {
        setFont(new Font("Arial", Font.BOLD, 36));
        setBorder(BorderFactory.createLineBorder(BORDER_NORMAL, 2));
        setFocusPainted(false);

        if (isEmpty) {
            setText("");
            setBackground(EMPTY_COLOR);
            setContentAreaFilled(true);
            setEnabled(false);
        } else {
            setText(String.valueOf(value));
            setBackground(Color.WHITE);
        }

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEmpty) {
                    setBorder(BorderFactory.createLineBorder(BORDER_HOVER, 3));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isEmpty) {
                    setBorder(BorderFactory.createLineBorder(BORDER_NORMAL, 2));
                }
            }
        });
    }

    public void setImage(Image image) {
        if (image != null) {
            setIcon(new ImageIcon(image));
            setText("");
        }
    }

    public void makeEmpty() {
        isEmpty = true;
        value = 0;
        setText("");
        setIcon(null);
        setBackground(EMPTY_COLOR);
        setEnabled(false);
        setBorder(BorderFactory.createLineBorder(BORDER_NORMAL, 1));
    }

    public void setValue(int value, String text) {
        this.value = value;
        this.isEmpty = (value == 0);

        if (isEmpty) {
            makeEmpty();
        } else {
            setText(text);
            setBackground(Color.WHITE);
            setEnabled(true);
        }
    }

    public void setValueWithImage(int value, Image image) {
        this.value = value;
        this.isEmpty = (value == 0);

        if (isEmpty) {
            makeEmpty();
        } else {
            setImage(image);
            setBackground(Color.WHITE);
            setEnabled(true);
        }
    }

    public boolean isInCorrectPosition() {
        return position.equals(originalPosition);
    }

    public void highlightCorrect() {
        setBackground(new Color(200, 255, 200));
    }

    public void highlightWrong() {
        setBackground(new Color(255, 200, 200));
    }

    public void resetHighlight() {
        if (!isEmpty) {
            setBackground(Color.WHITE);
        }
    }

    // Getters and Setters
    public boolean isEmpty() { return isEmpty; }
    public int getValue() { return value; }
    public Point getPosition() { return position; }
    public Point getOriginalPosition() { return originalPosition; }

    public void setPosition(Point position) {
        this.position = position;
    }
}