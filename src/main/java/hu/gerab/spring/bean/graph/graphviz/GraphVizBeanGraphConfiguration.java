package hu.gerab.spring.bean.graph.graphviz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import hu.gerab.spring.bean.graph.BeanGraphConfiguration;
import hu.gerab.spring.bean.graph.BeanNode;
import hu.gerab.spring.bean.graph.DirectedBeanGraph;
import hu.gerab.spring.bean.graph.Traversal;
import hu.gerab.spring.bean.graph.utils.BeanNodePredicates;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static hu.gerab.spring.bean.graph.utils.BeanNodePredicates.toPackagePredicate;

public class GraphVizBeanGraphConfiguration implements BeanGraphConfiguration {

    private class Conditional<T> implements Predicate<BeanNode> {
        protected Predicate<BeanNode> predicate;
        protected T target;

        public Conditional(Predicate<BeanNode> predicate, T target) {
            this.predicate = predicate;
            this.target = target;
        }

        @Override
        public boolean test(BeanNode beanNode) {
            return predicate.test(beanNode);
        }
    }

    private class ConditionalStyle extends Conditional<Attributes<? extends ForNode>> {

        ConditionalStyle(Predicate<BeanNode> predicate, Attributes<? extends ForNode> target) {
            super(predicate, target);
        }
    }

    private class ConditionalHtml extends Conditional<BiConsumer<BeanNode, StringBuilder>> {

        ConditionalHtml(Predicate<BeanNode> predicate, BiConsumer<BeanNode, StringBuilder> target) {
            super(predicate, target);
        }
    }

    private Predicate<BeanNode> excludeNode = BeanNodePredicates::rejectAll;
    private Predicate<BeanNode> includeNode = BeanNodePredicates::rejectAll;

    private List<ConditionalStyle> conditionalStyles = new ArrayList<>();
    private List<ConditionalHtml> conditionalHtmls = new ArrayList<>();

    private Traversal traversal = Traversal.DEPENDENT_TO_DEPENDENCY;
    private Format format = Format.SVG;
    private String title = "Spring Beans";

    public Renderer render(DirectedBeanGraph beanGraph) {
        Collection<BeanNode> rootNodes = beanGraph.getRootNodes().values();

        List<String> visitedNodes = new ArrayList<>();
        List<Node> nodes = rootNodes.stream()
                .map(node -> convert(node, visitedNodes))
                .collect(Collectors.toList());

        Graph graph = graph(title).directed()
                .with(nodes);

        int maxDepth = beanGraph.getDepth();
        int width = rootNodes.stream()
                .map(BeanNode::getName)
                .mapToInt(name -> name.length() + 4)
                .sum();

        return Graphviz.fromGraph(graph)
                .height(maxDepth * 20)
                .width(width)
                .render(format);
    }


    protected Node convert(BeanNode beanNode, List<String> visitedNodes) {
        String html = toHtml(beanNode);
        String name = beanNode.getName();
        Node node = node(name).with(Label.html(html));
        visitedNodes.add(name);

        node = style(beanNode, node);

        for (BeanNode dependent : traversal.getChildren(beanNode)) {
            String dependentName = dependent.getName();
            if (visitedNodes.contains(dependentName)) {
                node = node.link(dependentName);
            } else {
                node = node.link(convert(dependent, visitedNodes));
            }
        }

        return node;
    }

    protected Node style(BeanNode beanNode, Node node) {
        for (ConditionalStyle conditionalStyle : conditionalStyles) {
            if (conditionalStyle.test(beanNode)) {
                node = node.with(conditionalStyle.target);
            }
        }
        return node;
    }

    protected String toHtml(BeanNode node) {
        StringBuilder sb = new StringBuilder();
        
        for (ConditionalHtml conditionalHtml : conditionalHtmls) {
            if (conditionalHtml.test(node)) {
                conditionalHtml.target.accept(node, sb);
            }
        }

        return sb.toString();
    }

    //////////////////////////////////////// Getters & Setters /////////////////////////////////////

    @Override
    public Predicate<BeanNode> getExcludeNode() {
        return excludeNode;
    }

    public void setExcludeNode(Predicate<BeanNode> excludeNode) {
        this.excludeNode = excludeNode;
    }

    public void excludePackages(String... packages) {
        excludeNode = toPackagePredicate(packages);
    }

    @Override
    public Predicate<BeanNode> getIncludeNode() {
        return includeNode;
    }

    public void setIncludeNode(Predicate<BeanNode> includeNode) {
        this.includeNode = includeNode;
    }

    public void includePackages(String... packages) {
        includeNode = toPackagePredicate(packages);
    }

    public void addHtml(Predicate<BeanNode> predicate, BiConsumer<BeanNode, StringBuilder> sb) {
        conditionalHtmls.add(new ConditionalHtml(predicate, sb));
    }

    public void addNodeStyle(Predicate<BeanNode> predicate, Attributes<? extends ForNode> style) {
        conditionalStyles.add(new ConditionalStyle(predicate, style));
    }

    @Override
    public Traversal getTraversal() {
        return traversal;
    }

    public void setTraversal(Traversal traversal) {
        this.traversal = traversal;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
