/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.application;

import java.io.File;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Describes an application deployment that consists of one or more processing unit deployments.
 * @since 8.0.6
 * @author itaif
 */
public class ApplicationDeployment {

    private final ApplicationConfig config;
    
    /**
     * Creates a new application deployment with the specified name
     */
    public ApplicationDeployment(String applicationName) {
        config = new ApplicationConfig();
        config.setName(applicationName);
    }
    
    /**
     * Creates a new application deployment based on a folder containing an application.xml file and the pu jars
     * @param application - the application folder containing application.xml file, or the application.xml file itself
     * All jars referenced from the application.xml are assumed to be relative to the folder containing the applicaiton.xml file.
     * 
     * @since 9.0.1
     */
    public ApplicationDeployment(final File application) {
        
        if (!application.exists()) {
            throw new AdminException("Cannot find " + application.getAbsolutePath());
        }
        
        File applicationXmlFile = application;
        if (application.isDirectory()) {
            //default xml filename
            applicationXmlFile = new File(application,"application.xml");
        }
        
        // read xml file
        final ApplicationContext context = new FileSystemXmlApplicationContext(applicationXmlFile.getAbsolutePath());
        config = context.getBean(org.openspaces.admin.application.config.ApplicationConfig.class);
        if (config == null) {
            throw new AdminException("Cannot find an application in " + applicationXmlFile.getAbsolutePath());
        }
        
        // inject application directory to config object
        if (config.getJarsDirectory() == null) {
            File applicationDir = applicationXmlFile.getParentFile();
            config.setJarsDirectory(applicationDir);
        }
    }
    
    public ApplicationDeployment(String applicationName, ProcessingUnitDeploymentTopology ... processingUnitDeployments) {
        this(applicationName);
        for (ProcessingUnitDeploymentTopology puDeployment : processingUnitDeployments) {
            addProcessingUnitDeployment(puDeployment);
        }
    }

    /**
     * Deprecated Method. Use {@link #addProcessingUnitDeployment(ProcessingUnitDeploymentTopology)} instead
     */
    @Deprecated
    public ApplicationDeployment deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment) {
        return addProcessingUnitDeployment(puDeployment);
    }

    /**
     * Adds a processing unit deployment to this application deployment.
     * All processing units are deployed in parallel (unless dependencies are defined)
     */
    public ApplicationDeployment addProcessingUnitDeployment(ProcessingUnitDeploymentTopology puDeployment) {
        config.addProcessingUnit(puDeployment.create());
        return this;
    }
    
    public ApplicationConfig create() {
        return config;
    }
}
