package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEvent;


public class DefaultElasticMachineProvisioningFailureEvent 
    extends AbstractElasticProcessingUnitFailureEvent
    implements ElasticMachineProvisioningFailureEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public DefaultElasticMachineProvisioningFailureEvent() {
        super();
    }
}
