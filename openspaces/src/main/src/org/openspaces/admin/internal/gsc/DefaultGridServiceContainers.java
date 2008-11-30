package org.openspaces.admin.internal.gsc;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainerEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainers implements InternalGridServiceContainers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceContainer> gridServiceContainerMap = new SizeConcurrentHashMap<String, GridServiceContainer>();

    private final List<GridServiceContainerEventListener> eventListeners = new CopyOnWriteArrayList<GridServiceContainerEventListener>();

    public DefaultGridServiceContainers(InternalAdmin admin) {
        this.admin = admin;
    }

    public GridServiceContainer[] getContainers() {
        return gridServiceContainerMap.values().toArray(new GridServiceContainer[0]);
    }

    public GridServiceContainer getContainerByUID(String uid) {
        return gridServiceContainerMap.get(uid);
    }

    public int size() {
        return gridServiceContainerMap.size();
    }

    public boolean isEmpty() {
        return gridServiceContainerMap.isEmpty();
    }

    public Iterator<GridServiceContainer> iterator() {
        return gridServiceContainerMap.values().iterator();
    }

    public void addGridServiceContainer(final InternalGridServiceContainer gridServiceContainer) {
        final GridServiceContainer existingGSC = gridServiceContainerMap.put(gridServiceContainer.getUID(), gridServiceContainer);
        if (existingGSC == null) {
            for (final GridServiceContainerEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.gridServiceContainerAdded(gridServiceContainer);
                    }
                });
            }
        }
    }

    public InternalGridServiceContainer removeGridServiceContainer(String uid) {
        final InternalGridServiceContainer existingGSC = (InternalGridServiceContainer) gridServiceContainerMap.remove(uid);
        if (existingGSC != null) {
            for (final GridServiceContainerEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.gridServiceContainerRemoved(existingGSC);
                    }
                });
            }
        }
        return existingGSC;
    }

    public void addEventListener(final GridServiceContainerEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (GridServiceContainer gridServiceContainer : getContainers()) {
                    eventListener.gridServiceContainerAdded(gridServiceContainer);
                }
            }
        });
        eventListeners.add(eventListener);
    }

    public void removeEventListener(GridServiceContainerEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}