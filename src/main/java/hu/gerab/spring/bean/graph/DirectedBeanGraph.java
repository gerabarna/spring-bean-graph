package hu.gerab.spring.bean.graph;

import java.util.Map;

public class DirectedBeanGraph {

    private final Traversal traversal;
    private final Map<String, BeanNode> rootNodes;
    private final Map<String, BeanNode> leafNodes;

    public DirectedBeanGraph(BeanGraph beanGraph, Traversal traversal) {
        rootNodes = beanGraph.getRootNodes(traversal);
        leafNodes = beanGraph.getLeafNodes(traversal);
        this.traversal = traversal;
    }

    public int getWidth() {
        return BeanGraph.getWidth(rootNodes);
    }

    public int getDepth() {
        return BeanGraph.getDepth(rootNodes, traversal);
    }

    public Map<String, BeanNode> getRootNodes() {
        return rootNodes;
    }

    public Map<String, BeanNode> getLeafNodes() {
        return leafNodes;
    }

    public Traversal getTraversal() {
        return traversal;
    }

    @Override
    public String toString() {
        return "DirectedBeanGraph{" +
                "traversal=" + traversal +
                ", rootNodeCount=" + rootNodes.size() +
                ", depth=" + getDepth() +
                ", =" + getDepth() +
                '}';
    }
}
