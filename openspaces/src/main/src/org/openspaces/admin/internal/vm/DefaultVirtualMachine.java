package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import org.openspaces.admin.machine.Machine;
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

    private volatile Machine machine;

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

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultVirtualMachine that = (DefaultVirtualMachine) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
