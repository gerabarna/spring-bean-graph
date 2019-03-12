package hu.gerab.spring.bean.graph.graphviz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import hu.gerab.spring.bean.graph.BeanGraph;
import hu.gerab.spring.bean.graph.BeanGraphService;
import hu.gerab.spring.bean.graph.BeanNode;
import hu.gerab.spring.bean.graph.DirectedBeanGraph;
import hu.gerab.spring.bean.graph.Traversal;

import static guru.nidi.graphviz.engine.Format.SVG;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

@Service
@ManagedResource(objectName = "hu.gerab:name=GraphVizService")
public class GraphVizService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);
    public Traversal traversal = Traversal.DEPENDENT_TO_DEPENDENCY;

    @Autowired
    private BeanGraphService beanGraphService;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        createGraph("springBeans");
    }

    @ManagedOperation
    @ManagedOperationParameters({@ManagedOperationParameter(name = "pathWithoutExt", description = "the (relative/absolute) path to the generated png file without extension")})
    public void createGraph(String pathWithoutExt) {
        try {
            LOGGER.info("Creating bean graph...");
            BeanGraph beanGraph = beanGraphService.getBeanGraph("org.springframework", "hu.gerab.spring");
            exportBeanGraph(beanGraph.direct(traversal), pathWithoutExt);
        } catch (Exception e) {
            LOGGER.info("Bean graph creation failed", e);
        } finally {
            LOGGER.info("Bean graph process finished.");
        }
    }

    private void exportBeanGraph(DirectedBeanGraph beanGraph, String path) {
        try {
            Path nioPath = Paths.get(path + "." + SVG.name()).toAbsolutePath();
            render(beanGraph, SVG).toFile(nioPath.toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to write file at=" + path, e);
        }
    }

    private Renderer render(DirectedBeanGraph beanGraph, Format format) {
        return toGraphViz(beanGraph).render(format);
    }

    private Graphviz toGraphViz(DirectedBeanGraph beanGraph) {
        Collection<BeanNode> rootNodes = beanGraph.getRootNodes().values();

        List<Node> nodes = rootNodes.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        Graph graph = graph("Spring Beans").directed()
                .with(nodes);

        int maxDepth = beanGraph.getDepth();
        int width = beanGraph.getWidth();

        return Graphviz.fromGraph(graph)
                .height(maxDepth * 20)
                .width(width);
    }


    private Node convert(BeanNode beanNode) {
        Node node = node(beanNode.getName()).with(Label.html(toHtml(beanNode)));

        if (beanNode.isPrototype()) {
            node = node.with(Color.LIGHTGRAY);
        }

        if (beanNode.isLazyInit()) {
            node = node.with(Style.DOTTED);
        }

        for (BeanNode dependent : traversal.getChildren(beanNode)) {
            node = node.link(convert(dependent));
        }

        return node;
    }


    public String toHtml(BeanNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(node.getName()).append("</b>");

        String simpleName = last(node.getBeanClassName().split("\\."));
        if (simpleName != null && !simpleName.equalsIgnoreCase(node.getName())) {
            sb.append("<br/>").append(simpleName);
        }
        return sb.toString();
    }

    public static <T> T last(T[] values) {
        return ObjectUtils.isEmpty(values) ? null : values[values.length - 1];
    }

    public void setTraversal(Traversal traversal) {
        this.traversal = traversal;
    }
}
