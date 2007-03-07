package org.openspaces.pu.container.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>A {@link StandaloneProcessingUnitContainer} provider. A standalone processing unit container is a container
 * that understands a processing unit archive structure (both when working with an "exploded" directory and when
 * working with a zip/jar archive of it). It is provided with the location of the processing unit using
 * {@link org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainerProvider#StandaloneProcessingUnitContainerProvider(String)}.
 * The location itself follows Spring resource loader syntax.
 *
 * <p>When creating the container a thread is started with {@link org.openspaces.pu.container.standalone.StandaloneContainerRunnable}.
 * This is done since a custom class loader is created taking into account the processing unit archive structure, and
 * in order to allows using the standlone container within other environemnts, the new class loader is only set on
 * the newly created thread context.
 *
 * <p>At its core the integrated processing unit container is built around Spring
 * {@link org.springframework.context.ApplicationContext} configured based on a set of config locations.
 *
 * <p>The provider allows for programmatic configuration of different processing unit aspects. It allows to configure
 * where the processing unit Spring context xml descriptors are located (by default it uses
 * <code>classpath*:/META-INF/spring/*.xml</code>). It also allows to set {@link org.openspaces.core.config.BeanLevelProperties}
 * and {@link org.openspaces.core.cluster.ClusterInfo} that will be injected to beans configured within the processing
 * unit.
 *
 * <p>For a runnable "main" processing unit container please see
 * {@link StandaloneProcessingUnitContainer#main(String[])}.
 *
 * @author kimchy
 */
public class StandaloneProcessingUnitContainerProvider implements ApplicationContextProcessingUnitContainerProvider {

    private static Log logger = LogFactory.getLog(StandaloneProcessingUnitContainerProvider.class);

    private String location;

    private List<String> configLocations = new ArrayList<String>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    /**
     * Constructs a new standalone container provider using the provided location as the location of the processing
     * unit archive (either an exploded archive or a jar/zip archive). The location syntax follows Spring
     * {@link org.springframework.core.io.Resource} syntax.
     *
     * @param location The location of the processing unit archive
     */
    public StandaloneProcessingUnitContainerProvider(String location) {
        this.location = location;
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
     * Adds a config location based on a String description using Springs
     * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
     *
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void addConfigLocation(String configLocation) {
        configLocations.add(configLocation);
    }

    /**
     * <p>Creates a new {@link StandaloneProcessingUnitContainer} based on
     * the configured parameters. A standalone processing unit container is a container that understands a processing
     * unit archive structure (both when working with an "exploded" directory and when working with a zip/jar archive
     * of it). It is provided with the location of the processing unit using
     * {@link org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainerProvider#StandaloneProcessingUnitContainerProvider(String)}.
     * The location itself follows Spring resource loader syntax.
     *
     * <p>If {@link #addConfigLocation(String)} is used, the Spring xml context will be read based on the provided
     * locations. If no config location was provided the default config location will be
     * <code>classpath*:/META-INF/spring/*.xml</code>.
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
     * @return An {@link StandaloneProcessingUnitContainer} instance
     * @throws CannotCreateContainerException
     */
    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        File fileLocation = new File(location);
        if (!fileLocation.exists()) {
            throw new CannotCreateContainerException("Failed to locate pu location [" + location + "]");
        }
        List<URL> urls = new ArrayList<URL>();
        if (fileLocation.isDirectory()) {
            if (fileLocation.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding pu directory location [" + location + "] to classpath");
                }
                try {
                    urls.add(fileLocation.toURL());
                } catch (MalformedURLException e) {
                    throw new CannotCreateContainerException("Failed to add classes to class loader with location [" + location + "]", e);
                }
            }
            addJarsLocation(fileLocation, urls, "lib");
            addJarsLocation(fileLocation, urls, "shared-lib");
        } else {
            JarFile jarFile;
            try {
                jarFile = new JarFile(fileLocation);
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to open pu file [" + location + "]", e);
            }
            // add the root to the classpath
            try {
                urls.add(new URL("jar:" + fileLocation.toURL() + "!/"));
            } catch (MalformedURLException e) {
                throw new CannotCreateContainerException("Failed to add pu location [" + location + "] to classpath", e);
            }
            // add jars in lib and shared-lib to the classpath
            for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                if (isWithinDir(jarEntry, "lib") || isWithinDir(jarEntry, "shared-lib")) {
                    // extract the jar into a temp location
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding jar [" + jarEntry.getName() + "] with pu location [" + location + "]");
                    }
                    File tempLocation = new File(System.getProperty("java.io.tmpdir") + "/openspaces");
                    tempLocation.mkdirs();
                    File tempJar;
                    try {
                        tempJar = File.createTempFile("openspaces-", ".jar", tempLocation);
                    } catch (IOException e) {
                        throw new CannotCreateContainerException("Failed to create temp jar at location [" + tempLocation + "]");
                    }
                    tempJar.deleteOnExit();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Extracting jar [" + jarEntry.getName() + "] to temporary jar [" + tempJar.getAbsolutePath() + "]");
                    }

                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(tempJar);
                    } catch (FileNotFoundException e) {
                        throw new CannotCreateContainerException("Failed to find temp jar [" + tempJar.getAbsolutePath() + "]", e);
                    }
                    InputStream is = null;
                    try {
                        is = jarFile.getInputStream(jarEntry);
                        FileCopyUtils.copy(is, fos);
                    } catch (IOException e) {
                        throw new CannotCreateContainerException("Failed to create temp jar [" + tempJar.getAbsolutePath() + "]");
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e1) {
                                // do nothing
                            }
                        }
                        try {
                            fos.close();
                        } catch (IOException e1) {
                            // do nothing
                        }
                    }

                    try {
                        urls.add(tempJar.toURL());
                    } catch (MalformedURLException e) {
                        throw new CannotCreateContainerException("Failed to add pu entry [" + jarEntry.getName() + "] with location [" + location + "]", e);
                    }
                }
            }
        }

        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        if (parentClassLoader == null) {
            parentClassLoader = this.getClass().getClassLoader();
        }
        URL[] classLoaderUrls = urls.toArray(new URL[urls.size()]);
        // TODO need to probably implement our own class loader so we can control what gets propogated to the parent class loader
        URLClassLoader classLoader = new URLClassLoader(classLoaderUrls, parentClassLoader);
        StandaloneContainerRunnable containerRunnable = new StandaloneContainerRunnable(beanLevelProperties, clusterInfo, configLocations);
        Thread standaloneContainerThread = new Thread(containerRunnable, "Standalone Container Thread");
        standaloneContainerThread.setDaemon(false);
        standaloneContainerThread.setContextClassLoader(classLoader);
        standaloneContainerThread.start();

        // TODO implement proper shutdown if the runnable fails to start
        while (!containerRunnable.isInitialized()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for standalone container to initalize");
            }
        }

        return new StandaloneProcessingUnitContainer(containerRunnable);
    }

    private boolean isWithinDir(JarEntry jarEntry, String dir) {
        return jarEntry.getName().startsWith(dir + "/") && jarEntry.getName().length() > (dir + "/").length();
    }

    private void addJarsLocation(File fileLocation, List<URL> urls, String dir) {
        File libLocation = new File(fileLocation, dir);
        if (libLocation.exists()) {
            File[] jarFiles = libLocation.listFiles();
            for (File jarFile : jarFiles) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding jar [" + jarFile.getAbsolutePath() + "] with pu directory location [" + location + "] to classpath");
                }
                try {
                    urls.add(jarFile.toURL());
                } catch (MalformedURLException e) {
                    throw new CannotCreateContainerException("Failed to add jar file [" + jarFile.getAbsolutePath() + "] to classs loader", e);
                }
            }
        }
    }

}
