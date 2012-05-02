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

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.config.XmlProperty;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author itaif
 * @since 9.0.1
 */
public class ApplicationConfig {

    private String name;
    private List<ProcessingUnitConfigFactory> processingUnits = new ArrayList<ProcessingUnitConfigFactory>();
    
    public String getName() {
        return name;
    }

    @Required
    @XmlProperty
    public void setName(String name) {
        this.name = name;
    }
    
    public List<ProcessingUnitConfigFactory> getProcessingUnits() {
        return processingUnits;
    }
    
    @XmlProperty
    public void setProcessingUnits(List<ProcessingUnitConfigFactory> processingUnitDeployments) {
        this.processingUnits = processingUnitDeployments;
    }

    public void addProcessingUnit(ProcessingUnitConfigFactory puConfigFactory) {
        processingUnits.add(puConfigFactory);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((processingUnits == null) ? 0 : processingUnits.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationConfig other = (ApplicationConfig) obj;
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
}
