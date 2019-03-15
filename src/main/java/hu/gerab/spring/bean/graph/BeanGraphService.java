package hu.gerab.spring.bean.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import hu.gerab.spring.bean.graph.utils.BeanNodePredicates;

@Service
@ManagedResource(objectName = "hu.gerab:name=BeanGraphService")
public class BeanGraphService implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public BeanGraph getBeanGraph(Predicate<BeanNode>... excludes) {
        Predicate<BeanNode> exclude = Arrays.stream(excludes)
                .reduce(BeanNodePredicates::rejectAll, Predicate::or);
        return getBeanGraph(exclude);
    }

    public BeanGraph getBeanGraph(Predicate<BeanNode> exclude) {
        return getBeanGraph(exclude, BeanNodePredicates::rejectAll);
    }

    public BeanGraph getBeanGraph() {
        return getBeanGraph(BeanNodePredicates::rejectAll, BeanNodePredicates::rejectAll);
    }

    public BeanGraph getBeanGraph(Predicate<BeanNode> exclude, Predicate<BeanNode> include) {
        LOGGER.info("Creating bean graph...");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        //TODO classcastEx
        ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext) applicationContext).getBeanFactory();

        Map<String, BeanNode> beanNameToNodeMap = new HashMap<>();

        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);

            BeanNode beanNode = new BeanNode(beanDefinition, beanDefinitionName);

            if (!exclude.test(beanNode) || include.test(beanNode)) {
                beanNameToNodeMap.put(beanDefinitionName, beanNode);
            }
        }

        for (BeanNode dependent : beanNameToNodeMap.values()) {
            String[] dependenciesForBean = beanFactory.getDependenciesForBean(dependent.getName());
            for (String dependencyName : dependenciesForBean) {
                BeanNode dependency = beanNameToNodeMap.get(dependencyName);
                if (dependency == null) {
                    LOGGER.warn("Unable to resolve dependency={} for bean={}", dependencyName, dependent.getName());
                    continue;
                }
                addDependency(dependent, dependency);
            }
        }

        BeanGraph graph = new BeanGraph(beanNameToNodeMap);
        LOGGER.info("Bean Graph generation finished, found {} nodes, out of {} beanDefinitions."
                , beanNameToNodeMap.size(), beanDefinitionNames.length);
        return graph;
    }

    public DirectedBeanGraph getBeanGraph(BeanGraphConfiguration configuration) {
        return getBeanGraph(configuration.getExcludeNode(), configuration.getIncludeNode())
                .direct(configuration.getTraversal());
    }

    private void addDependency(BeanNode dependent, BeanNode dependency) {
        dependency.addDependent(dependent);
        dependent.addDependency(dependency);
    }

}
