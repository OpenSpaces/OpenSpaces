package org.openspaces.admin.internal.machine;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

/**
 * @author kimchy
 */
public interface InternalMachines extends Machines {

    Machine getMachineByUID(String uid);
    
    void addMachine(InternalMachine machine);

    void removeMachine(Machine machine);
    
    /**
     * If relevant raises events to relevant subscribers
     * @since 8.0.6
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);
}
