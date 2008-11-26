package org.openspaces.admin.internal.machine;

import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.lus.DefaultLookupServices;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.os.DefaultOperatingSystem;
import org.openspaces.admin.internal.transport.DefaultTransports;
import org.openspaces.admin.internal.transport.InternalTransports;
import org.openspaces.admin.internal.vm.DefaultVirtualMachines;
import org.openspaces.admin.internal.vm.InternalVirtualMachines;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private String uid;

    private String host;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    private final InternalTransports transports = new DefaultTransports();

    private final InternalVirtualMachines virtualMachines = new DefaultVirtualMachines();

    private volatile OperatingSystem operatingSystem;

    public DefaultMachine(String uid, String host) {
        this.uid = uid;
        this.host = host;
    }

    public String getUID() {
        return this.uid;
    }

    public String getHost() {
        return this.host;
    }

    public LookupServices getLookupServices() {
        return lookupServices;
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public boolean hasGridComponents() {
        return !gridServiceManagers.isEmpty() || !gridServiceContainers.isEmpty() || !lookupServices.isEmpty();
    }

    public Transports getTransports() {
        return transports;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public boolean hasOperatingSystem() {
        return operatingSystem != null;
    }

    private static OperatingSystem NA_OPERATING_SYSTEM = new DefaultOperatingSystem(new OSDetails());

    public OperatingSystem getOperatingSystem() {
        if (operatingSystem == null) {
            return NA_OPERATING_SYSTEM;
        }
        return this.operatingSystem;
    }

    public VirtualMachines getVirtualMachines() {
        return this.virtualMachines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultMachine that = (DefaultMachine) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
