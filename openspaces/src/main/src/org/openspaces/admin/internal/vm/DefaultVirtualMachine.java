package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.VirtualMachineConfiguration;
import com.gigaspaces.jvm.VirtualMachineStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultVirtualMachine implements InternalVirtualMachine {

    private final String uid;

    private final VirtualMachineConfiguration config;

    private final Set<InternalVirtualMachineInfoProvider> virtualMachineInfoProviders = new ConcurrentHashSet<InternalVirtualMachineInfoProvider>();

    public DefaultVirtualMachine(VirtualMachineConfiguration config) {
        this.config = config;
        this.uid = config.getUid();
    }

    public String getUID() {
        return this.uid;
    }

    public void addVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        virtualMachineInfoProviders.add(virtualMachineInfoProvider);
    }

    public void removeVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        virtualMachineInfoProviders.remove(virtualMachineInfoProvider);
    }

    public boolean hasVirtualMachineInfoProviders() {
        return !virtualMachineInfoProviders.isEmpty();
    }

    public VirtualMachineConfiguration getConfiguration() {
        return this.config;
    }

    public VirtualMachineStatistics getStatistics() {
        for (InternalVirtualMachineInfoProvider provider : virtualMachineInfoProviders) {
            try {
                return provider.getVirtualMachineStatistics();
            } catch (RemoteException e) {
                // continue to the next one
            }
        }
        // all failed, return NA
        return new VirtualMachineStatistics();
    }
}
