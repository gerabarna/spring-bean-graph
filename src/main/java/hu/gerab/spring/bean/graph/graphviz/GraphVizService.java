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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Renderer;
import hu.gerab.spring.bean.graph.BeanGraphService;
import hu.gerab.spring.bean.graph.BeanNode;
import hu.gerab.spring.bean.graph.DirectedBeanGraph;
import hu.gerab.spring.bean.graph.utils.BeanNodePredicates;

@Service
@ManagedResource(objectName = "hu.gerab:name=GraphVizService")
public class GraphVizService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);

    @Autowired
    private BeanGraphService beanGraphService;

    @Autowired(required = false)
    private volatile GraphVizBeanGraphConfiguration configuration;

    public static GraphVizBeanGraphConfiguration createDefaultConfiguration() {
        GraphVizBeanGraphConfiguration configuration = new GraphVizFileBeanGraphConfiguration();

        configuration.excludePackages("org.springframework", "hu.gerab.spring");

        configuration.addNodeStyle(BeanNode::isPrototype, Color.LIGHTGRAY);
        configuration.addNodeStyle(BeanNode::isLazyInit, Style.DOTTED);

        configuration.addHtml(BeanNodePredicates::acceptAll
                , (node, sb) -> sb.append("<b>").append(node.getName()).append("</b>"));
        configuration.addHtml((BeanNode node) -> !node.getSimpleClassName().equalsIgnoreCase(node.getName())
                , (node, sb) -> sb.append("<br/>").append(node.getSimpleClassName()));
        return configuration;
    }

    @PostConstruct
    public void init() {
        if (configuration == null) {
            configuration = createDefaultConfiguration();
        }
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (configuration instanceof GraphVizFileBeanGraphConfiguration) {
            GraphVizFileBeanGraphConfiguration fileConfiguration = (GraphVizFileBeanGraphConfiguration) this.configuration;
            if (fileConfiguration.isAutoCreateAfterStartup()) {
                Path path = fileConfiguration.getPath();
                try {
                    createGraphFile(path);
                    LOGGER.info("Finished writing bean graph file at={}", path);
                } catch (IOException e) {
                    LOGGER.error("Failed to write file at=" + path, e);
                }
            }
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "the (relative/absolute) path to the generated file")})
    public void createGraph(String path) throws IOException {
        Path nioPath = Paths.get(path).toAbsolutePath();
        createGraphFile(nioPath);
    }

    public void createGraphFile(Path path) throws IOException {
        createGraphFile(path, configuration);
    }

    public void createGraphFile(Path path, GraphVizBeanGraphConfiguration beanGraphConfiguration) throws IOException {
        render(beanGraphConfiguration).toFile(path.toFile());
    }

    @ManagedOperation
    public BufferedImage createGraphImage() {
        return createGraphImage(configuration);
    }

    public BufferedImage createGraphImage(GraphVizBeanGraphConfiguration beanGraphConfiguration) {
        return render(beanGraphConfiguration).toImage();
    }

    @ManagedOperation
    public String createGrphString() {
        return createGrphString(configuration);
    }

    public String createGrphString(GraphVizBeanGraphConfiguration beanGraphConfiguration) {
        return render(beanGraphConfiguration).toString();
    }

    public void createGrphStream(OutputStream os) throws IOException {
        createGrphStream(os, configuration);
    }

    public void createGrphStream(OutputStream os, GraphVizBeanGraphConfiguration beanGraphConfiguration) throws IOException {
        render(beanGraphConfiguration).toOutputStream(os);
    }

    private Renderer render(GraphVizBeanGraphConfiguration beanGraphConfiguration) {
        DirectedBeanGraph beanGraph = beanGraphService.getBeanGraph(beanGraphConfiguration);
        return beanGraphConfiguration.render(beanGraph);
    }

    private GraphVizBeanGraphConfiguration getConfiguration() {
        return configuration;
    }
}
