/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.jee.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.webapp.WebAppContext;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class JettyJeeProcessingUnitContainerProvider implements JeeProcessingUnitContainerProvider {

    public final static String DEFAULT_JETTY_PLAIN_PU = "/org/openspaces/pu/container/jee/jetty/jetty.plain.pu.xml";

    public final static String DEFAULT_JETTY_SHARED_PU = "/org/openspaces/pu/container/jee/jetty/jetty.shared.pu.xml";

    public static final String DEFAULT_JETTY_PU_LOCATION_SYSPROP = "com.gs.pu.web.jetty.defaultPuLocation";

    public static final String SHARED_PROP = "jetty.shared";

    private static final Log logger = LogFactory.getLog(JettyJeeProcessingUnitContainerProvider.class);

    private ApplicationContext parentContext;

    private List<Resource> configResources = new ArrayList<Resource>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private File deployPath;

    /**
     * Sets Spring parent {@link org.springframework.context.ApplicationContext} that will be used
     * when constructing this processing unit application context.
     */
    public void setParentContext(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * Sets the {@link org.openspaces.core.properties.BeanLevelProperties} that will be used to
     * configure this processing unit. When constructing the container this provider will
     * automatically add to the application context both
     * {@link org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor} and
     * {@link org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer} based on this
     * bean level properties.
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Sets the {@link org.openspaces.core.cluster.ClusterInfo} that will be used to configure this
     * processing unit. When constructing the container this provider will automatically add to the
     * application context the {@link org.openspaces.core.cluster.ClusterInfoBeanPostProcessor} in
     * order to allow injection of cluster info into beans that implement
     * {@link org.openspaces.core.cluster.ClusterInfoAware}.
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
     * Adds a config location using Springs {@link org.springframework.core.io.Resource}
     * abstraction. This config location represents a Spring xml context.
     *
     * <p>Note, once a config location is added that default location used when no config location is
     * defined won't be used (the default location is <code>classpath*:/META-INF/spring/pu.xml</code>).
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
        for (Resource resource : resources) {
            addConfigLocation(resource);
        }
    }

    public void setDeployPath(File warPath) {
        this.deployPath = warPath;
    }

    /**
     *
     */
    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        if (configResources.size() == 0) {
            try {
                addConfigLocation(DEFAULT_PU_CONTEXT_LOCATION);
                boolean foundValidResource = false;
                for (Resource resource : configResources) {
                    if (resource.exists()) {
                        foundValidResource = true;
                        break;
                    }
                }
                if (!foundValidResource) {
                    String defaultLocation = System.getProperty(DEFAULT_JETTY_PU_LOCATION_SYSPROP);
                    if (defaultLocation == null) {
                        defaultLocation = DEFAULT_JETTY_PLAIN_PU;
                        String sharedProp = (String) beanLevelProperties.getContextProperties().get(SHARED_PROP);
                        if (sharedProp != null && sharedProp.equalsIgnoreCase("true")) {
                            defaultLocation = DEFAULT_JETTY_SHARED_PU;
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("No custom META-INF/spring/pu.xml found, using default [" + defaultLocation + "]");
                        }
                        Resource resource = new ClassPathResource(defaultLocation, JettyJeeProcessingUnitContainerProvider.class);
                        addConfigLocation(resource);
                    } else {
                        addConfigLocation(defaultLocation);
                    }
                    if (configResources.size() == 0 || !configResources.get(0).exists()) {
                        throw new CannotCreateContainerException("Faield to read default pu file [" + defaultLocation + "]");
                    }
                }
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to read config files from " + DEFAULT_PU_CONTEXT_LOCATION, e);
            }
        }
        if (clusterInfo != null) {
            ClusterInfoParser.guessSchema(clusterInfo);
        }

        beanLevelProperties.getContextProperties().setProperty("jee.deployPath", deployPath.getAbsolutePath());

        Resource[] resources = configResources.toArray(new Resource[configResources.size()]);
        // create the Spring application context
        ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, parentContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties, clusterInfo));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        if (clusterInfo != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));
        if (classLoader != null) {
            applicationContext.setClassLoader(classLoader);
        }
        // "start" the application context
        applicationContext.refresh();

        JettyHolder jettyHolder = (JettyHolder) applicationContext.getBean("jettyHolder");

        int retryPortCount = 20;
        try {
            retryPortCount = (Integer) applicationContext.getBean("retryPortCount");
        } catch (Exception e) {
            // do nothing
        }

        try {
            boolean success = false;
            for (int i = 0; i < retryPortCount; i++) {
                try {
                    jettyHolder.start();
                    success = true;
                    break;
                } catch (BindException e) {
                    try {
                        jettyHolder.stop();
                    } catch (Exception e1) {
                        // ignore
                    }
                    for (Connector connector : jettyHolder.getServer().getConnectors()) {
                        connector.setPort(connector.getPort() + 1);
                        if (connector instanceof AbstractConnector) {
                            ((AbstractConnector) connector).setConfidentialPort(connector.getConfidentialPort() + 1);
                        }
                    }
                }
            }
            if (!success) {
                throw new CannotCreateContainerException("Failed to bind jetty to port with retries [" + retryPortCount + "]");
            }
        } catch (CannotCreateContainerException e) {
            throw e;
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to start jetty server", e);
        }
        try {
            WebAppContext webAppContext = (WebAppContext) applicationContext.getBean("webAppContext");

            webAppContext.setExtractWAR(true);
            webAppContext.setCopyWebDir(false);

            webAppContext.setAttribute(APPLICATION_CONTEXT_CONTEXT, applicationContext);
            webAppContext.setAttribute(CLUSTER_INFO_CONTEXT, clusterInfo);
            webAppContext.setAttribute(BEAN_LEVEL_PROPERTIES_CONTEXT, beanLevelProperties);

            HandlerContainer container = jettyHolder.getServer();

            Handler[] contexts = jettyHolder.getServer().getChildHandlersByClass(ContextHandlerCollection.class);
            if (contexts != null && contexts.length > 0) {
                container = (HandlerContainer) contexts[0];
            } else {
                while (container != null) {
                    if (container instanceof HandlerWrapper) {
                        HandlerWrapper wrapper = (HandlerWrapper) container;
                        Handler handler = wrapper.getHandler();
                        if (handler == null)
                            break;
                        if (handler instanceof HandlerContainer)
                            container = (HandlerContainer) handler;
                        else
                            throw new IllegalStateException("No container");
                    }
                    throw new IllegalStateException("No container");
                }
            }

            container.addHandler(webAppContext);
            if (container.isStarted() || container.isStarting()) {
                try {
                    webAppContext.start();
                } catch (Exception e) {
                    throw new CannotCreateContainerException("Failed to start web app context", e);
                }
            }
            return new JettyProcessingUnitContainer(applicationContext, webAppContext, container, jettyHolder);
        } catch (Exception e) {
            try {
                jettyHolder.stop();
            } catch (Exception e1) {
                logger.debug("Failed to stop jetty after an error occured, ignoring", e);
            }
            throw new CannotCreateContainerException("Failed to start web application", e);
        }
    }
}