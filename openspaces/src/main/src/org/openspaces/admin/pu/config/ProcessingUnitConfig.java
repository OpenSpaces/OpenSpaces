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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;
import org.openspaces.pu.container.support.CommandLineParser.Parameter;

import com.gigaspaces.grid.zone.ZoneHelper;
import com.gigaspaces.security.directory.UserDetails;

/**
 * @author itaif
 * @since 9.0.1
 */
public class ProcessingUnitConfig implements ProcessingUnitConfigFactory{

    private String processingUnit;

    private String name;

    private String clusterSchema;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    private Integer maxInstancesPerVM;

    private Integer maxInstancesPerMachine;

    private Map<String, Integer> maxInstancesPerZone = new HashMap<String, Integer>();

    private List<String> zones = new ArrayList<String>();

    private Map<String,String> contextProperties = new HashMap<String,String>();

    private UserDetails userDetails;

    private String slaLocation;

    private Boolean secured;

    private Map<String,String> elasticProperties = new HashMap<String,String>();

    private InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies = new DefaultProcessingUnitDependencies();

    public String getProcessingUnit() {
        return processingUnit;
    }

    public void setProcessingUnit(String processingUnit) {
        this.processingUnit = processingUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterSchema() {
        return clusterSchema;
    }

    public void setClusterSchema(String clusterSchema) {
        this.clusterSchema = clusterSchema;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public Integer getNumberOfBackups() {
        return numberOfBackups;
    }

    public void setNumberOfBackups(Integer numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    public Integer getMaxInstancesPerVM() {
        return maxInstancesPerVM;
    }

    public void setMaxInstancesPerVM(Integer maxInstancesPerVM) {
        this.maxInstancesPerVM = maxInstancesPerVM;
    }

    public Integer getMaxInstancesPerMachine() {
        return maxInstancesPerMachine;
    }

    public void setMaxInstancesPerMachine(Integer maxInstancesPerMachine) {
        this.maxInstancesPerMachine = maxInstancesPerMachine;
    }

    public Map<String, Integer> getMaxInstancesPerZone() {
        return maxInstancesPerZone;
    }

    public List<String> getZones() {
        return zones;
    }

    public Map<String,String> getContextProperties() {
        return contextProperties;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getSlaLocation() {
        return slaLocation;
    }

    public void setSlaLocation(String slaLocation) {
        this.slaLocation = slaLocation;
    }

    public Boolean getSecured() {
        return secured;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public Map<String,String> getElasticProperties() {
        return elasticProperties;
    }

    public InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * @see ProcessingUnitDeployment#maxInstancesPerZone(String, int)
     */
    public void setMaxInstancesPerZone(Map<String, Integer> maxInstancesPerZone) {
        this.maxInstancesPerZone = maxInstancesPerZone;
    }

    /**
     * @see ProcessingUnitDeployment#setContextProperty(String, String)
     */
    public void setContextProperties(Map<String,String> contextProperties) {
        this.contextProperties = contextProperties;
    }
    
    public void setContextProperty(String key, String value) {
        contextProperties.put(key, value);
    }

    /**
     * @see ProcessingUnitDeployment#setElasticProperty(String, String)
     */
    public void setElasticProperties(Map<String,String> elasticProperties) {
        this.elasticProperties = elasticProperties;
    }

    /**
     * @see ProcessingUnitDeployment#getDeploymentOptions()
     */
    public String[] toDeploymentOptions() {
        List<String> deployOptions = new ArrayList<String>();

        if (name != null) {
            deployOptions.add("-override-name");
            deployOptions.add(name);
        }
        if (slaLocation != null) {
            deployOptions.add("-sla");
            deployOptions.add(slaLocation);
        }
        if (clusterSchema != null || numberOfInstances != null || numberOfBackups != null) {
            deployOptions.add("-cluster");
            if (clusterSchema != null) {
                deployOptions.add("schema=" + clusterSchema);
            }
            if (numberOfInstances != null) {
                String totalMembers = "total_members=" + numberOfInstances;
                if (numberOfBackups != null) {
                    totalMembers += "," + numberOfBackups;
                }
                deployOptions.add(totalMembers);
            }
        }
        if (maxInstancesPerVM != null) {
            deployOptions.add("-max-instances-per-vm");
            deployOptions.add(maxInstancesPerVM.toString());
        }
        if (maxInstancesPerMachine != null) {
            deployOptions.add("-max-instances-per-machine");
            deployOptions.add(maxInstancesPerMachine.toString());
        }
        if (!maxInstancesPerZone.isEmpty()) {
            deployOptions.add("-max-instances-per-zone");
            deployOptions.add(ZoneHelper.unparse(maxInstancesPerZone));
        }
        if (!zones.isEmpty()) {
            deployOptions.add("-zones");
            for (String requiredZone : zones) {
                deployOptions.add(requiredZone);
            }
        }
        if (!elasticProperties.isEmpty()){
            deployOptions.add("-elastic-properties");
            for (Map.Entry<String, String> elasticProp : elasticProperties.entrySet()){
                deployOptions.add(elasticProp.getKey() + "=" + elasticProp.getValue());
            }
        }
        
        for (Map.Entry<String,String> entry : contextProperties.entrySet()) {
            deployOptions.add("-properties");
            deployOptions.add("embed://" + entry.getKey() + "=" + entry.getValue());
        }

        for (Parameter parameter : dependencies.toCommandLineParameters()) {
            deployOptions.add("-"+parameter.getName());
            for (String arg : parameter.getArguments()) {
                deployOptions.add(arg);
            }
        }
        
        deployOptions.add(getProcessingUnit());

        return deployOptions.toArray(new String[deployOptions.size()]);

    }

    public void setZones(List<String> zones) {
        this.zones = zones;
    }

    @Override
    public ProcessingUnitConfig toProcessingUnitConfig(Admin admin) {
        return this;
    }
}
