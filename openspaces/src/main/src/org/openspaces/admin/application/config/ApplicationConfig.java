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
package org.openspaces.admin.application.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name="application")
public class ApplicationConfig {

    private String name;
    private List<ProcessingUnitConfigFactory> processingUnits = new ArrayList<ProcessingUnitConfigFactory>();
    private File jarsDirectory;
    
    public String getName() {
        return name;
    }

    @Required
    public void setName(String name) {
        this.name = name;
    }
    
    public ProcessingUnitConfigFactory[] getProcessingUnits() {
        return processingUnits.toArray(new ProcessingUnitConfigFactory[processingUnits.size()]);
    }
    
    @XmlElement(type = ProcessingUnitConfigFactory.class)
    public void setProcessingUnits(ProcessingUnitConfigFactory[] processingUnitDeployments) {
        this.processingUnits = new ArrayList<ProcessingUnitConfigFactory>(Arrays.asList(processingUnitDeployments));
    }

    public void addProcessingUnit(ProcessingUnitConfigFactory puConfigFactory) {
        this.processingUnits.add(puConfigFactory);
    }

    public File getJarsDirectory() {
        return jarsDirectory;
    }
    
    @XmlTransient
    public void setJarsDirectory(File jarsDirectory) {
        this.jarsDirectory = jarsDirectory;
        
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jarsDirectory == null) ? 0 : jarsDirectory.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((processingUnits == null) ? 0 : processingUnits.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationConfig other = (ApplicationConfig) obj;
        if (jarsDirectory == null) {
            if (other.jarsDirectory != null)
                return false;
        } else if (!jarsDirectory.equals(other.jarsDirectory))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (processingUnits == null) {
            if (other.processingUnits != null)
                return false;
        } else if (!processingUnits.equals(other.processingUnits))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ApplicationConfig [" + (name != null ? "name=" + name + ", " : "")
                + (processingUnits != null ? "processingUnits=" + processingUnits + ", " : "")
                + (jarsDirectory != null ? "jarsDirectory=" + jarsDirectory : "") + "]";
    }
}