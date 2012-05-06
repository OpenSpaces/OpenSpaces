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
package org.openspaces.admin.memcached.config;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;
import org.openspaces.pu.container.servicegrid.deploy.MemcachedDeploy;
import org.springframework.beans.factory.annotation.Required;

import com.gigaspaces.security.directory.UserDetails;

/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name="memcached")
public class MemcachedConfig implements ProcessingUnitConfigFactory {

    private final ProcessingUnitConfig config;
    private String spaceUrl;
    

    public MemcachedConfig() {
        config = new ProcessingUnitConfig();
    }

    @Required
    public void setSpaceUrl(String spaceUrl) {
        this.spaceUrl = spaceUrl;
        config.setName(MemcachedDeploy.extractName(spaceUrl) + "-memcached");
        config.setContextProperty("url", spaceUrl);
    }

    public String getSpaceUrl() {
        return spaceUrl;
    }
    
    public String getClusterSchema() {
        return config.getClusterSchema();
    }

    /**
     * @see ProcessingUnitDeployment#clusterSchema(String)
     */
    public void setClusterSchema(String clusterSchema) {
        config.setClusterSchema(clusterSchema);
    }

    public Integer getNumberOfInstances() {
        return config.getNumberOfInstances();
    }

    /**
     * @see ProcessingUnitDeployment#numberOfInstances(int)
     */
    public void setNumberOfInstances(Integer numberOfInstances) {
        config.setNumberOfInstances(numberOfInstances);
    }

    public String getSlaLocation() {
        return config.getSlaLocation();
    }

    /**
     * @see ProcessingUnitDeployment#slaLocation(String)
     */
    public void setSlaLocation(String slaLocation) {
        config.setSlaLocation(slaLocation);
    }
    
    public Integer getNumberOfBackups() {
        return config.getNumberOfBackups();
    }

    /**
     * @see ProcessingUnitDeployment#numberOfBackups(int)
     */
    public void setNumberOfBackups(Integer numberOfBackups) {
        config.setNumberOfBackups(numberOfBackups);
    }

    public Integer getMaxInstancesPerVM() {
        return config.getMaxInstancesPerVM();
    }

    /**
     * @see ProcessingUnitDeployment#maxInstancesPerVM(int)
     */
    @XmlAttribute(name="max-instances-per-vm")
    public void setMaxInstancesPerVM(Integer maxInstancesPerVM) {
        config.setMaxInstancesPerVM(maxInstancesPerVM);
    }

    public Integer getMaxInstancesPerMachine() {
        return config.getMaxInstancesPerMachine();
    }

    /**
     * @see ProcessingUnitDeployment#maxInstancesPerMachine(int)
     */
    public void setMaxInstancesPerMachine(Integer maxInstancesPerMachine) {
        config.setMaxInstancesPerMachine(maxInstancesPerMachine);
    }

    /**
     * @see ProcessingUnitDeployment#setContextProperty(String, String)
     */
    public void setContextProperties(Map<String,String> contextProperties) {
        config.setContextProperties(contextProperties);
    }
    
    /**
     * @see ProcessingUnitDeployment#setContextProperty(String, String)
     */
    @XmlTransient
    public void setContextProperty(String key, String value) {
        config.setContextProperty(key, value);
    }
    
    public Map<String,String> getContextProperties() {
        return config.getContextProperties();
    }
    
    public List<String> getZones() {
        return config.getZones();
    }
    
    /**
     * @see ProcessingUnitDeployment#addZone(String)
     */
    public void setZones(List<String> zones) {
        config.setZones(zones);
    }

    /**
     * @see ProcessingUnitDeployment#addZone(String)
     */
    public void addZone(String zone) {
        config.addZone(zone);
    }

    public Map<String, Integer> getMaxInstancesPerZone() {
        return config.getMaxInstancesPerZone();
    }
    
    /**
     * @see ProcessingUnitDeployment#maxInstancesPerZone(String, int)
     */
    public void setMaxInstancesPerZone(Map<String, Integer> maxInstancesPerZone) {
        config.setMaxInstancesPerZone(maxInstancesPerZone);
    }
    
    /**
     * @see ProcessingUnitDeployment#maxInstancesPerZone(String, int)
     */
    public void setMaxInstancesPerZone(String zone, int maxInstancesPerZone) {
        config.setMaxInstancesPerZone(zone, maxInstancesPerZone);
    }
    
    /**
     * @see ProcessingUnitDeployment#secured(boolean)
     */
    public Boolean getSecured() {
        return config.getSecured();
    }

    public void setSecured(Boolean secured) {
        config.setSecured(secured);
    }

    public UserDetails getUserDetails() {
        return config.getUserDetails();
    }

    /**
     * @see ProcessingUnitDeployment#userDetails(UserDetails)
     */
    public void setUserDetails(UserDetails userDetails) {
        config.setUserDetails(userDetails);
    }

    /**
     * @see ProcessingUnitConfig#setDeploymentDependencies(List)
     */
    public void setDeploymentDependencies(List<ProcessingUnitDependency> dependencies) {
        config.setDeploymentDependencies(dependencies);
    }
    
    public List<ProcessingUnitDependency> getDeploymentDependencies() {
        return config.getDeploymentDependencies();
    }
    
    /**
     * @see ProcessingUnitDeployment#addDependencies(org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies)
     */
    public InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> getDependencies() {
        return config.getDependencies();
    }

    @XmlTransient
    public void setDependencies(InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies) {
        config.setDependencies(dependencies);
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((config == null) ? 0 : config.hashCode());
        result = prime * result + ((spaceUrl == null) ? 0 : spaceUrl.hashCode());
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
        MemcachedConfig other = (MemcachedConfig) obj;
        if (config == null) {
            if (other.config != null)
                return false;
        } else if (!config.equals(other.config))
            return false;
        if (spaceUrl == null) {
            if (other.spaceUrl != null)
                return false;
        } else if (!spaceUrl.equals(other.spaceUrl))
            return false;
        return true;
    }

    @Override
    public ProcessingUnitConfig toProcessingUnitConfig(Admin admin) {
        return config;
    }
}
