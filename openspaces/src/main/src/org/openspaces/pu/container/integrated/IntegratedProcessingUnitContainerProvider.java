package org.openspaces.pu.container.integrated;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.core.config.BeanLevelPropertiesAware;
import org.openspaces.core.config.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.config.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class IntegratedProcessingUnitContainerProvider implements ProcessingUnitContainerProvider,
        BeanLevelPropertiesAware, ClusterInfoAware {

    private ApplicationContext parentContext;

    private List configResources = new ArrayList();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    public IntegratedProcessingUnitContainerProvider() {

    }

    public void setParentContext(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addConfigLocation(Resource resource) {
        this.configResources.add(resource);
    }

    /**
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void addConfigLocation(String path) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
        for (int i = 0; i < resources.length; i++) {
            addConfigLocation(resources[i]);
        }
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        if (configResources.size() == 0) {
            try {
                addConfigLocation("classpath*:/META-INF/pu/*.xml");
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to read config files from [/META_INF/pu/*.xml]", e);
            }
        }
        Resource[] resources = (Resource[]) configResources.toArray(new Resource[configResources.size()]);
        // create the Spring application context
        ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, parentContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        if (clusterInfo != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        if (classLoader != null) {
            applicationContext.setClassLoader(classLoader);
        }
        // "start" the application context
        applicationContext.refresh();

        return new IntegratedProcessingUnitContainer(applicationContext);
    }
}
