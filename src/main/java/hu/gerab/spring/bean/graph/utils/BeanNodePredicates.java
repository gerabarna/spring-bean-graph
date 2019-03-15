package hu.gerab.spring.bean.graph.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import hu.gerab.spring.bean.graph.BeanNode;

public class BeanNodePredicates {

    private BeanNodePredicates() {
    }

    public static <T> boolean acceptAll(T t) {
        return true;
    }

    public static <T> boolean rejectAll(T t) {
        return false;
    }

    public static Predicate<BeanNode> toPackagePredicate(String[] packages) {
        List<Predicate<BeanNode>> predicates = Arrays.stream(packages)
                .map(packageFilter -> packageFilter.replace(".", "\\."))
                .map(packageFilter -> (Predicate<BeanNode>) ((BeanNode node) -> node.getBeanClassName().matches(".*" + packageFilter + ".*")))
                .collect(Collectors.toList());
        return predicates.stream()
                .reduce(BeanNodePredicates::rejectAll, Predicate::or);
    }
}
