package org.openspaces.admin.internal.admin.gsm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.GridServiceManager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final Map<String, GridServiceManager> gridServiceManagerMap = new SizeConcurrentHashMap<String, GridServiceManager>();

    public GridServiceManager[] getManagers() {
        return gridServiceManagerMap.values().toArray(new GridServiceManager[0]);
    }

    public GridServiceManager getManagerByUID(String uid) {
        return gridServiceManagerMap.get(uid);
    }

    public int size() {
        return gridServiceManagerMap.size();
    }

    public Iterator<GridServiceManager> iterator() {
        return gridServiceManagerMap.values().iterator();
    }

    public void addGridServiceManager(InternalGridServiceManager gridServiceManager) {
        gridServiceManagerMap.put(gridServiceManager.getUID(), gridServiceManager);
    }

    public InternalGridServiceManager removeGridServiceManager(String uid) {
        return (InternalGridServiceManager) gridServiceManagerMap.remove(uid);
    }

    public InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        return (InternalGridServiceManager) gridServiceManagerMap.put(gridServiceManager.getUID(), gridServiceManager);
    }
}
