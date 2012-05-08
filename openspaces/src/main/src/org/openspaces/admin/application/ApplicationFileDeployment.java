/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.application;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.core.util.MemoryUnit;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * A helper method that creates a {@link ApplicationConfig} by reading an xml file
 * @author itaif
 * @since 9.0.1
 */
public class ApplicationFileDeployment {

    private static final Log logger = LogFactory.getLog(ApplicationFileDeployment.class);
    private File applicationDirectoryOrZip;
    private String applicationFileName;
    
    private static final String DEFAULT_APPLICATION_XML_FILENAME = "application.xml";
    private static final int MAX_XML_FILE_SIZE = (int) MemoryUnit.toBytes(System.getProperty("org.openspaces.admin.application.max-xml-file-size","10m"));

    /**
     * Creates a new application deployment based on the specified file
     * 
     * @param applicationDirectory
     *            - the application directory or zip file containing the application xml file and 
     *            the processing unit jar files.
     *            
     *            All jars referenced from application.xml are relative to this directory. If
     *            the processingUnit defined in the xml file with a '/' it is relative to the
     *            gigaspaces installation directory, and not the application directory
     *            
     * Assumes the directory or zip file contains application.xml file.
     * 
     * @since 9.0.1
     */
    public ApplicationFileDeployment(final File applicationDirectory) {
        this(applicationDirectory, DEFAULT_APPLICATION_XML_FILENAME);
    }

    /**
     * Creates a new application deployment based on the specified file
     * 
     * @param applicationDirectory
     *            - the application directory or zip file containing the application xml file and 
     *            the processing unit jar files.
     *            
     *            All jars referenced from application.xml are relative to this directory. If
     *            the processingUnit defined in the xml file with a '/' it is relative to the
     *            gigaspaces installation directory, and not the application directory
     *            
     * @param applicationFilename
     *            The application xml file (absolute or relative to the application directory)
     */
    public ApplicationFileDeployment(final File applicationDirectoryOrZip, final String applicationFileName) {
        this.applicationDirectoryOrZip = applicationDirectoryOrZip;
        this.applicationFileName = applicationFileName;
    }

    public ApplicationConfig create() {

        return readApplication(applicationDirectoryOrZip, applicationFileName);
    }

    private static ApplicationConfig readApplication(final File directoryOrZip, String applicationFile) {
        
        if (!directoryOrZip.exists()) {
            throw new AdminException("Application " + directoryOrZip.getAbsolutePath() + " does not exist.");
        }
        if (applicationFile.contains("\\") || applicationFile.contains("/")) {
            //TODO: Add test case to cover this scenario as a relative path in folders and zip files
            //      and as an absolute path.
            throw new AdminException("applicationFile " + applicationFile + " cannot be a path");
        }
        //read xml file into context
        ApplicationConfig config = null;
        if (directoryOrZip.isDirectory()) {
            final String applicationFilePath = new File(directoryOrZip, applicationFile).getAbsolutePath();
            config = readConfigFromXmlFile(applicationFilePath);
        }
        else {
            config = readConfigFromZipFile(directoryOrZip, applicationFile);
        }
          
        if (config == null) {
            throw new AdminException("Cannot find an application bean in "+ applicationFile + " file.");
        }
        
        // store application directory to config object for later use (reading the pus inside it)
        if (config.getJarsDirectoryOrZip() == null) {
            config.setJarsDirectoryOrZip(directoryOrZip);
        }
        return config;
    }

    private static ApplicationConfig readConfigFromXmlFile(final String applicationFilePath) {
        ApplicationConfig config;
        final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(applicationFilePath);
        try {
            //CR: Catch runtime exceptions. convert to AdminException(s)
            context.refresh();
            config = context.getBean(ApplicationConfig.class);
        }
        finally {
            if (context.isActive()) {
                context.close();
            }
        }
        return config;
    }

    private static ApplicationConfig readConfigFromZipFile(final File directoryOrZip, String applicationFile) {
        byte[] buffer = unzipFileToMemory(applicationFile, directoryOrZip);
        Resource resource = new ByteArrayResource(buffer);
        return getSpringBeanFromResource(resource, ApplicationConfig.class);
    }

    private static <T> T getSpringBeanFromResource(Resource resource, Class<T> type) {
        final GenericApplicationContext context = new GenericApplicationContext();
        try {
            final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
            xmlReader.loadBeanDefinitions(resource);
            context.refresh();
            return context.getBean(type);
        }
        finally {
            if (context.isActive()) {
                context.close();
            }
        }
    }

    private static byte[] unzipFileToMemory(String applicationFile, File directoryOrZip) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(directoryOrZip);
            final ZipEntry zipEntry = zipFile.getEntry(applicationFile);
            final int length = (int) zipEntry.getSize();
            if (length > MAX_XML_FILE_SIZE) {
                throw new AdminException("Application xml file size cannot be bigger than " + MAX_XML_FILE_SIZE
                        + " bytes");
            }
            byte[] buffer = new byte[length];
            final InputStream in = zipFile.getInputStream(zipEntry);
            final DataInputStream din = new DataInputStream(in);
            try {
                din.readFully(buffer, 0, length);
                return buffer;
            } catch (final IOException e) {
                throw new AdminException("Failed to read application file input stream", e);
            }

        } catch (final IOException e) {
            throw new AdminException("Failed to read zip file " + directoryOrZip, e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException e) {
                    logger.debug("Failed to close zip file " + directoryOrZip, e);
                }
            }
        }
    }
}
