package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
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
        for (InternalOperatingSystemInfoProvider provider : operatingSystemInfoProviders) {
            try {
                return provider.getOperatingSystemStatistics();
            } catch (RemoteException e) {
                // simply try the next one
            }
        }
        // return NA on complete failure
        return new OperatingSystemStatistics();
    }
}
