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
package org.openspaces.admin.internal.space;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.openspaces.admin.space.SpaceConnectionDetails;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.SpaceRuntimeDetails;
import org.openspaces.admin.space.SpaceTransactionDetails;

import com.gigaspaces.cluster.activeelection.SpaceMode;

public class DefaultSpaceRuntimeDetails implements SpaceRuntimeDetails {

    private final List<SpaceInstanceRuntimeDetails> spaceInstancesDetails;
    private final DefaultSpaceTransactionDetails spaceTransactionDetails;
    private final DefaultSpaceConnectionDetails spaceConnectionDetails;

    public DefaultSpaceRuntimeDetails(List<SpaceInstanceRuntimeDetails> details) {
        this.spaceInstancesDetails = details;
        this.spaceTransactionDetails = new DefaultSpaceTransactionDetails(details);
        this.spaceConnectionDetails = new DefaultSpaceConnectionDetails(details);
    }
    
    @Override
    public boolean isNA() {
        if (spaceInstancesDetails.size() == 0) {
            return true;
        }
        for (SpaceInstanceRuntimeDetails runtimeDetails : spaceInstancesDetails) {
            if (runtimeDetails.isNA()) {
                return true;
            }
        }
        return false;
    }
    
    public String[] getClassNames() {
        Set<String> classNames = new TreeSet<String>(); //keep same order
        for (SpaceInstanceRuntimeDetails runtimeDetails : spaceInstancesDetails) {
            if ( ((InternalSpaceInstanceRuntimeDetails)runtimeDetails).getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                for (String className : runtimeDetails.getClassNames()) {
                    classNames.add(className);
                }
            }
         }
        return classNames.toArray(new String[classNames.size()]);
    }

    public int getCount() {
        int count = 0;
        for (SpaceInstanceRuntimeDetails runtimeDetails : spaceInstancesDetails) {
            if ( ((InternalSpaceInstanceRuntimeDetails)runtimeDetails).getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                count += runtimeDetails.getCount();
            }
         }
        return count;
    }

    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (SpaceInstanceRuntimeDetails runtimeDetails : spaceInstancesDetails) {
            if ( ((InternalSpaceInstanceRuntimeDetails)runtimeDetails).getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                Map<String, Integer> countPerClassName = runtimeDetails.getCountPerClassName();
                for (Entry<String, Integer> entry : countPerClassName.entrySet()) {
                    Integer value = mapping.get(entry.getKey());
                    if (value == null) { 
                        //class name doesn't exist, add it with its count
                        mapping.put(entry.getKey(), entry.getValue());
                    } else {
                        //class name exists, sum both counts
                        int newValue = (value.intValue() + entry.getValue().intValue());
                        mapping.put(entry.getKey(), newValue);
                    }
                }
            }
        }
        return mapping;
    }

    public Map<String, Integer> getNotifyTemplateCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (SpaceInstanceRuntimeDetails runtimeDetails : spaceInstancesDetails) {
            if ( ((InternalSpaceInstanceRuntimeDetails)runtimeDetails).getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                Map<String, Integer> notifyTemplateCountPerClassName = runtimeDetails.getNotifyTemplateCountPerClassName();
                for (Entry<String, Integer> entry : notifyTemplateCountPerClassName.entrySet()) {
                    Integer value = mapping.get(entry.getKey());
                    if (value == null) { 
                        //class name doesn't exist, add it with its notify-template count
                        mapping.put(entry.getKey(), entry.getValue());
                    } else {
                        //class name exists, sum both notify-template counts
                        int newValue = (value.intValue() + entry.getValue().intValue());
                        mapping.put(entry.getKey(), newValue);
                    }
                }
            }
        }
        return mapping;
    }
    
    @Override
    public SpaceTransactionDetails getTransactionDetails() {
        return spaceTransactionDetails;
    }
    
    @Override
    public SpaceConnectionDetails getConnectionDetails() {
        return spaceConnectionDetails;
    }
}
