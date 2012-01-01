package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEventManager;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;

public class DefaultElasticMachineProvisioningProgressChangedEventManager 
    extends AbstractElasticProcessingUnitProgressChangedEventManager<ElasticMachineProvisioningProgressChangedEvent, ElasticMachineProvisioningProgressChangedEventListener>
    implements InternalElasticMachineProvisioningProgressChangedEventManager {

    public DefaultElasticMachineProvisioningProgressChangedEventManager(InternalAdmin admin) {
        super(admin);
    }

    @Override
    public void elasticMachineProvisioningProgressChanged(final ElasticMachineProvisioningProgressChangedEvent event) {
        super.pushEventToAllListeners(event);
    }
    
    @Override
    protected void fireEventToListener(ElasticMachineProvisioningProgressChangedEvent event, ElasticMachineProvisioningProgressChangedEventListener listener) {
        listener.elasticMachineProvisioningProgressChanged(event);
    }

}
