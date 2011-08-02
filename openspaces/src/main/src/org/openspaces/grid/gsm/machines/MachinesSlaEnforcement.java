package org.openspaces.grid.gsm.machines;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

/**
 * Enforces the MachinesSlaPolicy of all processing units by starting an enforcement endpoint for each PU.
 * The state is shared by all endpoints to detect conflicting operations.  
 * @author itaif
 *
 */
public class MachinesSlaEnforcement implements
        ServiceLevelAgreementEnforcement<CapacityMachinesSlaPolicy, MachinesSlaEnforcementEndpoint> {

    private final MachinesSlaEnforcementState state;
    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;

    private final Admin admin;

    public MachinesSlaEnforcement(Admin admin) {

        this.endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        this.state = new MachinesSlaEnforcementState();
        this.admin = admin;
    }

    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        
        if (!isEndpointDestroyed(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }

        // check pu zone matches container zones.
        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("PU has to have exactly 1 zone defined");
        }

        String zone = pu.getRequiredZones()[0];
        
        // Recover the endpoint state.
        // List all machines that have containers that match the specified pu (zone)
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            
            // found a container associated with this pu. take it.
            if (container.getZones().size() == 1 &&
                container.getZones().containsKey(zone) &&
                container.getMachine().getGridServiceAgents().getSize() == 1) {

                agents.add(container.getMachine().getGridServiceAgent());
           }
        }

        Collection<String> allUsedAgentUids = state.getAllUsedAgentUids();
        Iterator<GridServiceAgent> iterator = agents.iterator(); 
        while(iterator.hasNext()) {
            if (allUsedAgentUids.contains(iterator.next().getUid())) {
                iterator.remove();
            }
        }
        
        state.initProcessingUnit(pu, agents.toArray(new GridServiceAgent[agents.size()]));
        
        MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(pu, state);
        endpoints.put(pu, endpoint);
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        endpoints.remove(pu);
        state.destroyProcessingUnit(pu);
        
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
    
    private boolean isEndpointDestroyed(ProcessingUnit pu) {
        return 
            endpoints.get(pu) == null ||
            state.isProcessingUnitDestroyed(pu);
    }

   
}
