package org.openspaces.admin.internal.machine;

import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.lus.DefaultLookupServices;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.os.DefaultOperatingSystem;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.transport.DefaultTransports;
import org.openspaces.admin.internal.transport.InternalTransports;
import org.openspaces.admin.internal.vm.DefaultVirtualMachines;
import org.openspaces.admin.internal.vm.InternalVirtualMachines;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private InternalAdmin admin;

    private String uid;

    private String host;

    private final InternalLookupServices lookupServices;

    private final InternalGridServiceManagers gridServiceManagers;

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalTransports transports = new DefaultTransports();

    private final InternalVirtualMachines virtualMachines;

    private final InternalProcessingUnitInstances processingUnitInstances = new DefaultProcessingUnitInstances();

    private final Map<String, SpaceInstance> spaceInstances = new ConcurrentHashMap<String, SpaceInstance>();

    private volatile OperatingSystem operatingSystem;

    public DefaultMachine(InternalAdmin admin, String uid, String host) {
        this.admin = admin;
        this.uid = uid;
        this.host = host;
        this.lookupServices = new DefaultLookupServices(admin);
        this.gridServiceManagers = new DefaultGridServiceManagers(admin);
        this.gridServiceContainers = new DefaultGridServiceContainers(admin);
        this.virtualMachines = new DefaultVirtualMachines(admin);
    }

    public String getUid() {
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

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstnace(uid);
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.put(spaceInstance.getUid(), spaceInstance);
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.remove(uid);
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
