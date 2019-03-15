package hu.gerab.spring.bean.graph;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class BeanNode {

    public static final Comparator<BeanNode> BEAN_NODE_NAME_COMPARATOR = Comparator.comparing(BeanNode::getName);

    private final BeanDefinition beanDefinition;
    private final String name;
    private final String simpleClassName;

    private TreeSet<BeanNode> dependencies = new TreeSet<>(BEAN_NODE_NAME_COMPARATOR);
    private TreeSet<BeanNode> dependents = new TreeSet<>(BEAN_NODE_NAME_COMPARATOR);

    public BeanNode(BeanDefinition beanDefinition, String beanDefinitionName) {
        this.beanDefinition = beanDefinition;
        this.name = beanDefinitionName;
        simpleClassName = Optional.ofNullable(getBeanClassName())
                .map(name -> name.split("\\."))
                .filter(values -> !ObjectUtils.isEmpty(values))
                .map(values -> values[values.length - 1])
                .orElse("");
    }

    public void addDependent(BeanNode node) {
        dependents.add(node);
    }

    public void addDependency(BeanNode node) {
        dependencies.add(node);
    }

    public TreeSet<BeanNode> getDependencies() {
        return dependencies;
    }

    public TreeSet<BeanNode> getDependents() {
        return dependents;
    }

    public String getName() {
        return name;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    @Override
    public String toString() {
        return "BeanNode{" +
                "name='" + name + '\'' +
                ", beanDefinition=" + beanDefinition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeanNode beanNode = (BeanNode) o;
        return Objects.equals(name, beanNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /////////////////////////////////// delegated getters //////////////////////////////////////////

    public String getParentName() {
        return beanDefinition.getParentName();
    }

    public String getBeanClassName() {
        return beanDefinition.getBeanClassName();
    }

    public String getFactoryBeanName() {
        return beanDefinition.getFactoryBeanName();
    }

    public String getFactoryMethodName() {
        return beanDefinition.getFactoryMethodName();
    }

    public String getScope() {
        return beanDefinition.getScope();
    }

    public boolean isLazyInit() {
        return beanDefinition.isLazyInit();
    }

    public String[] getDependsOn() {
        return beanDefinition.getDependsOn();
    }

    public boolean isAutowireCandidate() {
        return beanDefinition.isAutowireCandidate();
    }

    public boolean isPrimary() {
        return beanDefinition.isPrimary();
    }

    public boolean isSingleton() {
        return beanDefinition.isSingleton();
    }

    public boolean isPrototype() {
        return beanDefinition.isPrototype();
    }

    public boolean isAbstract() {
        return beanDefinition.isAbstract();
    }

    public int getRole() {
        return beanDefinition.getRole();
    }

    public String getDescription() {
        return beanDefinition.getDescription();
    }

    public String getResourceDescription() {
        return beanDefinition.getResourceDescription();
    }

    public boolean hasAttribute(String name) {
        return beanDefinition.hasAttribute(name);
    }

    public String[] attributeNames() {
        return beanDefinition.attributeNames();
    }
}
