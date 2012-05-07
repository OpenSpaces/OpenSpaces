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

import java.io.File;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author itaif
 * @since 9.0.1
 */
public class ApplicationFileDeployment extends ApplicationDeployment {

    private static final String DEFAULT_APPLICATION_XML_FILENAME = "application.xml";

    /**
     * Creates a new application deployment based on the specified file
     * 
     * @param application
     *            - the application folder containing {@link #DEFAULT_APPLICATION_XML_FILENAME} file,
     * 
     *            or
     * 
     *            the application.xml file itself in which case all jars referenced from
     *            application.xml are relative to the folder containing the applicaiton.xml file. If
     *            the processingUnit defined in the xml file with a '/' it is relative to the
     *            gigaspaces installation folder, and not the applicaiton folder
     * 
     * @since 9.0.1
     */
    public ApplicationFileDeployment(final File application) {
        super(readApplication(application));
    }
    
    private static ApplicationConfig readApplication(final File application) {
        if (!application.exists()) {
            throw new AdminException("Cannot find " + application.getAbsolutePath());
        }
        
        File applicationXmlFile = application;
        if (application.isDirectory()) {
            //default xml filename
            applicationXmlFile = new File(application,DEFAULT_APPLICATION_XML_FILENAME);
        }
        
        // read xml file
        final ApplicationContext context = new FileSystemXmlApplicationContext(applicationXmlFile.getAbsolutePath());
        ApplicationConfig config = context.getBean(org.openspaces.admin.application.config.ApplicationConfig.class);
        if (config == null) {
            throw new AdminException("Cannot find an application in " + applicationXmlFile.getAbsolutePath());
        }
        
        // inject application directory to config object
        if (config.getJarsDirectory() == null) {
            final File applicationDir = applicationXmlFile.getParentFile();
            config.setJarsDirectory(applicationDir);
        }
        return config;
    }
}
