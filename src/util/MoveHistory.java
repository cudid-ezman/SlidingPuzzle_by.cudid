package util;

import java.util.Deque;
import java.util.ArrayDeque;

public class MoveHistory {

    private final Deque<PuzzleTree> history;
    private final int maxHistorySize;

    public MoveHistory(int maxSize) {
        this.history = new ArrayDeque<>();
        this.maxHistorySize = maxSize;
    }

    public void push(PuzzleTree state) {
        if (history.size() >= maxHistorySize) {
            history.removeFirst();
        }
        history.push(state);
    }

    public PuzzleTree pop() {
        if (isEmpty()) {
            return null;
        }
        return history.pop();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void clear() {
        history.clear();
    }

    public boolean canUndo() {
        return !isEmpty();
    }
}