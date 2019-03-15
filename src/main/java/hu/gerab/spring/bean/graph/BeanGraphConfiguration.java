package hu.gerab.spring.bean.graph;

import java.util.function.Predicate;

public interface BeanGraphConfiguration {

    Predicate<BeanNode> getExcludeNode();

    Predicate<BeanNode> getIncludeNode();

    Traversal getTraversal();
}
