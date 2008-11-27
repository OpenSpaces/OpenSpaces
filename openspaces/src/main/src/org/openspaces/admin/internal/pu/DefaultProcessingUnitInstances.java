package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstances implements InternalProcessingUnitInstances {

    private final Map<String, ProcessingUnitInstance> orphanedProcessingUnitInstnaces = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstnaces = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    public void addOrphaned(ProcessingUnitInstance processingUnitInstance) {
        orphanedProcessingUnitInstnaces.put(processingUnitInstance.getUID(), processingUnitInstance);
    }

    public ProcessingUnitInstance removeOrphaned(String uid) {
        return orphanedProcessingUnitInstnaces.remove(uid);
    }

    public void addInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstnaces.put(processingUnitInstance.getUID(), processingUnitInstance);
    }

    public ProcessingUnitInstance removeInstnace(String uid) {
        return processingUnitInstnaces.remove(uid);
    }

    public ProcessingUnitInstance[] getOrphaned() {
        return orphanedProcessingUnitInstnaces.values().toArray(new ProcessingUnitInstance[0]);
    }

    public Iterator<ProcessingUnitInstance> getInstancesIt() {
        return processingUnitInstnaces.values().iterator();
    }

    public ProcessingUnitInstance[] getInstances() {
        return processingUnitInstnaces.values().toArray(new ProcessingUnitInstance[0]);
    }
}
