package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;
import org.openspaces.admin.AdminException;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultOperatingSystem implements InternalOperatingSystem {

    private final String uid;

    private final OperatingSystemConfiguration config;

    private final Set<InternalOperatingSystemInfoProvider> operatingSystemInfoProviders = new ConcurrentHashSet<InternalOperatingSystemInfoProvider>();

    public DefaultOperatingSystem(OperatingSystemConfiguration config) {
        this.config = config;
        this.uid = config.getUID();
    }

    public void addOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        operatingSystemInfoProviders.add(provider);
    }

    public void removeOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider) {
        operatingSystemInfoProviders.remove(provider);
    }

    public boolean hasOperatingSystemInfoProviders() {
        return !operatingSystemInfoProviders.isEmpty();
    }

    public String getUID() {
        return this.uid;
    }

    public OperatingSystemConfiguration getConfiguration() {
        return this.config;
    }

    public OperatingSystemStatistics getStatistics() {
        Iterator<InternalOperatingSystemInfoProvider> iter = operatingSystemInfoProviders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalStateException("No transport information provider is bounded to os [" + uid + "]");
        }
        try {
            return iter.next().getOperatingSystemStatistics();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport statistics for os [" + uid + "]", e);
        }
    }
}
