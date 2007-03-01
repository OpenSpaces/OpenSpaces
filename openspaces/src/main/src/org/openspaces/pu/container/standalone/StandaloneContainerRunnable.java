package org.openspaces.pu.container.standalone;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.core.config.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.config.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A standalone container runnable allowing to start a Spring based application context based on the provided
 * parameters ({@link org.openspaces.core.config.BeanLevelProperties}, {@link org.openspaces.core.cluster.ClusterInfo},
 * and a list of config locations).
 *
 * <p>This runnable allows to start a processing unit container within a running thread, mainly allowing for custom
 * class loader created based on the processing unit structure to be scoped only within the running thread. When
 * using {@link org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer#main(String[])} this feature
 * is not requried but when integrating the standalone container within another application this allows not to corrupt
 * the external environment thread context class loader.
 *
 * @author kimchy
 */
public class StandaloneContainerRunnable implements Runnable {

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private List configLocations;

    private boolean running;

    private boolean initialized;

    private ResourceApplicationContext applicationContext;

    /**
     * Constructs a new standalone container runnable based on the provided configuraion set parameters.
     *
     * @param beanLevelProperties The properties based configuration for Spring context
     * @param clusterInfo         The cluster info configuration
     * @param configLocations     List of config locations (string based)
     */
    public StandaloneContainerRunnable(BeanLevelProperties beanLevelProperties, ClusterInfo clusterInfo, List configLocations) {
        this.beanLevelProperties = beanLevelProperties;
        this.clusterInfo = clusterInfo;
        this.configLocations = configLocations;
    }

    /**
     * Constructs a new Spring {@link org.springframework.context.ApplicationContext} based on the configured list
     * of config locations. Also uses the provided {@link org.openspaces.core.cluster.ClusterInfo} and
     * {@link org.openspaces.core.config.BeanLevelProperties} in order to further config the application context.
     */
    public void run() {
        try {
            Resource[] resources;
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            if (configLocations.size() == 0) {
                try {
                    resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/spring/*.xml");
                } catch (IOException e) {
                    throw new CannotCreateContainerException("Failed to parse pu xml", e);
                }
            } else {
                List tempResourcesList = new ArrayList();
                for (Iterator it = configLocations.iterator(); it.hasNext();) {
                    String configLocation = (String) it.next();
                    try {
                        Resource[] tempResources = pathMatchingResourcePatternResolver.getResources(configLocation);
                        for (int i = 0; i < tempResources.length; i++) {
                            tempResourcesList.add(tempResources[i]);
                        }
                    } catch (IOException e) {
                        throw new CannotCreateContainerException("Failed to parse pu xml from location [" + configLocation + "]");
                    }
                }
                resources = (Resource[]) tempResourcesList.toArray(new Resource[tempResourcesList.size()]);
            }
            // create the Spring application context
            applicationContext = new ResourceApplicationContext(resources, null);
            // add config information if provided   
            if (beanLevelProperties != null) {
                applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
                applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
            }
            if (clusterInfo != null) {
                applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
            }
            // "start" the application context
            applicationContext.refresh();
            this.running = true;
        } catch (Throwable t) {
            // TODO handle it better
            t.printStackTrace();
        } finally {
            initialized = true;
        }

        // Just hang in there until we get stopped
        while (isRunning() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // do nothing, the while clause will simply exit
            }
        }
        applicationContext.destroy();
    }

    /**
     * Returns <code>true</code> if the application context initialized successfully.
     */
    public synchronized boolean isInitialized() {
        return initialized;
    }

    /**
     * Return <code>true</code> if this runnable is currently running.
     */
    public synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Stop this currently running container runnable.
     */
    public synchronized void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
    }

    /**
     * Returns Spring application context used as the backend for this container.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
