package org.openspaces.grid.gsm.machines;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

public class MachinesSlaEnforcement 
    implements  ServiceLevelAgreementEnforcement<MachinesSlaPolicy, String, MachinesSlaEnforcementEndpoint>{

    private final Map<String, MachinesSlaEnforcementEndpoint> endpointPerZone;
    
    private final Admin admin;
    private final NonBlockingElasticScaleHandler elasticScaleHandler;
    
    public MachinesSlaEnforcement(Admin admin, NonBlockingElasticScaleHandler elasticScaleHandler) {
        endpointPerZone = new HashMap<String, MachinesSlaEnforcementEndpoint>();
        this.admin = admin;
        this.elasticScaleHandler = elasticScaleHandler;
    }
    
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

    public void destroy() throws Exception {
        for (String zone : endpointPerZone.keySet()) {
            destroyEndpoint(zone);
        }
    }
}
