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
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.space.SpaceInstanceConnectionDetails;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.SpaceInstanceTransactionDetails;

import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import com.j_spaces.kernel.time.SystemTime;

public class DefaultSpaceInstanceRuntimeDetails implements SpaceInstanceRuntimeDetails {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);
    private final DefaultSpaceInstance defaultSpaceInstance;
    private final DefaultSpaceInstanceTransactionDetails spaceInstanceTransactionDetails;
    private final DefaultSpaceInstanceConnectionDetails spaceInstanceConnectionDetails;
    
    private final long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private long lastStatisticsTimestamp = 0;
    
    private final Object monitor = new Object();

    private volatile SpaceRuntimeInfo lastSpaceRuntimeInfo;
    

    public DefaultSpaceInstanceRuntimeDetails(DefaultSpaceInstance defaultSpaceInstance) {
        this.defaultSpaceInstance = defaultSpaceInstance;
        this.spaceInstanceTransactionDetails = new DefaultSpaceInstanceTransactionDetails(defaultSpaceInstance);
        this.spaceInstanceConnectionDetails = new DefaultSpaceInstanceConnectionDetails(defaultSpaceInstance);
    }

    @Override
    public int getCount() {
        int count = 0;
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            SpaceRuntimeInfo spaceRuntimeInfo = getCachedSpaceRuntimeInfo( spaceAdmin );
            if( spaceRuntimeInfo != null ){
                for( Integer num : spaceRuntimeInfo.m_NumOFEntries ) {
                    count += num.intValue();
                }
            }
        }
        return count;
    }

    @Override
    public String[] getClassNames() {
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if(spaceAdmin != null) {
            SpaceRuntimeInfo spaceRuntimeInfo = getCachedSpaceRuntimeInfo( spaceAdmin );
            if( spaceRuntimeInfo != null ){
                ArrayList<String> classNames = 
                    new ArrayList<String>( spaceRuntimeInfo.m_ClassNames );
                Collections.sort(classNames);
                return classNames.toArray(new String[classNames.size()]);
            }
        }

        return new String[0];
    }

    @Override
    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            SpaceRuntimeInfo spaceRuntimeInfo = getCachedSpaceRuntimeInfo( spaceAdmin );
            if( spaceRuntimeInfo != null ){
                List<String> classNames = spaceRuntimeInfo.m_ClassNames;
                List<Integer> numOfEntries = spaceRuntimeInfo.m_NumOFEntries;
                for (int i=0; i<classNames.size(); ++i) {
                    mapping.put(classNames.get(i), numOfEntries.get(i));
                }
            }
        }
        
        return mapping;
    }

    @Override
    public Map<String, Integer> getNotifyTemplateCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if( spaceAdmin != null ) {
            SpaceRuntimeInfo spaceRuntimeInfo = getCachedSpaceRuntimeInfo( spaceAdmin );
            if( spaceRuntimeInfo != null ){
                List<String> classNames = spaceRuntimeInfo.m_ClassNames;
                List<Integer> numOfTemplates = spaceRuntimeInfo.m_NumOFTemplates;
                for( int i=0; i<classNames.size(); ++i ) {
                    mapping.put( classNames.get(i), numOfTemplates.get(i) );
                }
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
    
    private SpaceRuntimeInfo getCachedSpaceRuntimeInfo( IInternalRemoteJSpaceAdmin spaceAdmin ){
 
        synchronized( monitor ){

            long currentTime = SystemTime.timeMillis();
            if( ( currentTime - lastStatisticsTimestamp ) < statisticsInterval ) {
                return lastSpaceRuntimeInfo;
            }

            lastStatisticsTimestamp = currentTime;

            SpaceRuntimeInfo spaceRuntimeInfo;
            try {
                spaceRuntimeInfo = spaceAdmin.getRuntimeInfo();
                this.lastSpaceRuntimeInfo = spaceRuntimeInfo;
            } 
            catch( RemoteException e ) {

                logger.debug(
                        "RemoteException caught while trying to retrieve Space Runtime Info " +
                        "from space admin.", e );
            }
            
            return this.lastSpaceRuntimeInfo;
        }
    }
}