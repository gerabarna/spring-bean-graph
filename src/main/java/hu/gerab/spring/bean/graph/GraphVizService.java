package hu.gerab.spring.bean.graph;

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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

@Service
@ManagedResource(objectName = "hu.gerab:name=GraphVizService")
public class GraphVizService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);
    public static final BeanGraph.Traversal TRAVERSAL = BeanGraph.Traversal.DEPENDENCY_TO_DEPENDENT;

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
            BeanGraph beanGraph = beanGraphService.getBeanGraph(".*org\\.springframework.*", ".*hu\\.gerab\\.spring.*");
            exportBeanGraph(beanGraph, pathWithoutExt);
            LOGGER.info("Created bean graph");
        } catch (Exception e) {
            LOGGER.info("Bean graph creation failed", e);
        } finally {
            LOGGER.info("Bean graph process finished.");
        }
    }

    private void exportBeanGraph(BeanGraph beanGraph, String path) {
        try {
            Map<String, BeanNode> rootNodes = beanGraph.getRootNodes(TRAVERSAL);

            List<Node> nodes = rootNodes.values().stream()
                    .map(this::convert)
                    .peek(node -> node.with(Color.CADETBLUE))
                    .collect(Collectors.toList());

            Graph graph = graph("Spring Beans").directed();
            for (Node node : nodes) {
                graph = graph.with(node);
            }

            int maxDepth = rootNodes.values().stream().mapToInt(TRAVERSAL::getDepth).max().orElse(0);

            Format format = Format.SVG;
            Graphviz.fromGraph(graph)
                    .height(maxDepth*30)
                    .width(rootNodes.size() * 50)
                    .render(format)
                    .toFile(Paths.get(path+"."+format.name()).toAbsolutePath().toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to write file at=" + path, e);
        }
    }

    private Node convert(BeanNode beanNode) {
        Node node = node(beanNode.getName());

        for (BeanNode dependent : TRAVERSAL.getChildren(beanNode)) {
            convert(dependent);
            node = node.link(dependent.getName());
        }

        return node;
    }

}
