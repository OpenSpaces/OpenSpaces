package org.openspaces.admin.internal.os;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.os.OperatingSystem;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultOperatingSystems implements InternalOperatingSystems {

    private final Map<String, OperatingSystem> operatingSystemsByUID = new SizeConcurrentHashMap<String, OperatingSystem>();

    public OperatingSystem[] getOperatingSystems() {
        return operatingSystemsByUID.values().toArray(new OperatingSystem[0]);
    }

    public Iterator<OperatingSystem> iterator() {
        return operatingSystemsByUID.values().iterator();
    }

    public int size() {
        return operatingSystemsByUID.size();
    }

    public OperatingSystem getByUID(String uid) {
        return operatingSystemsByUID.get(uid);
    }

    public void addOperatingSystem(OperatingSystem operatingSystem) {
        operatingSystemsByUID.put(operatingSystem.getUID(), operatingSystem);
    }

    public void removeOperatingSystem(String uid) {
        operatingSystemsByUID.remove(uid);
    }
}
