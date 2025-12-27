package core;

import java.util.Stack;

/**
 * Class untuk menyimpan history gerakan menggunakan Stack
 * Fitur: Undo dengan LIFO (Last In First Out)
 */
public class MoveHistory {

    private Stack<PuzzleState> history;
    private int maxHistorySize;

    public MoveHistory() {
        this.history = new Stack<>();
        this.maxHistorySize = 1000; // Batasi ukuran history
    }

    public MoveHistory(int maxSize) {
        this.history = new Stack<>();
        this.maxHistorySize = maxSize;
    }

    /**
     * Simpan state ke history
     */
    public void push(PuzzleState state) {
        // Jika sudah penuh, hapus yang paling lama (bottom of stack)
        if (history.size() >= maxHistorySize) {
            history.remove(0);
        }
        history.push(state);
    }

    /**
     * Ambil state terakhir dari history (UNDO)
     */
    public PuzzleState pop() {
        if (isEmpty()) {
            return null;
        }
        return history.pop();
    }

    /**
     * Lihat state terakhir tanpa menghapus
     */
    public PuzzleState peek() {
        if (isEmpty()) {
            return null;
        }
        return history.peek();
    }

    /**
     * Cek apakah history kosong
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }

    /**
     * Dapatkan ukuran history
     */
    public int size() {
        return history.size();
    }

    /**
     * Clear semua history
     */
    public void clear() {
        history.clear();
    }

    /**
     * Cek apakah bisa undo
     */
    public boolean canUndo() {
        return !isEmpty();
    }

    /**
     * Get jumlah undo yang tersisa
     */
    public int getRemainingUndos() {
        return history.size();
    }
}