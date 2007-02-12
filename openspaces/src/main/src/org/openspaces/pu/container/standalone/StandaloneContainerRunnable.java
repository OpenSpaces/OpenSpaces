package org.openspaces.pu.container.standalone;

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
 * @author kimchy
 */
public class StandaloneContainerRunnable implements Runnable {

    private BeanLevelProperties beanLevelProperties;

    private List configLocations;

    private boolean running;

    private boolean initialized;

    private ResourceApplicationContext applicationContext;

    public StandaloneContainerRunnable(BeanLevelProperties beanLevelProperties, List configLocations) {
        this.beanLevelProperties = beanLevelProperties;
        this.configLocations = configLocations;
    }

    public void run() {
        try {
            Resource[] resources;
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            if (configLocations.size() == 0) {
                try {
                    resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/pu/*.xml");
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
            // "start" the application context
            applicationContext.refresh();
        } finally {
            initialized = true;
        }

        while (isRunning() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // do nothing, the while clause will simply exit
            }
        }
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized void stop() {
        this.running = true;
        Thread.currentThread().interrupt();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
