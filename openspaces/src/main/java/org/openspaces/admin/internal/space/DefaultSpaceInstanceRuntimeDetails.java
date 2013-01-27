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

import org.openspaces.admin.space.SpaceInstanceConnectionDetails;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.SpaceInstanceTransactionDetails;

import com.gigaspaces.management.space.LocalCacheDetails;
import com.gigaspaces.management.space.LocalViewDetails;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;

public class DefaultSpaceInstanceRuntimeDetails implements SpaceInstanceRuntimeDetails {

    private final SpaceRuntimeInfoCache spaceRuntimeInfoCache;
    private final LocalCacheDetailsCache localCacheDetailsCache;
    private final LocalViewDetailsCache localViewDetailsCache;
    
    private final DefaultSpaceInstanceTransactionDetails spaceInstanceTransactionDetails;
    private final DefaultSpaceInstanceConnectionDetails spaceInstanceConnectionDetails;
    
    public DefaultSpaceInstanceRuntimeDetails(DefaultSpaceInstance defaultSpaceInstance) {
        this.spaceRuntimeInfoCache = new SpaceRuntimeInfoCache(defaultSpaceInstance);
        this.localCacheDetailsCache = new LocalCacheDetailsCache(defaultSpaceInstance);
        this.localViewDetailsCache = new LocalViewDetailsCache(defaultSpaceInstance);
        this.spaceInstanceTransactionDetails = new DefaultSpaceInstanceTransactionDetails(defaultSpaceInstance);
        this.spaceInstanceConnectionDetails = new DefaultSpaceInstanceConnectionDetails(defaultSpaceInstance);
    }

    @Override
    public int getCount() {
        int count = 0;
        SpaceRuntimeInfo spaceRuntimeInfo = spaceRuntimeInfoCache.get();
        if (spaceRuntimeInfo != null) {
            for( Integer num : spaceRuntimeInfo.m_NumOFEntries ) {
                count += num.intValue();
            }
        }
        return count;
    }

    @Override
    public String[] getClassNames() {
        SpaceRuntimeInfo spaceRuntimeInfo = spaceRuntimeInfoCache.get();
        if (spaceRuntimeInfo != null) {
            ArrayList<String> classNames =  new ArrayList<String>(spaceRuntimeInfo.m_ClassNames);
            Collections.sort(classNames);
            return classNames.toArray(new String[classNames.size()]);
        }

        return new String[0];
    }

    @Override
    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        SpaceRuntimeInfo spaceRuntimeInfo = spaceRuntimeInfoCache.get();
        if (spaceRuntimeInfo != null) {
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
        SpaceRuntimeInfo spaceRuntimeInfo = spaceRuntimeInfoCache.get();
        if (spaceRuntimeInfo != null) {
            List<String> classNames = spaceRuntimeInfo.m_ClassNames;
            List<Integer> numOfTemplates = spaceRuntimeInfo.m_NumOFTemplates;
            for( int i=0; i<classNames.size(); ++i ) {
                mapping.put( classNames.get(i), numOfTemplates.get(i) );
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
    

	@Override
	public Map<String, LocalCacheDetails> getLocalCacheDetails() {
		return localCacheDetailsCache.get();
	}

	@Override
	public Map<String, LocalViewDetails> getLocalViewDetails() {
		return localViewDetailsCache.get();
	}

    private static class SpaceRuntimeInfoCache extends RemoteOperationTimeBasedCache<SpaceRuntimeInfo> {

    	private final DefaultSpaceInstance spaceInstance;
    	
    	public SpaceRuntimeInfoCache(DefaultSpaceInstance spaceInstance) {
    		this.spaceInstance = spaceInstance;
    	}
    	
		@Override
		protected SpaceRuntimeInfo load() throws RemoteException {
	        IInternalRemoteJSpaceAdmin spaceAdmin = spaceInstance.getSpaceAdmin();
	        return spaceAdmin != null ? spaceAdmin.getRuntimeInfo() : null;
		}   	
    }
    
    private static class LocalCacheDetailsCache extends RemoteOperationTimeBasedCache<Map<String, LocalCacheDetails>> {

    	private final DefaultSpaceInstance spaceInstance;
    	
    	public LocalCacheDetailsCache(DefaultSpaceInstance spaceInstance) {
    		this.spaceInstance = spaceInstance;
    	}
    	
		@Override
		protected Map<String, LocalCacheDetails> load() throws RemoteException {
	        IInternalRemoteJSpaceAdmin spaceAdmin = spaceInstance.getSpaceAdmin();
	        return spaceAdmin != null ? spaceAdmin.getLocalCacheDetails() : null;
		}   	
    }
    
    private static class LocalViewDetailsCache extends RemoteOperationTimeBasedCache<Map<String, LocalViewDetails>> {

    	private final DefaultSpaceInstance spaceInstance;
    	
    	public LocalViewDetailsCache(DefaultSpaceInstance spaceInstance) {
    		this.spaceInstance = spaceInstance;
    	}
    	
		@Override
		protected Map<String, LocalViewDetails> load() throws RemoteException {
	        IInternalRemoteJSpaceAdmin spaceAdmin = spaceInstance.getSpaceAdmin();
	        return spaceAdmin != null ? spaceAdmin.getLocalViewDetails() : null;
		}   	
    }
}