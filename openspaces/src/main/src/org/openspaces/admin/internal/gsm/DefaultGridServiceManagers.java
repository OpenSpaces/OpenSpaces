package org.openspaces.admin.internal.gsm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagerEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceManager> gridServiceManagerMap = new SizeConcurrentHashMap<String, GridServiceManager>();

    private final List<GridServiceManagerEventListener> eventListeners = new CopyOnWriteArrayList<GridServiceManagerEventListener>();

    public DefaultGridServiceManagers(InternalAdmin admin) {
        this.admin = admin;
    }

    public GridServiceManager[] getManagers() {
        return gridServiceManagerMap.values().toArray(new GridServiceManager[0]);
    }

    public GridServiceManager getManagerByUID(String uid) {
        return gridServiceManagerMap.get(uid);
    }

    public int size() {
        return gridServiceManagerMap.size();
    }

    public boolean isEmpty() {
        return gridServiceManagerMap.isEmpty();
    }

    public Iterator<GridServiceManager> iterator() {
        return gridServiceManagerMap.values().iterator();
    }

    public void addGridServiceManager(final InternalGridServiceManager gridServiceManager) {
        GridServiceManager existingGSM = gridServiceManagerMap.put(gridServiceManager.getUID(), gridServiceManager);
        if (existingGSM == null) {
            for (final GridServiceManagerEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.gridServiceManagerAdded(gridServiceManager);
                    }
                });
            }
        }
    }

    public InternalGridServiceManager removeGridServiceManager(String uid) {
        final InternalGridServiceManager existingGSM = (InternalGridServiceManager) gridServiceManagerMap.remove(uid);
        if (existingGSM != null) {
            for (final GridServiceManagerEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.gridServiceManagerRemoved(existingGSM);
                    }
                });
            }
        }
        return existingGSM;
    }

    public InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        return (InternalGridServiceManager) gridServiceManagerMap.put(gridServiceManager.getUID(), gridServiceManager);
    }

    public void addEventListener(final GridServiceManagerEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (GridServiceManager gridServiceManager : getManagers()) {
                    eventListener.gridServiceManagerAdded(gridServiceManager);
                }
            }
        });
        eventListeners.add(eventListener);
    }

    public void removeEventListener(GridServiceManagerEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}
