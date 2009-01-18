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

package org.openspaces.pu.container.jee.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.SharedServiceData;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;

/**
 * Bootstap servlet context listener allowing to get the {@link org.openspaces.core.cluster.ClusterInfo},
 * {@link org.openspaces.core.properties.BeanLevelProperties}, and handle an optional pu.xml file within
 * META-INF/spring by loading it.
 *
 * <p>The different web containers support in OpenSpaces will use {@link #prepareForBoot(java.io.File, org.openspaces.core.cluster.ClusterInfo, org.openspaces.core.properties.BeanLevelProperties)}
 * before the web application is started. It will basically marshall the ClusterInfo and BeanLevelProperties so they
 * can be read when the {@link #contextInitialized(javax.servlet.ServletContextEvent)} is called. It will also
 * change the web.xml in order to add this class as a context listener.
 *
 * <p>During context initializtion, the marshalled ClusterInfo and BeanLevelProperties can be read and
 * put in the servlet context (allowing us to support any web container).
 *
 * <p>If there is a pu.xml file, it will be started as well, with the application context itself set in the
 * servlet context (can then be used by the {@link org.openspaces.pu.container.jee.context.ProcessingUnitContextLoaderListener}
 * as well as all its beans read and set in the servlet context under their respective names.
 *
 * @author kimchy
 */
public class BootstrapWebApplicationContextListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(BootstrapWebApplicationContextListener.class);

    private static final String BOOTSTRAP_CONTEXT_KEY = BootstrapWebApplicationContextListener.class.getName() + ".bootstraped";

    private static final String MARSHALLED_STORE = "/WEB-INF/gsstore";
    private static final String MARSHALLED_CLUSTER_INFO = MARSHALLED_STORE + "/cluster-info";
    private static final String MARSHALLED_BEAN_LEVEL_PROPERTIES = MARSHALLED_STORE + "/bean-level-properties";

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();

        Boolean bootstraped = (Boolean) servletContext.getAttribute(BOOTSTRAP_CONTEXT_KEY);
        if (bootstraped != null && bootstraped) {
            logger.debug("Already performed bootstrap, ignoring");
            return;
        }
        servletContext.setAttribute(BOOTSTRAP_CONTEXT_KEY, true);
        
        logger.info("Booting OpenSpaces Web Application Support");
        ClusterInfo clusterInfo = null;
        BeanLevelProperties beanLevelProperties = null;

        InputStream is = servletContext.getResourceAsStream(MARSHALLED_CLUSTER_INFO);
        if (is != null) {
            try {
                clusterInfo = (ClusterInfo) objectFromByteBuffer(FileCopyUtils.copyToByteArray(is));
                servletContext.setAttribute(JeeProcessingUnitContainerProvider.CLUSTER_INFO_CONTEXT, clusterInfo);
            } catch (Exception e) {
                logger.warn("Failed to read cluster info from " + MARSHALLED_CLUSTER_INFO, e);
            }
        } else {
            logger.debug("No cluster info found at " + MARSHALLED_CLUSTER_INFO);
        }
        is = servletContext.getResourceAsStream(MARSHALLED_BEAN_LEVEL_PROPERTIES);
        if (is != null) {
            try {
                beanLevelProperties = (BeanLevelProperties) objectFromByteBuffer(FileCopyUtils.copyToByteArray(is));
                servletContext.setAttribute(JeeProcessingUnitContainerProvider.BEAN_LEVEL_PROPERTIES_CONTEXT, beanLevelProperties);
            } catch (Exception e) {
                logger.warn("Failed to read bean level properties from " + MARSHALLED_BEAN_LEVEL_PROPERTIES, e);
            }
        } else {
            logger.debug("No bean level properties found at " + MARSHALLED_BEAN_LEVEL_PROPERTIES);
        }

        Resource[] resources = null;
        try {
            resources = new PathMatchingResourcePatternResolver().getResources(ApplicationContextProcessingUnitContainerProvider.DEFAULT_PU_CONTEXT_LOCATION);
        } catch (IOException e) {
            // ignore, don't load it
        }
        if (resources != null && resources.length > 0) {
            logger.debug("Loading [" + ApplicationContextProcessingUnitContainerProvider.DEFAULT_PU_CONTEXT_LOCATION + "]");
            // create the Spring application context
            ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, null);
            // add config information if provided
            if (beanLevelProperties != null) {
                applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties, clusterInfo));
                applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
            }
            if (clusterInfo != null) {
                applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
            }
            applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));
            // "start" the application context
            applicationContext.refresh();

            servletContext.setAttribute(JeeProcessingUnitContainerProvider.APPLICATION_CONTEXT_CONTEXT, applicationContext);
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                servletContext.setAttribute(beanName, applicationContext.getBean(beanName));
            }
        } else {
            logger.debug("No [" + ApplicationContextProcessingUnitContainerProvider.DEFAULT_PU_CONTEXT_LOCATION + "] to load");
        }

        // set the class loader used so the service bean can use it
        if (clusterInfo != null) {
            SharedServiceData.putWebAppClassLoader(clusterInfo.getName() + clusterInfo.getRunningNumber(), Thread.currentThread().getContextClassLoader());
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) servletContextEvent.getServletContext().getAttribute(JeeProcessingUnitContainerProvider.APPLICATION_CONTEXT_CONTEXT);
        if (applicationContext != null && applicationContext.isActive()) {
            applicationContext.close();
        }
    }

    public static void prepareForBoot(File warPath, ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties) throws Exception {
        File gsStore = new File(warPath, MARSHALLED_STORE);
        gsStore.mkdirs();
        if (clusterInfo != null) {
            FileCopyUtils.copy(objectToByteBuffer(clusterInfo), new File(warPath, MARSHALLED_CLUSTER_INFO));
        }
        if (beanLevelProperties != null) {
            FileCopyUtils.copy(objectToByteBuffer(beanLevelProperties), new File(warPath, MARSHALLED_BEAN_LEVEL_PROPERTIES));
        }

        new File(warPath, "/WEB-INF/web.xml").renameTo(new File(warPath, "/WEB-INF/web.xml.orig"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(warPath, "/WEB-INF/web.xml.orig"))));
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(warPath, "/WEB-INF/web.xml"), false))));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replace("org.springframework.web.context.ContextLoaderListener", "org.openspaces.pu.container.jee.context.ProcessingUnitContextLoaderListener");
            if (line.indexOf("<web-app") != -1) {
                writer.println(line);
                if (line.indexOf('>') == -1) {
                    // the tag is not closed
                    while ((line = reader.readLine()) != null) {
                        if (line.indexOf('>') == -1) {
                            writer.println(line);
                        } else {
                            break;
                        }
                    }
                    writer.println(line);
                }
                // now append our context listener
                writer.println("<!-- GigaSpaces CHANGE START: Boot Listener -->");
                writer.println("<listener>");
                writer.println("    <listener-class>" + BootstrapWebApplicationContextListener.class.getName() + "</listener-class>");
                writer.println("</listener>");
                writer.println("<!-- GigaSpaces CHANGE END: Boot Listener -->");
            } else {
                writer.println(line);
            }
        }
        writer.close();
        reader.close();
    }

    public static Object objectFromByteBuffer(byte[] buffer) throws Exception {
        if (buffer == null)
            return null;

        ByteArrayInputStream inStream = new ByteArrayInputStream(buffer);
        ObjectInputStream in = new ObjectInputStream(inStream);
        Object retval = in.readObject();
        in.close();

        return retval;
    }

    public static byte[] objectToByteBuffer(Object obj) throws Exception {
        byte[] result = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(obj);
        out.flush();
        result = outStream.toByteArray();
        out.close();
        outStream.close();

        return result;
    }
}
