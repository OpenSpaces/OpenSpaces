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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceConnectionDetails;
import org.openspaces.admin.space.SpaceInstanceTransactionDetails;

import com.j_spaces.core.admin.SpaceRuntimeInfo;

public class DefaultSpaceInstanceRuntimeDetails implements InternalSpaceInstanceRuntimeDetails {

    private final SpaceRuntimeInfo spaceRuntimeInfo;
    private final SpaceInstanceConnectionDetails spaceInstanceConnectionDetails;
    private final SpaceInstanceTransactionDetails spaceInstanceTransactionDetails;
    private final SpaceInstance spaceInstance;

    /**
     * Constructs an "NA" SpaceInstanceRuntimeDetails. {@link #isNA()}
     * @param defaultSpaceInstance 
     */
    public DefaultSpaceInstanceRuntimeDetails(SpaceInstance spaceInstance) {
        this(spaceInstance, null);
    }
    
    /**
     * Constructs a SpaceInstanceRuntimeDetails with values extracted form SpaceRuntimeInfo.
     * @param spaceRuntimeInfo runtime content of the space  
     */
    public DefaultSpaceInstanceRuntimeDetails(SpaceInstance spaceInstance, SpaceRuntimeInfo spaceRuntimeInfo) {
        this.spaceInstance = spaceInstance;
        this.spaceRuntimeInfo = spaceRuntimeInfo;
        this.spaceInstanceConnectionDetails = new DefaultSpaceInstanceConnectionDetails(spaceRuntimeInfo);
        this.spaceInstanceTransactionDetails = new DefaultSpaceInstanceTransactionDetails(spaceRuntimeInfo);
    }
    
    @Override
    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }
    
    @Override
    public boolean isNA() {
        return spaceRuntimeInfo == null;
    }

    @Override
    public int getCount() {
        int count = 0;
        if( spaceRuntimeInfo != null ) {
            for( Integer num : spaceRuntimeInfo.m_NumOFEntries ) {
                count += num.intValue();
            }
        }
        return count;
    }

    @Override
    public String[] getClassNames() {
        if( spaceRuntimeInfo != null ) {
            ArrayList<String> classNames = new ArrayList<String>(spaceRuntimeInfo.m_ClassNames);
            Collections.sort(classNames);
            return classNames.toArray(new String[classNames.size()]);
        }

      return new String[0];
    }

    @Override
    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        if( spaceRuntimeInfo != null ){
            List<String> classNames = spaceRuntimeInfo.m_ClassNames;
            List<Integer> numOfEntries = spaceRuntimeInfo.m_NumOFEntries;
            for (int i=0; i<classNames.size(); ++i) {
                mapping.put(classNames.get(i), numOfEntries.get(i));
            }
        }
        
        return mapping;
    }

    @Override
    public Map<String, Integer> getNotifyTemplateCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        if( spaceRuntimeInfo != null ){
            List<String> classNames = spaceRuntimeInfo.m_ClassNames;
            List<Integer> numOfTemplates = spaceRuntimeInfo.m_NumOFTemplates;
            for( int i=0; i<classNames.size(); ++i ) {
                mapping.put( classNames.get(i), numOfTemplates.get(i) );
            }
        }
        return mapping;
    }
    
    @Override
    public SpaceInstanceConnectionDetails getConnectionDetails() {
        return spaceInstanceConnectionDetails;
    }
    
    @Override
    public SpaceInstanceTransactionDetails getTransactionDetails() {
        return spaceInstanceTransactionDetails;
    }
}