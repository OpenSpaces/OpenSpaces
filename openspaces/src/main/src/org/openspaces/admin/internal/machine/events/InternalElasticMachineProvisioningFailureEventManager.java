package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventListener;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventManager;

public interface InternalElasticMachineProvisioningFailureEventManager 
    extends ElasticMachineProvisioningFailureEventManager,
            ElasticMachineProvisioningFailureEventListener {

}
