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

package org.openspaces.pu.container.jee.glassfish;

import com.j_spaces.kernel.Environment;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import org.apache.catalina.startup.TldConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embed.Application;
import org.glassfish.embed.Server;
import org.jini.rio.boot.CommonClassLoader;
import org.jvnet.hk2.component.Habitat;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ClassLoaderAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener;
import org.openspaces.pu.container.support.BeanLevelPropertiesUtils;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A container allowing to support Glassfish V3 prelude embedded as a web container.
 *
 * <p>The glassfish server is a shared one between all applications. The first application deployed will
 * start the glassfish server. The last one undeployed will stop it (though currently it is not closed
 * properly).
 *
 * <p>Since there is no way to cleanly stop glassfish (currently), a port is assigned to glassfish on creation
 * and is used throughout the lifecycle of the JVM. The port starts from 9008 (can be set using <code>com.gs.glassfish.port</code>
 * system property) and tries for 20 times until it manages to find a usuable port. Finding usuable ports is done through
 * locks on files, so it only works for used ports between different JVMs that use this container. Files are used since
 * it is not possible to start stop glassfish cleanly now.
 *
 * <p>The glassfish work directory is created under <code>GSHOME/work/glassfish/[portNumber]</code>.
 *
 * <p>A glassfish <code>domain.xml</code> file is used in order to configure the glassfish instance. By default, it is
 * located under <code>GSHOME/lib/glassfish/domain.xml</code> and can be changed using <code>com.gs.glassfish.domainXml</code>
 * system property. The domain xml uses a single main GigaSpaces property called <code>com.gs.glassfish.port</code> and is
 * changed dynamically and created under the glassfish work directory (generated-domain.xml).
 *
 * <p>Note, this overrides the built in Glassfish {@link org.glassfish.embed.Server} code so it won't generate dynamically
 * the http listener and virtual server. This allows for greater control over how the Glassfish is conifugred by exposing
 * the full domain xml to the user.
 *
 * <p>Any other ${...} notation will be replaced (if possible) in the domain.xml file using context propertes as well.
 *
 * @author kimchy
 */
public class GlassfishJeeProcessingUnitContainerProvider implements JeeProcessingUnitContainerProvider, ClassLoaderAwareProcessingUnitContainerProvider {

    private static final Log logger = LogFactory.getLog(GlassfishJeeProcessingUnitContainerProvider.class);

    public final static String DEFAULT_GLASSFISH_PU = "/META-INF/spring/glassfish.pu.xml";

    public final static String INTERNAL_GLASSFISH_PU_PREFIX = "/org/openspaces/pu/container/jee/glassfish/glassfish.";

    public final static String INSTANCE_SHARD = "shared";

    public static final String GLASSFISH_LOCATION_PREFIX_SYSPROP = "com.gs.pu.jee.glassfish.pu.locationPrefix";


    private ApplicationContext parentContext;

    private List<Resource> configResources = new ArrayList<Resource>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private File deployPath;

    private static File glassfishGlobalWorkDir;
    private static File glassfishInstanceWorkDir;
    // HACK to manage ports, store a JVM level file lock with the port number as file
    // this means that once started, Glassfish will use that port even when stopped.
    // in glassfish (the version we use), even when stopping the server causes it to still use the same port
    private static RandomAccessFile portFile;
    private static FileLock portFileLock;
    private static int portNumber;

    static {
        glassfishGlobalWorkDir = new File(System.getProperty("com.gs.work", Environment.getHomeDirectory() + "/work") + "/glassfish");
        File portDir = new File(glassfishGlobalWorkDir, "ports");
        portDir.mkdirs();
        int startFromPort = Integer.getInteger("com.gs.glassfish.port", 9008);
        int retryCount = 20;
        for (int i = 0; i < retryCount; i++) {
            portNumber = startFromPort + i;
            File portF = new File(portDir, portNumber + ".port");
            try {
                portFile = new RandomAccessFile(portF, "rw");
            } catch (FileNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to open port marker file [" + portF.getAbsolutePath() + "]", e);
                }
                // can't get this one, continue
                continue;
            }
            try {
                portFileLock = portFile.getChannel().tryLock();
                if (portFileLock == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Can't get lock for [" + portF.getAbsolutePath() + "], try another");
                    }
                    continue;
                }
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Faield to get lock file for [" + portF.getAbsolutePath() + "]", e);
                }
                // failed to get the lock, continue
                continue;
            }
            // all is well, break
            break;
        }
        if (portFileLock == null) {
            portNumber = -1;
            throw new IllegalStateException("Failed to acquire port file lock, tried for [" + retryCount + "], basePort [" + startFromPort + "]");
        }
        glassfishInstanceWorkDir = new File(glassfishGlobalWorkDir, "" + portNumber);
        glassfishInstanceWorkDir.mkdirs();
        System.setProperty("com.gs.glassfish.port", "" + portNumber);
        logger.info("Glassfish instance will use port [" + portNumber + "] and location [" + glassfishInstanceWorkDir.getAbsolutePath() + "]");
    }

    public static int getPortNumber() {
        return portNumber;
    }

    public static File getGlassfishGlobalWorkDir() {
        return glassfishGlobalWorkDir;
    }

    public static File getGlassfishInstanceWorkDir() {
        return glassfishInstanceWorkDir;
    }

    private static final ThreadLocal<ApplicationContext> currentApplicationContext = new ThreadLocal<ApplicationContext>();

    private static final ThreadLocal<ClusterInfo> currentClusterInfo = new ThreadLocal<ClusterInfo>();

    private static final ThreadLocal<BeanLevelProperties> currentBeanLevelProperties = new ThreadLocal<BeanLevelProperties>();

    /**
     * Allows to get the current application context (loaded from <code>pu.xml</code>) during web application
     * startup. Can be used to access beans defined within it (like a Space) by components loaded (such as
     * session storage). Note, this is only applicable during web application startup. It is cleared right
     * afterwards.
     */
    public static ApplicationContext getCurrentApplicationContext() {
        return currentApplicationContext.get();
    }

    /**
     * Intenrall used to set the applicationn context loaded from the <code>pu.xml</code> on a thread local
     * so components within the web container (such as session storge) will be able to access it during
     * startup time of the web application using {@link #getCurrentApplicationContext()}.
     */
    private static void setCurrentApplicationContext(ApplicationContext applicationContext) {
        currentApplicationContext.set(applicationContext);
    }

    public static ClusterInfo getCurrentClusterInfo() {
        return currentClusterInfo.get();
    }

    private static void setCurrentClusterInfo(ClusterInfo clusterInfo) {
        currentClusterInfo.set(clusterInfo);
    }

    public static BeanLevelProperties getCurrentBeanLevelProperties() {
        return currentBeanLevelProperties.get();
    }

    private static void setCurrentBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        currentBeanLevelProperties.set(beanLevelProperties);
    }

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

    /**
     * Sets the deploy path where the exploded war jetty will work with is located.
     */
    public void setDeployPath(File warPath) {
        this.deployPath = warPath;
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        Resource glassfishPuResource = new ClassPathResource(DEFAULT_GLASSFISH_PU);
        if (!glassfishPuResource.exists()) {
            String defaultLocation = System.getProperty(GLASSFISH_LOCATION_PREFIX_SYSPROP, INTERNAL_GLASSFISH_PU_PREFIX) + "shared.pu.xml";
            glassfishPuResource = new ClassPathResource(defaultLocation);
            if (!glassfishPuResource.exists()) {
                throw new CannotCreateContainerException("Failed to read internal pu file [" + defaultLocation + "] as well as user defined [" + DEFAULT_GLASSFISH_PU + "]");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Using internal bulit in glassfish pu.xml from [" + defaultLocation + "]");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using user specific glassfish pu.xml from [" + DEFAULT_GLASSFISH_PU + "]");
            }
        }
        addConfigLocation(glassfishPuResource);

        if (clusterInfo != null) {
            ClusterInfoParser.guessSchema(clusterInfo);
        }

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

        // process the domain XML
        String domainXmlLocation = System.getProperty("com.gs.glassfish.domainXml", Environment.getHomeDirectory() + "/lib/glassfish/domain.xml");
        File domainXml = new File(domainXmlLocation);
        if (!domainXml.exists()) {
            throw new IllegalStateException("Failed to find domain xml to process from [" + domainXml.getAbsolutePath() + "]");
        }
        File generatedDomainXml = new File(getGlassfishInstanceWorkDir(), "generated-domain.xml");
        try {
            FileCopyUtils.copy(domainXml, generatedDomainXml);
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, generatedDomainXml);
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to handle domain xml processing", e);
        }

        GlassfishServer server;
        try {
            // TODO pass the domain xml path (bean context with system property)
            server = new GlassfishServer(portNumber, generatedDomainXml.toURL());
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to create glassfish server", e);
        }
        GlassfishHolder glassfishHolder = new SharedGlassfishHolder(server);
        try {
            glassfishHolder.start();
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to start glassfish server", e);
        }

        try {
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, new File(deployPath, "WEB-INF/web.xml"));
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to resolve properties on WEB-INF/web.xml", e);
        }
        // TODO post process glassfish files

        try {
            setCurrentApplicationContext(applicationContext);
            setCurrentBeanLevelProperties(beanLevelProperties);
            setCurrentClusterInfo(clusterInfo);

            BootstrapWebApplicationContextListener.prepareForBoot(deployPath, clusterInfo, beanLevelProperties);

            // we disable the smart getUrl in the common class loader so the JSP classpath will be built correclty
            CommonClassLoader.getInstance().setDisableSmartGetUrl(true);

            // we want to scan parent class loaders for TLDs
            TldConfig.setScanParentTldListener(true);
            URL[] commonURLs = CommonClassLoader.getInstance().getURLs();
            StringBuilder sb = new StringBuilder();
            for (URL url : commonURLs) {
                String urlForm = url.toExternalForm();
                urlForm = urlForm.replace('\\', '/');
                sb.append(urlForm.substring(urlForm.lastIndexOf('/') + 1)).append(',');
            }
            System.setProperty("com.sun.enterprise.taglisteners", sb.toString());

            WebappConfiguration webappConfiguration = (WebappConfiguration) applicationContext.getBean("webAppConfiguration");

            ReadableArchive archive = getArchiveFactory(glassfishHolder.getServer()).openArchive(new File(webappConfiguration.getWar()));
            Properties params = new Properties();
            params.setProperty(ParameterNames.CONTEXT_ROOT, webappConfiguration.getContextPath());
            Application application = glassfishHolder.getServer().deploy(archive, params);

            return new GlassfishProcessingUnitContainer(applicationContext, application, glassfishHolder, webappConfiguration);
        } catch (Exception e) {
            try {
                glassfishHolder.stop();
            } catch (Exception e1) {
                logger.debug("Failed to stop glasfish after an error occured, ignoring", e);
            }
            throw new CannotCreateContainerException("Failed to start web application", e);
        } finally {
            setCurrentApplicationContext(null);
            setCurrentBeanLevelProperties(null);
            setCurrentClusterInfo(null);
            CommonClassLoader.getInstance().setDisableSmartGetUrl(false);
        }
    }

    private static ArchiveFactory getArchiveFactory(Server server) throws Exception {
        return getHabitat(server).getComponent(ArchiveFactory.class);
    }

    private static GrizzlyService getGrizzlyService(Server server) throws Exception {
        return getHabitat(server).getComponent(GrizzlyService.class);
    }

    private static Habitat getHabitat(Server server) throws Exception {
        Field field = server.getClass().getDeclaredField("habitat");
        field.setAccessible(true);
        return (Habitat) field.get(server);
    }
}
