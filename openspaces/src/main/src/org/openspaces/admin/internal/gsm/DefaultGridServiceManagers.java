package org.openspaces.admin.internal.gsm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventManager;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceManager> gridServiceManagersByUID = new SizeConcurrentHashMap<String, GridServiceManager>();

    private final InternalGridServiceManagerAddedEventManager gridServiceManagerAddedEventManager;

    private final InternalGridServiceManagerRemovedEventManager gridServiceManagerRemovedEventManager;

    public DefaultGridServiceManagers(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceManagerAddedEventManager = new DefaultGridServiceManagerAddedEventManager(this);
        this.gridServiceManagerRemovedEventManager = new DefaultGridServiceManagerRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public GridServiceManagerAddedEventManager getGridServiceManagerAdded() {
        return this.gridServiceManagerAddedEventManager;
    }

    public GridServiceManagerRemovedEventManager getGridServiceManagerRemoved() {
        return this.gridServiceManagerRemovedEventManager;
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
            gridServiceManagerAddedEventManager.gridServiceManagerAdded(gridServiceManager);
        }
    }

    public InternalGridServiceManager removeGridServiceManager(String uid) {
        final InternalGridServiceManager existingGSM = (InternalGridServiceManager) gridServiceManagersByUID.remove(uid);
        if (existingGSM != null) {
            gridServiceManagerRemovedEventManager.gridServiceManagerRemoved(existingGSM);
        }
        return existingGSM;
    }

    public InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        return (InternalGridServiceManager) gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
    }
}
