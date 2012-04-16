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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.space.SpaceInstanceConnectionDetails;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.SpaceInstanceTransactionDetails;

import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;

public class DefaultSpaceInstanceRuntimeDetails implements SpaceInstanceRuntimeDetails {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);
    private final DefaultSpaceInstance defaultSpaceInstance;
    private final DefaultSpaceInstanceTransactionDetails spaceInstanceTransactionDetails;
    private final DefaultSpaceInstanceConnectionDetails spaceInstanceConnectionDetails;

    public DefaultSpaceInstanceRuntimeDetails(DefaultSpaceInstance defaultSpaceInstance) {
        this.defaultSpaceInstance = defaultSpaceInstance;
        this.spaceInstanceTransactionDetails = new DefaultSpaceInstanceTransactionDetails(defaultSpaceInstance);
        this.spaceInstanceConnectionDetails = new DefaultSpaceInstanceConnectionDetails(defaultSpaceInstance);
    }

    public int getCount() {
        int count = 0;
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            try {
                for (Integer num : spaceAdmin.getRuntimeInfo().m_NumOFEntries) {
                    count += num.intValue();
                }
            } catch (RemoteException e) {
                logger.debug("RemoteException caught while trying to get Space count information from "
                        + defaultSpaceInstance.getSpaceName(), e);
            }
        }
        return count;
    }

    public String[] getClassNames() {
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            try {
                ArrayList<String> classNames = new ArrayList<String>(spaceAdmin.getRuntimeInfo().m_ClassNames);
                Collections.sort(classNames);
                return classNames.toArray(new String[classNames.size()]);
            } catch (RemoteException e) {
                logger.debug("RemoteException caught while trying to get Space class names information from "
                        + defaultSpaceInstance.getSpaceName(), e);
                return new String[0];
            }
        }else {
            return new String[0];
        }
    }

    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            try {
                List<String> classNames = spaceAdmin.getRuntimeInfo().m_ClassNames;
                List<Integer> numOfEntries = spaceAdmin.getRuntimeInfo().m_NumOFEntries;
                for (int i=0; i<classNames.size(); ++i) {
                    mapping.put(classNames.get(i), numOfEntries.get(i));
                }
            } catch (RemoteException e) {
                logger.debug("RemoteException caught while trying to get Space count per class name information from "
                        + defaultSpaceInstance.getSpaceName(), e);
                return mapping;
            }
        }
        return mapping;
    }

    public Map<String, Integer> getNotifyTemplateCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            try {
                List<String> classNames = spaceAdmin.getRuntimeInfo().m_ClassNames;
                List<Integer> numOfTemplates = spaceAdmin.getRuntimeInfo().m_NumOFTemplates;
                for (int i=0; i<classNames.size(); ++i) {
                    mapping.put(classNames.get(i), numOfTemplates.get(i));
                }
            } catch (RemoteException e) {
                logger.debug("RemoteException caught while trying to get Space template count per class name information from "
                        + defaultSpaceInstance.getSpaceName(), e);
                return mapping;
            }
        }
        return mapping;
    }
    
    @Override
    public SpaceInstanceTransactionDetails getTransactionDetails() {
        return spaceInstanceTransactionDetails;
    }
    @Override
    public SpaceInstanceConnectionDetails getConnectionDetails() {
        return spaceInstanceConnectionDetails;
    }
}
