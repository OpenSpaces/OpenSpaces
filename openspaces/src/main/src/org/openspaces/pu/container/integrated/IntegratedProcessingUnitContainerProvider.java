package org.openspaces.pu.container.integrated;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.core.config.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.config.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>An {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer} provider. An integrated
 * processing unit container can be used to run a processing unit within an existing environemnt. An example
 * of what this existing environment will provide is the classpath that the processing unit will run with. Examples
 * for using the integrated processing unit container can be integration tests or running the processing
 * unit from within an IDE.
 *
 * <p>At its core the integrated processing unit container is built around Spring
 * {@link org.springframework.context.ApplicationContext} configured based on a set of config locations.
 *
 * <p>The provider allows for programmatic configuration of different processing unit aspects. It allows to configure
 * where the processing unit Spring context xml descriptors are located (by default it uses
 * <code>classpath*:/META-INF/pu/*.xml</code>). It also allows to set {@link org.openspaces.core.config.BeanLevelProperties}
 * and {@link org.openspaces.core.cluster.ClusterInfo} that will be injected to beans configured within the processing
 * unit.
 *
 * <p>For a runnable "main" processing unit container please see
 * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer#main(String[])}.
 *
 * @author kimchy
 */
public class IntegratedProcessingUnitContainerProvider implements ApplicationContextProcessingUnitContainerProvider {

    private ApplicationContext parentContext;

    private List configResources = new ArrayList();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    /**
     * Sets Spring parent {@link org.springframework.context.ApplicationContext} that will be used
     * when constructing this processing unit application context.
     */
    public void setParentContext(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * Sets the {@link org.openspaces.core.config.BeanLevelProperties} that will be used to configure this
     * processing unit. When constructing the container this provider will automatically add to the
     * application context both {@link org.openspaces.core.config.BeanLevelPropertyBeanPostProcessor} and
     * {@link org.openspaces.core.config.BeanLevelPropertyPlaceholderConfigurer} based on this bean level
     * properties.
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Sets the {@link org.openspaces.core.cluster.ClusterInfo} that will be used to configure this
     * processing unit. When constructing the container this provider will automatically add to the
     * application context the {@link org.openspaces.core.cluster.ClusterInfoBeanPostProcessor} in order
     * to allow injection of cluster info into beans that implement {@link org.openspaces.core.cluster.ClusterInfoAware}.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Sets the class loader this processing unit container will load the application context with.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * <p>Adds a config location using Springs {@link org.springframework.core.io.Resource} abstraction. This
     * config location represents a Spring xml context.
     *
     * <p>Note, once a config location is added that default location used when no config location is defined
     * won't be used (the default location is <code>classpath*:/META-INF/pu/*.xml</code>).
     */
    public void addConfigLocation(Resource resource) {
        this.configResources.add(resource);
    }

    /**
     * Adds a config location based on a String description using Springs
     * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
     *
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void addConfigLocation(String path) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
        for (int i = 0; i < resources.length; i++) {
            addConfigLocation(resources[i]);
        }
    }

    /**
     * <p>Creates a new {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer} based on
     * the configured parameters.
     *
     * <p>If {@link #addConfigLocation(org.springframework.core.io.Resource)} or
     * {@link #addConfigLocation(String)} were used, the Spring xml context will be read based on the provided
     * locations. If no config location was provided the default config location will be
     * <code>classpath*:/META-INF/pu/*.xml</code>.
     *
     * <p>If {@link #setBeanLevelProperties(org.openspaces.core.config.BeanLevelProperties)} is set will use
     * the configured bean level properties in order to configure the application context and specific beans
     * within it based on properties. This is done by adding {@link org.openspaces.core.config.BeanLevelPropertyBeanPostProcessor}
     * and {@link org.openspaces.core.config.BeanLevelPropertyPlaceholderConfigurer} to the application context.
     *
     * <p>If {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)} is set will use it to inject
     * {@link org.openspaces.core.cluster.ClusterInfo} into beans that implement
     * {@link org.openspaces.core.cluster.ClusterInfoAware}.
     *
     * @return An {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer} instance
     * @throws CannotCreateContainerException
     */
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
