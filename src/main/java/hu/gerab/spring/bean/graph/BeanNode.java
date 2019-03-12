package hu.gerab.spring.bean.graph;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;

import java.util.Comparator;
import java.util.TreeSet;

public class BeanNode {

    public static final Comparator<BeanNode> BEAN_NODE_NAME_COMPARATOR = Comparator.comparing(BeanNode::getName);

    private BeanDefinition beanDefinition;
    private String name;
    private TreeSet<BeanNode> dependencies = new TreeSet<>(BEAN_NODE_NAME_COMPARATOR);
    private TreeSet<BeanNode> dependents = new TreeSet<>(BEAN_NODE_NAME_COMPARATOR);

    public BeanNode(BeanDefinition beanDefinition, String beanDefinitionName) {
        this.beanDefinition = beanDefinition;
        this.name = beanDefinitionName;
    }

    public static boolean isRoot(BeanDefinition beanDefinition) {
        String[] dependsOn = beanDefinition.getDependsOn();
        return dependsOn == null || dependsOn.length == 0;
    }

    boolean isRoot() {
        return dependencies.isEmpty();
    }

    boolean isLeaf() {
        return dependents.isEmpty();
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

    protected BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

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

    public ConstructorArgumentValues getConstructorArgumentValues() {
        return beanDefinition.getConstructorArgumentValues();
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

    public BeanDefinition getOriginatingBeanDefinition() {
        return beanDefinition.getOriginatingBeanDefinition();
    }

    public boolean hasAttribute(String name) {
        return beanDefinition.hasAttribute(name);
    }

    public String[] attributeNames() {
        return beanDefinition.attributeNames();
    }

    @Override
    public String toString() {
        return "BeanNode{" +
                "name='" + name + '\'' +
                ", beanDefinition=" + beanDefinition +
                '}';
    }
}
