package hu.gerab.spring.bean.graph.graphviz;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GraphVizFileBeanGraphConfiguration extends GraphVizBeanGraphConfiguration {

    private Path path;
    private boolean autoCreateAfterStartup;

    public GraphVizFileBeanGraphConfiguration() {
        this(Paths.get("springBeans.svg"), true);
    }

    public GraphVizFileBeanGraphConfiguration(Path path, boolean autoCreateAfterStartup) {
        this.path = path;
        this.autoCreateAfterStartup = autoCreateAfterStartup;
    }

    public boolean isAutoCreateAfterStartup() {
        return autoCreateAfterStartup;
    }

    public void setAutoCreateAfterStartup(boolean autoCreateAfterStartup) {
        this.autoCreateAfterStartup = autoCreateAfterStartup;
    }

    public Path getPath() {

        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
