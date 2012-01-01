package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEventManager;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventListener;

public class DefaultElasticMachineProvisioningFailureEventManager 
extends AbstractElasticProcessingUnitFailureEventManager<ElasticMachineProvisioningFailureEvent, ElasticMachineProvisioningFailureEventListener>    
implements InternalElasticMachineProvisioningFailureEventManager {

    public DefaultElasticMachineProvisioningFailureEventManager(InternalAdmin admin) {
        super(admin);
    }

    @Override
    public void elasticMachineProvisioningFailure(ElasticMachineProvisioningFailureEvent event) {
        super.pushEventToAllListeners(event);
        
    }

    @Override
    protected void fireEventToListener(
            ElasticMachineProvisioningFailureEvent event,
            ElasticMachineProvisioningFailureEventListener listener) {
        
        listener.elasticMachineProvisioningFailure(event);
    }
}