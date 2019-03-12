package hu.gerab.spring.bean.graph;

import java.util.Collection;

public interface Traversal {

    Traversal DEPENDENCY_TO_DEPENDENT = BeanNode::getDependents;
    Traversal DEPENDENT_TO_DEPENDENCY = BeanNode::getDependencies;

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