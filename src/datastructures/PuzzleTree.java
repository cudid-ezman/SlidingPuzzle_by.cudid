package datastructures;

import core.PuzzleState;

import java.util.*;

@SuppressWarnings("ALL")
public class PuzzleTree {

    public static class TreeNode {
        private final PuzzleState state;
        private TreeNode parent;
        private final ArrayList<TreeNode> children;
        private int depth;
        private final String moveFromParent;

        public TreeNode(PuzzleState state) {
            this.state = state;
            this.children = new ArrayList<>();
            this.depth = 0;
            this.moveFromParent = "START";
        }

        public TreeNode(PuzzleState state, TreeNode parent, String move) {
            this.state = state;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.depth = parent != null ? parent.depth + 1 : 0;
            this.moveFromParent = move;
        }

        public void addChild(TreeNode child) {
            children.add(child);
            child.parent = this;
            child.depth = this.depth + 1;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public List<TreeNode> getPathFromRoot() {
            LinkedList<TreeNode> path = new LinkedList<>();
            TreeNode current = this;

            while (current != null) {
                path.addFirst(current);
                current = current.parent;
            }

            return path;
        }

        public PuzzleState getState() { return state; }

        public int getDepth() { return depth; }
        public String getMoveFromParent() { return moveFromParent; }
        public int getChildrenCount() { return children.size(); }
    }

    private static class NodeMapEntry {
        @SuppressWarnings("unused")
        String key;
        @SuppressWarnings("unused")
        TreeNode node;

        NodeMapEntry(String key, TreeNode node) {
            this.key = key;
            this.node = node;
        }
    }

    private final TreeNode root;
    private int totalNodes;
    private int maxDepth;
    private final ArrayList<NodeMapEntry> nodeMap;

    public PuzzleTree(PuzzleState rootState) {
        this.root = new TreeNode(rootState);
        this.totalNodes = 1;
        this.maxDepth = 0;
        this.nodeMap = new ArrayList<>();
        addToMap(rootState.getStateKey(), root);
    }

    private void addToMap(String key, TreeNode node) {
        nodeMap.add(new NodeMapEntry(key, node));
    }

    public void buildFromSolutionPath(List<PuzzleState> solutionPath) {
        if (solutionPath == null || solutionPath.isEmpty()) {
            return;
        }

        TreeNode current = root;

        for (int i = 1; i < solutionPath.size(); i++) {
            PuzzleState state = solutionPath.get(i);
            String move = state.getMoveDescription();

            TreeNode child = new TreeNode(state, current, move);
            current.addChild(child);
            addToMap(state.getStateKey(), child);

            current = child;
            totalNodes++;
            maxDepth = Math.max(maxDepth, child.getDepth());
        }
    }

    public void buildCompleteTree(int maxDepth) {
        Queue<TreeNode> queue = new LinkedList<>();
        ArrayList<String> visited = new ArrayList<>();

        queue.add(root);
        visited.add(root.getState().getStateKey());

        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();

            if (current.getDepth() >= maxDepth) {
                continue;
            }

            List<PuzzleState> neighbors = current.getState().getNeighbors();

            for (PuzzleState neighborState : neighbors) {
                String key = neighborState.getStateKey();

                TreeNode child = new TreeNode(neighborState, current,
                        neighborState.getMoveDescription());
                current.addChild(child);
                totalNodes++;

                if (!containsKey(visited, key)) {
                    visited.add(key);
                    queue.add(child);
                    addToMap(key, child);
                    this.maxDepth = Math.max(this.maxDepth, child.getDepth());
                }
            }
        }
    }

    private boolean containsKey(ArrayList<String> list, String key) {
        for (String item : list) {
            if (item.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public List<TreeNode> preOrderTraversal() {
        ArrayList<TreeNode> result = new ArrayList<>();
        preOrderHelper(root, result);
        return result;
    }

    private void preOrderHelper(TreeNode node, ArrayList<TreeNode> result) {
        if (node == null) return;

        result.add(node);
        for (TreeNode child : node.children) {
            preOrderHelper(child, result);
        }
    }

    public List<TreeNode> postOrderTraversal() {
        ArrayList<TreeNode> result = new ArrayList<>();
        postOrderHelper(root, result);
        return result;
    }

    private void postOrderHelper(TreeNode node, ArrayList<TreeNode> result) {
        if (node == null) return;

        for (TreeNode child : node.children) {
            postOrderHelper(child, result);
        }
        result.add(node);
    }

    public List<List<TreeNode>> levelOrderTraversal() {
        ArrayList<List<TreeNode>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            ArrayList<TreeNode> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node);

                assert node != null;
                queue.addAll(node.children);
            }

            result.add(currentLevel);
        }

        return result;
    }

    public List<TreeNode> getLeafNodes() {
        ArrayList<TreeNode> leaves = new ArrayList<>();
        collectLeaves(root, leaves);
        return leaves;
    }

    private void collectLeaves(TreeNode node, ArrayList<TreeNode> leaves) {
        if (node == null) return;

        if (node.isLeaf()) {
            leaves.add(node);
        } else {
            for (TreeNode child : node.children) {
                collectLeaves(child, leaves);
            }
        }
    }

    public List<TreeNode> getNodesAtDepth(int depth) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        collectNodesAtDepth(root, depth, nodes);
        return nodes;
    }

    private void collectNodesAtDepth(TreeNode node, int targetDepth, ArrayList<TreeNode> nodes) {
        if (node == null) return;

        if (node.getDepth() == targetDepth) {
            nodes.add(node);
        }

        for (TreeNode child : node.children) {
            collectNodesAtDepth(child, targetDepth, nodes);
        }
    }

    public int getHeight() {
        return maxDepth;
    }

    public double getAverageBranchingFactor() {
        if (totalNodes <= 1) return 0;

        int totalChildren = 0;
        int nonLeafNodes = 0;

        List<TreeNode> allNodes = preOrderTraversal();
        for (TreeNode node : allNodes) {
            if (!node.isLeaf()) {
                totalChildren += node.getChildrenCount();
                nonLeafNodes++;
            }
        }

        return nonLeafNodes == 0 ? 0 : (double) totalChildren / nonLeafNodes;
    }

    public String getTreeStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PUZZLE SOLUTION TREE ===\n");
        printTreeHelper(root, "", true, sb);
        return sb.toString();
    }

    private void printTreeHelper(TreeNode node, String prefix, boolean isTail, StringBuilder sb) {
        if (node == null) return;

        sb.append(prefix);
        sb.append(isTail ? "└── " : "├── ");
        sb.append("Depth ").append(node.getDepth());
        sb.append(" [").append(node.getMoveFromParent()).append("]\n");

        for (int i = 0; i < node.children.size(); i++) {
            boolean isLast = (i == node.children.size() - 1);
            printTreeHelper(node.children.get(i),
                    prefix + (isTail ? "    " : "│   "),
                    isLast, sb);
        }
    }

    public String getTreeStats() {
        return String.format("""
                        Tree Statistics:
                        - Total Nodes: %d
                        - Max Depth (Height): %d
                        - Leaf Nodes: %d
                        - Avg Branching Factor: %.2f
                        - Root State: %s""",
                totalNodes, maxDepth, getLeafNodes().size(),
                getAverageBranchingFactor(),
                root.getState().getStateKey());
    }

    public TreeNode getRoot() { return root; }
    public int getTotalNodes() { return totalNodes; }
    public int getMaxDepth() { return maxDepth; }
}