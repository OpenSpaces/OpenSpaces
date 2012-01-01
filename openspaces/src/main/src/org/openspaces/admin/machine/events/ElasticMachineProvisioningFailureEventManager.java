package org.openspaces.admin.machine.events;

public interface ElasticMachineProvisioningFailureEventManager {

    void add(ElasticMachineProvisioningFailureEventListener listener);

    void remove(ElasticMachineProvisioningFailureEventListener listener);

}
