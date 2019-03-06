package hu.gerab.spring.bean.graph;

import java.util.Collection;
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

    public interface Traversal {

        Traversal DEPENDENCY_TO_DEPENDENT = BeanNode::getDependendents;
        Traversal DEPENDENT_TO_DEPENDENCY = BeanNode::getDependendencies;

        Collection<BeanNode> getChildren(BeanNode parent);

        default int getDepth(BeanNode root) {
            return getChildren(root).stream().mapToInt(this::getDepth).max().orElse(0);
        }

        default Traversal invert() {
            return this == DEPENDENCY_TO_DEPENDENT ? DEPENDENT_TO_DEPENDENCY : DEPENDENCY_TO_DEPENDENT;
        }

        default boolean isRoot(BeanNode node) {
            return invert().getChildren(node).isEmpty();
        }

        default boolean isLeaf(BeanNode node) {
            return getChildren(node).isEmpty();
        }
    }

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
}
