package org.openspaces.admin.internal.os;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystemsStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    public OperatingSystemsStatistics getStatistics() {
        List<OperatingSystemStatistics> stats = new ArrayList<OperatingSystemStatistics>();
        for (OperatingSystem os : operatingSystemsByUID.values()) {
            stats.add(os.getStatistics());
        }
        return new DefaultOperatingSystemsStatistics(stats.toArray(new OperatingSystemStatistics[stats.size()]));
    }

    public OperatingSystem getByUID(String uid) {
        return operatingSystemsByUID.get(uid);
    }

    public Map<String, OperatingSystem> getUids() {
        return Collections.unmodifiableMap(operatingSystemsByUID);
    }

    public void addOperatingSystem(OperatingSystem operatingSystem) {
        operatingSystemsByUID.put(operatingSystem.getUid(), operatingSystem);
    }

    public void removeOperatingSystem(String uid) {
        operatingSystemsByUID.remove(uid);
    }
}
