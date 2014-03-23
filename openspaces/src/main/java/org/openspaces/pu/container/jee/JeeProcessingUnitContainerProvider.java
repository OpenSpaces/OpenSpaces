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

package org.openspaces.pu.container.jee;

import com.gigaspaces.start.Locator;
import org.jini.rio.boot.SharedServiceData;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ManifestClasspathAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension to the {@link org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider}
 * that can handle JEE processing units.
 *
 * @author kimchy
 */
public abstract class JeeProcessingUnitContainerProvider implements
    ApplicationContextProcessingUnitContainerProvider, 
    DeployableProcessingUnitContainerProvider,
    ManifestClasspathAwareProcessingUnitContainerProvider {

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.openspaces.core.cluster.ClusterInfo}
     * is stored.
     */
    public static final String CLUSTER_INFO_CONTEXT = "clusterInfo";

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.openspaces.core.properties.BeanLevelProperties}
     * is stored.
     */
    public static final String BEAN_LEVEL_PROPERTIES_CONTEXT = "beanLevelProperties";

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.springframework.context.ApplicationContext}
     * (loaded from the <code>pu.xml</code>) is stored.
     */
    public static final String APPLICATION_CONTEXT_CONTEXT = "applicationContext";

    public static final String JEE_CONTAINER_PROPERTY_NAME = "jee.container";

    public static final String DEFAULT_JEE_CONTAINER = "jetty";

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private File deployPath;

    private final List<Resource> configResources = new ArrayList<Resource>();

    private Iterable<URL> manifestURLs;

    private ApplicationContext parentContext;

    public abstract String getJeeContainerType();

    public Iterable<URL> getManifestURLs() {
        return this.manifestURLs;
    }

    public void setManifestUrls(Iterable<URL> manifestURLs) {
        this.manifestURLs = manifestURLs;
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

    public BeanLevelProperties getBeanLevelProperties() {
        return beanLevelProperties;
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

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    /**
     * Sets the class loader this processing unit container will load the application context with.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the deploy path where the exploded war jetty will work with is located.
     */
    public void setDeployPath(File warPath) {
        this.deployPath = warPath;
    }

    public File getDeployPath() {
        return deployPath;
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

    /**
     * Sets Spring parent {@link org.springframework.context.ApplicationContext} that will be used
     * when constructing this processing unit application context.
     */
    public void setParentContext(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    protected Iterable<String> getWebAppClassLoaderJars() {
        List<String> result = new ArrayList<String>();
        String gsLibOpt = System.getProperty(Locator.GS_LIB_OPTIONAL);
        result.add(System.getProperty("com.gs.pu-common", gsLibOpt + "pu-common"));
        result.add(System.getProperty("com.gs.web-pu-common", gsLibOpt + "web-pu-common"));
        return result;
    }
    protected Iterable<String> getWebAppClassLoaderClassPath() {
        List<String> result = new ArrayList<String>();
        result.add(getJeeContainerJarPath(getJeeContainerType()));
        return result;
    }

    protected ClassLoader getJeeClassLoader() throws Exception {
        return SharedServiceData.getJeeClassLoader(getJeeContainerType());
    }

    protected ResourceApplicationContext initApplicationContext() {
        Resource[] resources = configResources.toArray(new Resource[configResources.size()]);
        // create the Spring application context
        ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, parentContext);
        // add config information if provided
        if (getBeanLevelProperties() != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(getBeanLevelProperties(), getClusterInfo()));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(getBeanLevelProperties()));
        }
        if (getClusterInfo() != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(getClusterInfo()));
        }
        applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(getClusterInfo()));
        if (classLoader != null) {
            applicationContext.setClassLoader(classLoader);
        }
        return applicationContext;
    }

    public static String getJeeContainerJarPath(String jeeContainer) {
        return System.getProperty(Locator.GS_LIB_PLATFORM) + "openspaces/gs-openspaces-" + jeeContainer + ".jar";
    }

    public static String getJeeContainer(BeanLevelProperties properties) {
        return properties.getContextProperties().getProperty(JEE_CONTAINER_PROPERTY_NAME, DEFAULT_JEE_CONTAINER);
    }
}
