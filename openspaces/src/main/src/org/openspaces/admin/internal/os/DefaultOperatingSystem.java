package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultOperatingSystem implements InternalOperatingSystem {

    private final String uid;

    private final OperatingSystemDetails details;

    private final Set<InternalOperatingSystemInfoProvider> operatingSystemInfoProviders = new ConcurrentHashSet<InternalOperatingSystemInfoProvider>();

    public DefaultOperatingSystem(OSDetails osDetails) {
        this.details = new DefaultOperatingSystemDetails(osDetails);
        this.uid = details.getUid();
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

    public String getUid() {
        return this.uid;
    }

    public OperatingSystemDetails getDetails() {
        return this.details;
    }

    private static final OperatingSystemStatistics NA_STATS = new DefaultOperatingSystemStatistics();

    public OperatingSystemStatistics getStatistics() {
        for (InternalOperatingSystemInfoProvider provider : operatingSystemInfoProviders) {
            try {
                return new DefaultOperatingSystemStatistics(provider.getOSStatistics());
            } catch (RemoteException e) {
                // simply try the next one
            }
        }
        // return NA on complete failure
        return NA_STATS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultOperatingSystem that = (DefaultOperatingSystem) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
