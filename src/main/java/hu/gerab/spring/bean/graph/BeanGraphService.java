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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@ManagedResource(objectName = "hu.gerab:name=BeanGraphService")
public class BeanGraphService implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static <T> boolean acceptAll(T t) {
        return true;
    }

    private static <T> boolean rejectAll(T t) {
        return false;
    }

    public BeanGraph getBeanGraph(String... excludingPackages) {
        List<Predicate<BeanNode>> predicates = Arrays.stream(excludingPackages)
                .map(packageFilter -> packageFilter.replace(".", "\\."))
                .map(packageFilter -> (Predicate<BeanNode>) ((BeanNode node) -> node.getBeanClassName().matches(".*" + packageFilter + ".*")))
                .collect(Collectors.toList());
        return getBeanGraphExcluding(predicates.toArray((Predicate<BeanNode>[]) new Predicate[predicates.size()]));
    }

    public BeanGraph getBeanGraphExcluding(Predicate<BeanNode>... excludes) {
        Predicate<BeanNode> exclude = Arrays.stream(excludes)
                .reduce(BeanGraphService::rejectAll, Predicate::or);
        return getBeanGraph(exclude, BeanGraphService::rejectAll);
    }

    public BeanGraph getBeanGraph(Predicate<BeanNode> exclude, Predicate<BeanNode> include) {
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

        return new BeanGraph(beanNameToNodeMap);
    }

    private void addDependency(BeanNode dependent, BeanNode dependency) {
        dependency.addDependent(dependent);
        dependent.addDependency(dependency);
    }

}
