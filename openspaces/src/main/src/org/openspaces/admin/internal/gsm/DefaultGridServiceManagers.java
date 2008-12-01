package org.openspaces.admin.internal.gsm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagerEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceManager> gridServiceManagersByUID = new SizeConcurrentHashMap<String, GridServiceManager>();

    private final List<GridServiceManagerEventListener> eventListeners = new CopyOnWriteArrayList<GridServiceManagerEventListener>();

    public DefaultGridServiceManagers(InternalAdmin admin) {
        this.admin = admin;
    }

    public GridServiceManager[] getManagers() {
        return gridServiceManagersByUID.values().toArray(new GridServiceManager[0]);
    }

    public GridServiceManager getManagerByUID(String uid) {
        return gridServiceManagersByUID.get(uid);
    }

    public Map<String, GridServiceManager> getUids() {
        return Collections.unmodifiableMap(gridServiceManagersByUID);
    }

    public int getSize() {
        return gridServiceManagersByUID.size();
    }

    public boolean isEmpty() {
        return gridServiceManagersByUID.isEmpty();
    }

    public Iterator<GridServiceManager> iterator() {
        return gridServiceManagersByUID.values().iterator();
    }

    public void addGridServiceManager(final InternalGridServiceManager gridServiceManager) {
        GridServiceManager existingGSM = gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
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
        final InternalGridServiceManager existingGSM = (InternalGridServiceManager) gridServiceManagersByUID.remove(uid);
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
        return (InternalGridServiceManager) gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
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
