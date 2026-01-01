package util;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.LinkedList;

@SuppressWarnings("ALL")
public class PuzzleSolver {

    private final PuzzleTree goalState;
    private int maxIterations;
    
    private final Map<Integer, Point> posisiTujuan;

    public PuzzleSolver(PuzzleTree goalState) {
        this.goalState = goalState;
        this.maxIterations = 200000; 
        this.posisiTujuan = siapkanPosisiTujuan(goalState);
    }
    
    public void aturMaksIterasi(int max) {
        this.maxIterations = max;
    }

    private static class SearchNode implements Comparable<SearchNode> {
        PuzzleTree state;
        int g;
        int h;
        int f;

        public SearchNode(PuzzleTree state, int g, int h) {
            this.state = state;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(SearchNode other) {
            return Integer.compare(this.f, other.f);
        }
    }

    public List<PuzzleTree> solve(PuzzleTree startState) {
        if (!startState.isSolvable()) {
            System.out.println("[A*] Puzzle logic says unsolvable.");
            return null;
        }
        System.out.println("[A*] Starting search (Manhattan Heuristic)...");

        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        Map<String, Integer> gScoreMap = new HashMap<>();

        int startH = hitungJarakManhattan(startState);
        SearchNode startNode = new SearchNode(startState, 0, startH);
        
        openSet.add(startNode);
        gScoreMap.put(startState.getStateKey(), 0);

        int iterations = 0;

        while (!openSet.isEmpty()) {
            iterations++;
            
            if (iterations > maxIterations) {
                System.out.println("[A*] Limit reached (" + iterations + ")");
                return null; 
            }

            SearchNode currentWrapper = openSet.poll();
            PuzzleTree current = currentWrapper.state;

            if (current.getStateKey().equals(goalState.getStateKey())) {
                System.out.println("[A*] Solution found in " + iterations + " iterations.");
                return buatUrutanLangkah(current);
            }

            for (PuzzleTree neighbor : current.getNeighbors()) {
                int tentativeG = currentWrapper.g + 1;
                String neighborKey = neighbor.getStateKey();

                if (!gScoreMap.containsKey(neighborKey) || tentativeG < gScoreMap.get(neighborKey)) {
                    gScoreMap.put(neighborKey, tentativeG);
                    int h = hitungJarakManhattan(neighbor);
                    openSet.add(new SearchNode(neighbor, tentativeG, h));
                }
            }
        }
        return null;
    }

    private int hitungJarakManhattan(PuzzleTree state) {
        int distance = 0;
        int[][] board = state.getBoard();
        int rows = state.getRows();
        int cols = state.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int value = board[r][c];
                if (value != 0) {
                    Point target = posisiTujuan.get(value);
                    if (target != null) {
                        // Rumus Manhattan: |x1 - x2| + |y1 - y2|
                        distance += Math.abs(r - target.x) + Math.abs(c - target.y);
                    }
                }
            }
        }
        return distance;
    }

    private Map<Integer, Point> siapkanPosisiTujuan(PuzzleTree goal) {
        Map<Integer, Point> map = new HashMap<>();
        int[][] board = goal.getBoard();
        for (int r = 0; r < goal.getRows(); r++) {
            for (int c = 0; c < goal.getCols(); c++) {
                map.put(board[r][c], new Point(r, c));
            }
        }
        return map;
    }

    private List<PuzzleTree> buatUrutanLangkah(PuzzleTree state) {
        LinkedList<PuzzleTree> path = new LinkedList<>();
        PuzzleTree current = state;
        while (current != null) {
            path.addFirst(current);
            current = current.getParent();
        }
        return path;
    }
}