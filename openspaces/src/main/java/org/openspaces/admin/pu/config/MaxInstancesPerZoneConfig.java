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
package org.openspaces.admin.pu.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name="max-instances-per-zone")
public class MaxInstancesPerZoneConfig {

    private String zone;
    private int maxNumberOfInstances;
    
    public String getZone() {
        return zone;
    }
    
    public void setZone(String zone) {
        this.zone = zone;
    }
    

    public int getMaxNumberOfInstances() {
        return maxNumberOfInstances;
    }
    
    @XmlAttribute(name ="max-instances")    
    public void setMaxNumberOfInstances(int maxNumberOfInstances) {
        this.maxNumberOfInstances = maxNumberOfInstances;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maxNumberOfInstances;
        result = prime * result + ((zone == null) ? 0 : zone.hashCode());
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
        MaxInstancesPerZoneConfig other = (MaxInstancesPerZoneConfig) obj;
        if (maxNumberOfInstances != other.maxNumberOfInstances)
            return false;
        if (zone == null) {
            if (other.zone != null)
                return false;
        } else if (!zone.equals(other.zone))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MaxInstancesPerZoneConfig [" + (zone != null ? "zone=" + zone + ", " : "") + "maxNumberOfInstances="
                + maxNumberOfInstances + "]";
    }
    
    
}
