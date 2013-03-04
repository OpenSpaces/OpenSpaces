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
package org.openspaces.grid.esm;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import net.jini.id.Uuid;

import org.jini.rio.monitor.event.Events;
import org.jini.rio.resources.servicecore.AbstractProxy;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.security.service.SecurityContext;

public class ESMProxy extends AbstractProxy implements ESM, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ESM esmServer;
    
    static ESMProxy getInstance(ESM esm, Uuid id) {
        return (new ESMProxy(esm, id));
    }

    ESMProxy(ESM esm, Uuid id) {
        super(esm, id);
        this.esmServer = esm;
    }

    private transient Integer cachedAgentId = null;
	public int getAgentId() throws RemoteException {
		if (cachedAgentId == null) {
			cachedAgentId = Integer.valueOf(esmServer.getAgentId());
		}
		return cachedAgentId.intValue();
	}

	private transient String cachedGSAServiceID = null;
	public String getGSAServiceID() throws RemoteException {
		if (cachedGSAServiceID == null) {
			cachedGSAServiceID = esmServer.getGSAServiceID();
		}
		return cachedGSAServiceID;
	}

    public String[] getManagedProcessingUnits() {
        return esmServer.getManagedProcessingUnits();
    }

    public boolean isServiceSecured() throws RemoteException {
        return esmServer.isServiceSecured();
    }

    public SecurityContext login(UserDetails userDetails) throws SecurityException, RemoteException {
        return esmServer.login( userDetails );
    }

    public void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties)
            throws RemoteException {
        esmServer.setProcessingUnitElasticProperties(processingUnitName, properties);
    }

    public void setProcessingUnitScaleStrategy(String puName, ScaleStrategyConfig scaleStrategyConfig)
            throws RemoteException {
        esmServer.setProcessingUnitScaleStrategy(puName, scaleStrategyConfig);
    }

    public ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(String processingUnitName)throws RemoteException {
        return esmServer.getProcessingUnitScaleStrategyConfig(processingUnitName);
    }

    @Override
    public boolean isManagingProcessingUnit(String processingUnitName) throws RemoteException {
         return esmServer.isManagingProcessingUnit(processingUnitName);
    }

    @Override
    public boolean isManagingProcessingUnitAndScaleNotInProgress(String processingUnitName) throws RemoteException {
        return esmServer.isManagingProcessingUnitAndScaleNotInProgress(processingUnitName);
    }

    @Override
    public Events getScaleStrategyEvents(long cursor, int maxNumberOfEvents) throws RemoteException {
        return esmServer.getScaleStrategyEvents(cursor, maxNumberOfEvents);
    }

	@Override
	public Remote getStorageApi(String processingUnitName) throws RemoteException {
		return esmServer.getStorageApi(processingUnitName);
	}
}
