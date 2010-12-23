package org.openspaces.grid.gsm.machines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

public class MachinesSlaEnforcement implements
        ServiceLevelAgreementEnforcement<MachinesSlaPolicy, ProcessingUnit, MachinesSlaEnforcementEndpoint> {

    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;

    private final Admin admin;

    public MachinesSlaEnforcement(Admin admin) {

        endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        this.admin = admin;
    }

    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        if (endpoints.containsKey(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("PU has to have exactly 1 zone defined");
        }

        String zone = pu.getRequiredZones()[0];
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            
            // found a container associated with this pu. take it.
            if (container.getZones().size() == 1 &&
                container.getZones().containsKey(zone) &&
                container.getMachine().getGridServiceAgents().getSize() == 1) {

                agents.add(container.getMachine().getGridServiceAgent());
           }
        }

        for (MachinesSlaEnforcementEndpoint endpoint : endpoints.values()) {
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgents()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgentsPendingShutdown()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
        }

        // we need to decide what to do with these agents...
        // it really depends if its a VM based agent in which case it 
        // could be passed in the constructor and then the endpoint would either
        // consume it or destroy it. Also we need to get the list of machines from
        // the machines provisioning not admin, since admin may return machines that
        // are non-VMed.
        //
        // if its not a VM, then we need a shared pool of those for all endpoints
        // and the endpoints need to consume those only on demand
        // the pool could implement a degenerated form of machines provisioning
        // that does not implement start/stop, but rather just implements getallmachines
        //
        MachinesSlaEnforcementEndpoint endpoint = 
            new DefaultMachinesSlaEnforcementEndpoint(admin, pu, agents);
        endpoints.put(pu, endpoint);
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        DefaultMachinesSlaEnforcementEndpoint endpoint = (DefaultMachinesSlaEnforcementEndpoint) endpoints.remove(pu);
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
