package datastructures;

import core.PuzzleState;

import java.util.*;

/**
 * Class untuk merepresentasikan Puzzle sebagai Graph
 * REFACTORED: Mengganti HashMap dan HashSet dengan ArrayList
 */
public class PuzzleGraph {

    /**
     * Graph Node dengan Adjacency List
     */
    public static class GraphNode {
        private PuzzleState state;
        private ArrayList<GraphNode> neighbors; // Adjacency List
        private boolean visited;
        private int distance;

        public GraphNode(PuzzleState state) {
            this.state = state;
            this.neighbors = new ArrayList<>();
            this.visited = false;
            this.distance = Integer.MAX_VALUE;
        }

        public void addNeighbor(GraphNode neighbor) {
            // Cek duplikasi manual
            boolean found = false;
            for (GraphNode n : neighbors) {
                if (n.getState().getStateKey().equals(neighbor.getState().getStateKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                neighbors.add(neighbor);
            }
        }

        public PuzzleState getState() { return state; }
        public ArrayList<GraphNode> getNeighbors() { return neighbors; }
        public boolean isVisited() { return visited; }
        public void setVisited(boolean visited) { this.visited = visited; }
        public int getDistance() { return distance; }
        public void setDistance(int distance) { this.distance = distance; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof GraphNode)) return false;
            GraphNode other = (GraphNode) obj;
            return this.state.getStateKey().equals(other.state.getStateKey());
        }
    }

    /**
     * Entry untuk menyimpan node (menggantikan HashMap)
     */
    private static class NodeEntry {
        String key;
        GraphNode node;

        NodeEntry(String key, GraphNode node) {
            this.key = key;
            this.node = node;
        }
    }

    // Mengganti HashMap dengan ArrayList
    private ArrayList<NodeEntry> nodes;
    private int totalNodes;
    private int totalEdges;

    public PuzzleGraph() {
        this.nodes = new ArrayList<>();
        this.totalNodes = 0;
        this.totalEdges = 0;
    }

    /**
     * Tambah node ke graph
     * Menggantikan HashMap.put()
     */
    public GraphNode addNode(PuzzleState state) {
        String key = state.getStateKey();

        // Cari apakah sudah ada (menggantikan HashMap.containsKey())
        GraphNode existing = findNode(key);
        if (existing != null) {
            return existing;
        }

        // Tambah baru
        GraphNode node = new GraphNode(state);
        nodes.add(new NodeEntry(key, node));
        totalNodes++;
        return node;
    }

    /**
     * Cari node berdasarkan key
     * Menggantikan HashMap.get()
     */
    private GraphNode findNode(String key) {
        for (NodeEntry entry : nodes) {
            if (entry.key.equals(key)) {
                return entry.node;
            }
        }
        return null;
    }

    /**
     * Tambah edge antara dua node
     */
    public void addEdge(PuzzleState state1, PuzzleState state2) {
        GraphNode node1 = addNode(state1);
        GraphNode node2 = addNode(state2);

        node1.addNeighbor(node2);
        node2.addNeighbor(node1);
        totalEdges++;
    }

    /**
     * Build graph dari initial state dengan BFS
     * Mengganti HashSet dengan ArrayList untuk visited
     */
    public void buildGraphBFS(PuzzleState startState, int maxDepth) {
        Queue<GraphNode> queue = new LinkedList<>();
        ArrayList<String> visited = new ArrayList<>(); // Mengganti HashSet

        GraphNode startNode = addNode(startState);
        startNode.setDistance(0);
        queue.add(startNode);
        visited.add(startState.getStateKey());

        while (!queue.isEmpty() && startNode.getDistance() < maxDepth) {
            GraphNode current = queue.poll();

            List<PuzzleState> neighborStates = current.getState().getNeighbors();

            for (PuzzleState neighborState : neighborStates) {
                String key = neighborState.getStateKey();

                // Linear search di ArrayList (menggantikan HashSet.contains())
                if (!containsKey(visited, key)) {
                    GraphNode neighborNode = addNode(neighborState);
                    current.addNeighbor(neighborNode);
                    neighborNode.setDistance(current.getDistance() + 1);

                    visited.add(key);
                    queue.add(neighborNode);
                    totalEdges++;
                }
            }
        }
    }

    /**
     * Helper method untuk cek contains
     */
    private boolean containsKey(ArrayList<String> list, String key) {
        for (String item : list) {
            if (item.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DFS Traversal menggunakan Stack
     */
    public List<PuzzleState> findPathDFS(PuzzleState start, PuzzleState goal) {
        resetVisited();

        GraphNode startNode = getNode(start.getStateKey());
        GraphNode goalNode = getNode(goal.getStateKey());

        if (startNode == null || goalNode == null) {
            return null;
        }

        Stack<GraphNode> stack = new Stack<>();
        ArrayList<ParentEntry> parentList = new ArrayList<>(); // Mengganti HashMap

        stack.push(startNode);
        startNode.setVisited(true);

        while (!stack.isEmpty()) {
            GraphNode current = stack.pop();

            if (current.equals(goalNode)) {
                return reconstructPath(parentList, startNode, goalNode);
            }

            for (GraphNode neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    addParent(parentList, neighbor, current);
                    stack.push(neighbor);
                }
            }
        }

        return null;
    }

    /**
     * BFS Traversal menggunakan Queue
     */
    public List<PuzzleState> findShortestPathBFS(PuzzleState start, PuzzleState goal) {
        resetVisited();

        GraphNode startNode = getNode(start.getStateKey());
        GraphNode goalNode = getNode(goal.getStateKey());

        if (startNode == null || goalNode == null) {
            return null;
        }

        Queue<GraphNode> queue = new LinkedList<>();
        ArrayList<ParentEntry> parentList = new ArrayList<>();

        queue.add(startNode);
        startNode.setVisited(true);

        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();

            if (current.equals(goalNode)) {
                return reconstructPath(parentList, startNode, goalNode);
            }

            for (GraphNode neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    addParent(parentList, neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return null;
    }

    /**
     * Entry untuk parent mapping (menggantikan HashMap)
     */
    private static class ParentEntry {
        GraphNode child;
        GraphNode parent;

        ParentEntry(GraphNode child, GraphNode parent) {
            this.child = child;
            this.parent = parent;
        }
    }

    /**
     * Add parent relationship (menggantikan HashMap.put())
     */
    private void addParent(ArrayList<ParentEntry> parentList, GraphNode child, GraphNode parent) {
        parentList.add(new ParentEntry(child, parent));
    }

    /**
     * Get parent of node (menggantikan HashMap.get())
     */
    private GraphNode getParent(ArrayList<ParentEntry> parentList, GraphNode child) {
        for (ParentEntry entry : parentList) {
            if (entry.child.equals(child)) {
                return entry.parent;
            }
        }
        return null;
    }

    /**
     * Rekonstruksi path
     */
    private List<PuzzleState> reconstructPath(ArrayList<ParentEntry> parentList,
                                              GraphNode start, GraphNode goal) {
        LinkedList<PuzzleState> path = new LinkedList<>();
        GraphNode current = goal;

        while (current != null && !current.equals(start)) {
            path.addFirst(current.getState());
            current = getParent(parentList, current);
        }

        if (current != null) {
            path.addFirst(current.getState());
        }

        return path;
    }

    /**
     * Hitung degree dari sebuah node
     */
    public int getNodeDegree(PuzzleState state) {
        GraphNode node = getNode(state.getStateKey());
        if (node == null) return 0;
        return node.getNeighbors().size();
    }

    /**
     * Cek apakah graph connected
     */
    public boolean isConnected() {
        if (nodes.isEmpty()) return true;

        resetVisited();
        GraphNode start = nodes.get(0).node;

        Queue<GraphNode> queue = new LinkedList<>();
        queue.add(start);
        start.setVisited(true);
        int visitedCount = 1;

        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();

            for (GraphNode neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    queue.add(neighbor);
                    visitedCount++;
                }
            }
        }

        return visitedCount == totalNodes;
    }

    /**
     * Reset visited flag
     */
    private void resetVisited() {
        for (NodeEntry entry : nodes) {
            entry.node.setVisited(false);
        }
    }

    /**
     * Get statistics
     */
    public String getGraphStats() {
        return String.format("Graph Stats:\n" +
                        "- Total Nodes: %d\n" +
                        "- Total Edges: %d\n" +
                        "- Is Connected: %s\n" +
                        "- Average Degree: %.2f",
                totalNodes, totalEdges,
                isConnected() ? "Yes" : "No",
                totalNodes > 0 ? (totalEdges * 2.0 / totalNodes) : 0);
    }

    // Getters
    public int getTotalNodes() { return totalNodes; }
    public int getTotalEdges() { return totalEdges; }
    public GraphNode getNode(String stateKey) { return findNode(stateKey); }

    public ArrayList<GraphNode> getAllNodes() {
        ArrayList<GraphNode> allNodes = new ArrayList<>();
        for (NodeEntry entry : nodes) {
            allNodes.add(entry.node);
        }
        return allNodes;
    }
}