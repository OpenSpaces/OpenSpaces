package org.openspaces.grid.gsm.machines;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.core.bean.Bean;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

public class MachinesSlaEnforcment 
    implements  Bean ,NonBlockingElasticScaleHandlerAware , 
                ServiceLevelAgreementEnforcement<MachinesSlaPolicy, String, MachinesSlaEnforcementEndpoint>{

    private Map<String, MachinesSlaEnforcementEndpoint> endpointPerZone;
    
    private Admin admin;
    private Map<String, String> properties;
    private NonBlockingElasticScaleHandler elasticScaleHandler;
        
    public MachinesSlaEnforcementEndpoint createEndpoint(String zone) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        if (endpointPerZone.containsKey(zone)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }
    
    	MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(admin, zone, elasticScaleHandler);
    	endpointPerZone.put(zone, endpoint);
    	return endpoint;
    }

    public void destroyEndpoint(String id) {

        DefaultMachinesSlaEnforcementEndpoint endpoint = (DefaultMachinesSlaEnforcementEndpoint) endpointPerZone.get(id);
        endpoint.destroy();
        
        endpointPerZone.remove(id);
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void destroy() throws Exception {
        for (String zone : endpointPerZone.keySet()) {
            destroyEndpoint(zone);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

	public void setNonBlockingElasticScaleHandler(NonBlockingElasticScaleHandler elasticScaleHandler) {
		this.elasticScaleHandler = elasticScaleHandler;
	}

}
