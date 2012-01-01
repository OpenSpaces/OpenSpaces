package org.openspaces.admin.machine.events;

public interface ElasticMachineProvisioningFailureEventListener {
    
    void elasticMachineProvisioningFailure(ElasticMachineProvisioningFailureEvent event);
}
