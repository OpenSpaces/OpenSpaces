package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultVirtualMachine implements InternalVirtualMachine {

    private final String uid;

    private final VirtualMachineDetails details;

    private final Set<InternalVirtualMachineInfoProvider> virtualMachineInfoProviders = new ConcurrentHashSet<InternalVirtualMachineInfoProvider>();

    public DefaultVirtualMachine(JVMDetails details) {
        this.details = new DefaultVirtualMachineDetails(details);
        this.uid = details.getUid();
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

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    private static final VirtualMachineStatistics NA_STATS = new DefaultVirtualMachineStatistics();

    public VirtualMachineStatistics getStatistics() {
        for (InternalVirtualMachineInfoProvider provider : virtualMachineInfoProviders) {
            try {
                return new DefaultVirtualMachineStatistics(provider.getJVMStatistics());
            } catch (RemoteException e) {
                // continue to the next one
            }
        }
        // all failed, return NA
        return NA_STATS;
    }
}
