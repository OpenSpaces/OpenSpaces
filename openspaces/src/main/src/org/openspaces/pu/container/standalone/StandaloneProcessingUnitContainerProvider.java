package org.openspaces.pu.container.standalone;

import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.core.config.BeanLevelPropertiesAware;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * @author kimchy
 */
public class StandaloneProcessingUnitContainerProvider implements ProcessingUnitContainerProvider, BeanLevelPropertiesAware {

    private static Log logger = LogFactory.getLog(StandaloneProcessingUnitContainerProvider.class);

    private String location;

    private List configLocations = new ArrayList();

    private BeanLevelProperties beanLevelProperties;

    public StandaloneProcessingUnitContainerProvider(String location) {
        this.location = location;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public void addConfigLocation(String configLocation) {
        configLocations.add(configLocation);
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        File fileLocation = new File(location);
        if (!fileLocation.exists()) {
            throw new CannotCreateContainerException("Failed to locate pu location [" + location + "]");
        }
        List urls = new ArrayList();
        if (fileLocation.isDirectory()) {
            File classesLocation = new File(fileLocation, "classes");
            if (classesLocation.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding classes directory with pu directory location [" + location + "]");
                }
                try {
                    urls.add(classesLocation.toURL());
                } catch (MalformedURLException e) {
                    throw new CannotCreateContainerException("Failed to add classes to class loader with location [" + location + "]", e);
                }
            }
            File libLocation = new File(fileLocation, "lib");
            if (libLocation.exists()) {
                File[] jarFiles = libLocation.listFiles();
                for (int i = 0; i < jarFiles.length; i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding jar [" + jarFiles[i].getAbsolutePath() + "] with pu directory location [" + location + "]");
                    }
                    try {
                        urls.add(jarFiles[i].toURL());
                    } catch (MalformedURLException e) {
                        throw new CannotCreateContainerException("Failed to add jar file [" + jarFiles[i].getAbsolutePath() + "] to classs loader", e);
                    }
                }
            }
        } else {
            JarFile jarFile;
            try {
                jarFile = new JarFile(fileLocation);
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to open pu file [" + location + "]", e);
            }
            for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                if (jarEntry.getName().equals("classes/")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding classes directory with pu location [" + location + "]");
                    }
                    try {
                        urls.add(new URL("jar:" + fileLocation.toURL() + "!/" + jarEntry.getName()));
                    } catch (MalformedURLException e) {
                        throw new CannotCreateContainerException("Failed to add pu entry [" + jarEntry.getName() + "] with location [" + location + "]", e);
                    }
                } else if (jarEntry.getName().startsWith("lib/") && jarEntry.getName().length() > "lib/".length()) {
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
        URL[] classLoaderUrls = (URL[]) urls.toArray(new URL[urls.size()]);
        URLClassLoader classLoader = new URLClassLoader(classLoaderUrls, parentClassLoader);
        StandaloneContainerRunnable containerRunnable = new StandaloneContainerRunnable(beanLevelProperties, configLocations);
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

}
