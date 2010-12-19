package org.openspaces.grid.gsm.machines;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

public class MachinesSlaEnforcement 
    implements  ServiceLevelAgreementEnforcement<MachinesSlaPolicy, ProcessingUnit, MachinesSlaEnforcementEndpoint>{

    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;
    
    private final Admin admin;
    
    public MachinesSlaEnforcement(Admin admin) {

        endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        this.admin = admin;
    }
    
    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        if (endpoints.containsKey(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }
    
    	MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(admin, pu);
    	endpoints.put(pu, endpoint);
    	return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        DefaultMachinesSlaEnforcementEndpoint endpoint = 
            (DefaultMachinesSlaEnforcementEndpoint) endpoints.remove(pu);
        endpoint.destroy();
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
}
