package org.openspaces.admin.internal.pu;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnits implements InternalProcessingUnits {

    private final InternalAdmin admin;

    private final Map<String, ProcessingUnit> processingUnits = new SizeConcurrentHashMap<String, ProcessingUnit>();

    private List<ProcessingUnitEventListener> eventListeners = new CopyOnWriteArrayList<ProcessingUnitEventListener>();

    public DefaultProcessingUnits(InternalAdmin admin) {
        this.admin = admin;
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public Iterator<ProcessingUnit> iterator() {
        return processingUnits.values().iterator();
    }

    public ProcessingUnit[] getProcessingUnits() {
        return processingUnits.values().toArray(new ProcessingUnit[0]);
    }

    public ProcessingUnit getProcessingUnit(String name) {
        return processingUnits.get(name);
    }

    public Map<String, ProcessingUnit> getNames() {
        return Collections.unmodifiableMap(processingUnits);
    }

    public int getSize() {
        return processingUnits.size();
    }

    public boolean isEmpty() {
        return processingUnits.size() == 0;
    }

    public void addProcessingUnit(final ProcessingUnit processingUnit) {
        ProcessingUnit existingProcessingUnit = processingUnits.put(processingUnit.getName(), processingUnit);
        if (existingProcessingUnit == null) {
            for (final ProcessingUnitEventListener listener : eventListeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitAdded(processingUnit);
                    }
                });
            }
        }
    }

    public void removeProcessingUnit(String name) {
        final ProcessingUnit existingProcessingUnit = processingUnits.remove(name);
        if (existingProcessingUnit != null) {
            for (final ProcessingUnitEventListener listener : eventListeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitRemoved(existingProcessingUnit);
                    }
                });
            }
        }
    }

    public List<ProcessingUnitEventListener> getEventListeners() {
        return this.eventListeners;
    }

    public void addEventListener(final ProcessingUnitEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (ProcessingUnit processingUnit : getProcessingUnits()) {
                    eventListener.processingUnitAdded(processingUnit);
                    for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                        eventListener.processingUnitInstanceAdded(processingUnitInstance);
                    }
                }
            }
        });
        eventListeners.add(eventListener);
    }

    public void removeEventListener(ProcessingUnitEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}
