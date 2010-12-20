package org.openspaces.grid.gsm.machines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
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
    
    //TODO: check existing running GSAs for other PUs or other GSCs (different zone)
    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        if (endpoints.containsKey(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }
    
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        for (GridServiceAgent agent : admin.getGridServiceAgents()) {
            agents.add(agent);
        }
        
        for (MachinesSlaEnforcementEndpoint endpoint : endpoints.values()) {
            for (GridServiceAgent gsa : endpoint.getGridServiceAgents()) {
                agents.remove(gsa);
            }
            for (GridServiceAgent gsa : endpoint.getGridServiceAgentsPendingShutdown()) {
                agents.remove(gsa);
            }
        }
        
    	MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(admin, pu, agents);
    	endpoints.put(pu, endpoint);
    	return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        DefaultMachinesSlaEnforcementEndpoint endpoint = 
            (DefaultMachinesSlaEnforcementEndpoint) endpoints.remove(pu);
        if (endpoint != null) {
            endpoint.destroy();
        }
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
}
