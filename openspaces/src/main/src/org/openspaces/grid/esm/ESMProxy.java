package org.openspaces.grid.esm;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import net.jini.id.Uuid;

import org.jini.rio.resources.servicecore.AbstractProxy;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.grid.gsm.strategy.ElasticScaleStrategyEvents;

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
    public ElasticScaleStrategyEvents getScaleStrategyEvents(long cursor, int maxNumberOfEvents) throws RemoteException {
        return esmServer.getScaleStrategyEvents(cursor, maxNumberOfEvents);
    }

}