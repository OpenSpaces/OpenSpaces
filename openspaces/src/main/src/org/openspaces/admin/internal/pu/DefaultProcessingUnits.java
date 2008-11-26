package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnits implements InternalProcessingUnits {

    private final Map<String, ProcessingUnit> processingUnits = new ConcurrentHashMap<String, ProcessingUnit>();

    public Iterator<ProcessingUnit> iterator() {
        return processingUnits.values().iterator();
    }

    public ProcessingUnit[] getProcessingUnits() {
        return processingUnits.values().toArray(new ProcessingUnit[0]);
    }

    public ProcessingUnit getProcessingUnit(String name) {
        return processingUnits.get(name);
    }

    public void addProcessingUnit(ProcessingUnit processingUnit) {
        processingUnits.put(processingUnit.getName(), processingUnit);
    }

    public void removeProcessingUnit(String name) {
        processingUnits.remove(name);
    }
}
