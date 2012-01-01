package org.openspaces.grid.gsm.rebalancing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.exceptions.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

/**
 * Implements the elastic re-balancing algorithm based on the specified SLA. Each endpoint is
 * dedicated to the rebalancing of one processing unit. The SLA specifies the containers that are
 * approved for the pu deployment, and the algorithm enforces equal spread of instances per
 * container, and the primary instances per machine.
 * 
 * @author itaif
 * 
 */
public class RebalancingSlaEnforcement implements
ServiceLevelAgreementEnforcement<RebalancingSlaEnforcementEndpoint> {

    
    public void enableTracing() {
        state.enableTracing();
    }
    public List<FutureStatefulProcessingUnitInstance> getTrace() {
        return state.getDoneFutureStatefulDeployments();
    }
    
    private final RebalancingSlaEnforcementState state;
    
    private final HashMap<ProcessingUnit, RebalancingSlaEnforcementEndpoint> endpoints;

    public RebalancingSlaEnforcement() {
        state = new RebalancingSlaEnforcementState();
                
        endpoints = new HashMap<ProcessingUnit, RebalancingSlaEnforcementEndpoint>();
    }

    public void destroy() {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }

    public RebalancingSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("Processing Unit must have exactly one container zone defined.");
        }

        if (!isEndpointDestroyed(pu)) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for the pu already exists.");
        }

        ProcessingUnit otherPu1 = getEndpointsWithSameNameAs(pu);
        if (otherPu1 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same name already exists.");
        }

        ProcessingUnit otherPu2 = getEndpointsWithSameContainersZoneAs(pu);
        if (otherPu2 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same (containers) zone already exists: "
                    + otherPu2.getName());
        }

        RebalancingSlaEnforcementEndpoint endpoint = new DefaultRebalancingSlaEnforcementEndpoint(pu, state);

        endpoints.put(pu, endpoint);
        state.initProcessingUnit(pu);
        return endpoint;
    }
    
    public void destroyEndpoint(ProcessingUnit pu) {
        state.destroyProcessingUnit(pu);
        endpoints.remove(pu);
    }

    private boolean isEndpointDestroyed(ProcessingUnit pu) {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        return !endpoints.containsKey(pu) || 
               state.isDestroyedProcessingUnit(pu);
    }

    private ProcessingUnit getEndpointsWithSameContainersZoneAs(ProcessingUnit pu) {
        for (ProcessingUnit endpointPu : this.endpoints.keySet()) {
            if (getContainerZone(endpointPu).equals(getContainerZone(pu))) {
                return endpointPu;
            }
        }
        return null;
    }

    private ProcessingUnit getEndpointsWithSameNameAs(ProcessingUnit pu) {
        for (ProcessingUnit endpointPu : this.endpoints.keySet()) {
            if (endpointPu.getName().equals(pu.getName())) {
                return endpointPu;
            }
        }
        return null;
    }

    private static String getContainerZone(ProcessingUnit pu) {
        String[] zones = pu.getRequiredZones();
        if (zones.length == 0) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with no zones defined.");
        }

        if (zones.length > 1) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with " + zones.length + " zones : " + Arrays.toString(zones));
        }

        return zones[0];
    }

}
