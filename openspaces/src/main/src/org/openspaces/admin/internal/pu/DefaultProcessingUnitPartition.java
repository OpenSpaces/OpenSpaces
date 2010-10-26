package org.openspaces.admin.internal.pu;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openspaces.admin.internal.admin.InternalAdmin;
/**
 * @author kimchy
 */
public class DefaultProcessingUnitPartition implements InternalProcessingUnitPartition {

    private final ProcessingUnit processingUnit;

    private final int patitionId;

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    public DefaultProcessingUnitPartition(ProcessingUnit processingUnit, int patitionId) {
        this.processingUnit = processingUnit;
        this.patitionId = patitionId;
    }

    public int getPartitionId() {
        return this.patitionId;
    }

    public ProcessingUnitInstance[] getInstances() {
        return processingUnitInstances.values().toArray(new ProcessingUnitInstance[0]);
    }

    public ProcessingUnit getProcessingUnit() {
        return this.processingUnit;
    }

    public ProcessingUnitInstance getPrimary() {
        for (ProcessingUnitInstance processingUnitInstance : this) {
            if (processingUnitInstance.isEmbeddedSpaces() && processingUnitInstance.getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                return processingUnitInstance;
            }
        }
        return null;
    }

    public ProcessingUnitInstance getBackup() {
        for (ProcessingUnitInstance processingUnitInstance : this) {
            if (processingUnitInstance.isEmbeddedSpaces() && processingUnitInstance.getSpaceInstance().getMode() == SpaceMode.BACKUP) {
                return processingUnitInstance;
            }
        }
        return null;
    }

    public Iterator<ProcessingUnitInstance> iterator() {
        return processingUnitInstances.values().iterator();
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        assertStateChangesPermitted();
        processingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        assertStateChangesPermitted();
        processingUnitInstances.remove(uid);
    }
    
    private void assertStateChangesPermitted() {
        ((InternalAdmin)this.processingUnit.getAdmin()).assertStateChangesPermitted();
    }
}
