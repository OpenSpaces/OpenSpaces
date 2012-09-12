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
package org.openspaces.admin.internal.pu.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jini.rio.core.RequiredDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.container.support.CommandLineParser.Parameter;
import org.openspaces.pu.container.support.RequiredDependenciesCommandLineParser;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public abstract class AbstractProcessingUnitDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> {

    private final Map<String,RequiredDependencies> requiredDependeciesPerCommandLineOption;

    protected AbstractProcessingUnitDependencies() {
        requiredDependeciesPerCommandLineOption = new HashMap<String, RequiredDependencies>();
    }
    
    public void addDetailedDependenciesByCommandLineOption(String commandLineOption, RequiredDependencies requiredDependencies) {
        if (!requiredDependencies.isEmpty()) {
            RequiredDependencies existingRequiredDependencies = requiredDependeciesPerCommandLineOption.get(commandLineOption);
            if (existingRequiredDependencies != null) {
                //merge
                existingRequiredDependencies.addRequiredDependencies(requiredDependencies);
            }
            else {
                requiredDependeciesPerCommandLineOption.put(commandLineOption, requiredDependencies);
            }
        }
    }
    
    public void addDetailedDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
       InternalProcessingUnitDetailedDependencies internalDependencies = (InternalProcessingUnitDetailedDependencies)detailedDependencies;
       addDetailedDependenciesByCommandLineOption(internalDependencies.getCommandLineOption(), internalDependencies.toRequiredDependencies());
    }
        
    protected void addDetailedAllDependencies(ProcessingUnitDependencies<? extends ProcessingUnitDependency> dependencies) {
        Map<String,RequiredDependencies> map = ((AbstractProcessingUnitDependencies)dependencies).requiredDependeciesPerCommandLineOption;
        for (Map.Entry<String,RequiredDependencies> entry : map.entrySet()) {
            addDetailedDependenciesByCommandLineOption(entry.getKey(), entry.getValue());
        }
    }
    
    protected <Z extends InternalProcessingUnitDetailedDependencies<T,IT>> Z getDetailedDependencies(Z newDetailedDependencies) {
        RequiredDependencies requiredDependencies = requiredDependeciesPerCommandLineOption.get(newDetailedDependencies.getCommandLineOption());
        if (requiredDependencies != null) {
            newDetailedDependencies.addDependencies(requiredDependencies);
        }
        return newDetailedDependencies;
    }
    
    protected <Z extends InternalProcessingUnitDetailedDependencies<T,IT>> void setDetailedDependencies(Z newDetailedDependencies) {
        requiredDependeciesPerCommandLineOption.put(newDetailedDependencies.getCommandLineOption(), newDetailedDependencies.toRequiredDependencies());
    }
    
    public CommandLineParser.Parameter[] toCommandLineParameters() {
        List<CommandLineParser.Parameter> parameters = new ArrayList<CommandLineParser.Parameter>();
        for (Entry<String, RequiredDependencies> entry : requiredDependeciesPerCommandLineOption.entrySet()) {
            RequiredDependencies requiredDependencies = entry.getValue();
            if (!requiredDependencies.isEmpty()) {
                String commandLineOption = entry.getKey();
                Parameter parameter = RequiredDependenciesCommandLineParser.convertRequiredDependenciesToCommandLineParameter(commandLineOption, requiredDependencies);
                parameters.add(parameter);
            }
        }
        return parameters.toArray(new CommandLineParser.Parameter[parameters.size()]);
    }
    
    public String[] getDependenciesRequiredProcessingUnitNames() {
        List<String> requiredProcessingUnitNames = new ArrayList<String>(); 
        for (RequiredDependencies requiredDependencies : requiredDependeciesPerCommandLineOption.values()) {
            for (String requiredProcessingUnitName : requiredDependencies.getRequiredDependenciesNames()) {
                if (!requiredProcessingUnitNames.contains(requiredProcessingUnitName)) {
                    requiredProcessingUnitNames.add(requiredProcessingUnitName);
                }
            }
        }
        return requiredProcessingUnitNames.toArray(new String[requiredProcessingUnitNames.size()]);
    }

    @Override
    public String toString() {
        List<String> commandline = new ArrayList<String>();
        for (Entry<String, RequiredDependencies>  entry : requiredDependeciesPerCommandLineOption.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Parameter parameter = RequiredDependenciesCommandLineParser.convertRequiredDependenciesToCommandLineParameter(entry.getKey(),entry.getValue());
                commandline.add("-"+parameter.getName());
                commandline.addAll(Arrays.asList(parameter.getArguments()));
            }
        }
        return StringUtils.collectionToDelimitedString(commandline," ");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((requiredDependeciesPerCommandLineOption == null) ? 0 : requiredDependeciesPerCommandLineOption.hashCode());
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
        AbstractProcessingUnitDependencies other = (AbstractProcessingUnitDependencies) obj;
        if (requiredDependeciesPerCommandLineOption == null) {
            if (other.requiredDependeciesPerCommandLineOption != null)
                return false;
        } else if (!requiredDependeciesPerCommandLineOption.equals(other.requiredDependeciesPerCommandLineOption))
            return false;
        return true;
    }
    
    
}
