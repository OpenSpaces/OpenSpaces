package org.openspaces.admin.internal.admin;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.GridServiceContainer;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainers implements InternalGridServiceContainers {

    private final Map<String, GridServiceContainer> gridServiceContainerMap = new SizeConcurrentHashMap<String, GridServiceContainer>();

    public GridServiceContainer[] getContainers() {
        return gridServiceContainerMap.values().toArray(new GridServiceContainer[0]);
    }

    public GridServiceContainer getContainerByUID(String uid) {
        return gridServiceContainerMap.get(uid);
    }

    public int size() {
        return gridServiceContainerMap.size();
    }

    public Iterator<GridServiceContainer> iterator() {
        return gridServiceContainerMap.values().iterator();
    }

    public void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        gridServiceContainerMap.put(gridServiceContainer.getUID(), gridServiceContainer);
    }

    public InternalGridServiceContainer removeGridServiceContainer(String uid) {
        return (InternalGridServiceContainer) gridServiceContainerMap.remove(uid);
    }

    public InternalGridServiceContainer replaceGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        return (InternalGridServiceContainer) gridServiceContainerMap.put(gridServiceContainer.getUID(), gridServiceContainer);
    }
}