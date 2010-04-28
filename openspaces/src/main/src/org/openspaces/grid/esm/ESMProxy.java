package org.openspaces.grid.esm;

import java.io.Serializable;
import java.rmi.RemoteException;

import net.jini.id.Uuid;

import org.jini.rio.resources.servicecore.AbstractProxy;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;

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

    public void deploy(ElasticDataGridDeployment deployment) {
        esmServer.deploy(deployment);
    }

    public String[] getManagedProcessingUnits() {
        return esmServer.getManagedProcessingUnits();
    }
}
