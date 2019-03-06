package hu.gerab.spring.bean.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ManagedResource(objectName = "hu.gerab:name=BeanGraphService")
public class BeanGraphService implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanGraphService.class);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        System.out.println("appctx set!!");
    }

    public BeanGraph getBeanGraph(String... packageFilters) {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        //TODO classcastEx
        ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext) applicationContext).getBeanFactory();

        Map<String, BeanNode> beanNameToNodeMap = new HashMap<>();

        beanRegistration: for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);

            for (String filter : packageFilters) {
                if (beanDefinition.getBeanClassName().matches(filter)) {
                    continue beanRegistration;
                }
            }

            BeanNode beanNode = new BeanNode(beanDefinition, beanDefinitionName);
            beanNameToNodeMap.put(beanDefinitionName, beanNode);
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

    @ManagedAttribute
    public boolean isUp() {
        return true;
    }
}
