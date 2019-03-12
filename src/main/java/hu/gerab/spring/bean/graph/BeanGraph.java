package hu.gerab.spring.bean.graph;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class BeanGraph {

    public static final Collector<BeanNode, ?, Map<String, BeanNode>> THROWING_TREE_MAP_COLLECTOR = Collectors.
            toMap(BeanNode::getName, Function.identity(), (a, b) -> {
                throw new IllegalStateException("Duplicate key=" + a);
            }, TreeMap::new);


    private final Map<String, BeanNode> beanNameToNodeMap;

    public BeanGraph(Map<String, BeanNode> beanNameToNodeMap) {
        this.beanNameToNodeMap = new TreeMap<>(beanNameToNodeMap);
    }

    public Map<String, BeanNode> getRootNodes(Traversal traversal) {
        return getLeafNodes(traversal.invert());
    }

    public Map<String, BeanNode> getLeafNodes(Traversal traversal) {
        return beanNameToNodeMap.values().stream()
                .filter(traversal::isLeaf)
                .collect(THROWING_TREE_MAP_COLLECTOR);
    }

    public int getWidth(Traversal traversal) {
        return getWidth(getRootNodes(traversal));
    }

    public static int getWidth(Map<String, BeanNode> rootNodes) {
        return rootNodes.values().stream().map(BeanNode::getName).mapToInt(name -> name.length() + 4).sum();
    }

    public int getDepth(Traversal traversal) {
        return getDepth(getRootNodes(traversal), traversal);
    }

    public static int getDepth(Map<String, BeanNode> rootNodes, Traversal traversal) {
        return rootNodes.values().stream().mapToInt(traversal::getDepth).max().orElse(0);
    }

    public DirectedBeanGraph direct(Traversal traversal) {
        return new DirectedBeanGraph(this, traversal);
    }
}
