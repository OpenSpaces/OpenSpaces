package org.openspaces.admin.internal.zone;

import java.util.Map;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.DefaultElasticServiceManagers;
import org.openspaces.admin.internal.esm.InternalElasticServiceManagers;
import org.openspaces.admin.internal.gsa.DefaultGridServiceAgents;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.lus.DefaultLookupServices;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.machine.DefaultMachines;
import org.openspaces.admin.internal.machine.InternalMachines;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.space.DefaultSpaceInstances;
import org.openspaces.admin.internal.space.InternalSpaceInstances;
import org.openspaces.admin.internal.transport.DefaultTransports;
import org.openspaces.admin.internal.transport.InternalTransports;
import org.openspaces.admin.internal.vm.DefaultVirtualMachines;
import org.openspaces.admin.internal.vm.InternalVirtualMachines;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

/**
 * @author kimchy
 */
public class DefaultZone implements InternalZone {

    private final String name;

    private final InternalAdmin admin;

    private final InternalMachines machines;

    private final InternalLookupServices lookupServices;

    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalGridServiceManagers gridServiceManagers;
    
    private final InternalElasticServiceManagers elasticServiceManagers;

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalTransports transports;

    private final InternalVirtualMachines virtualMachines;

    private final InternalProcessingUnitInstances processingUnitInstances;

    private final InternalSpaceInstances spaceInstances;


    public DefaultZone(InternalAdmin admin, String name) {
        this.admin = admin;
        this.name = name;
        this.machines = new DefaultMachines(admin);
        this.gridServiceAgents = new DefaultGridServiceAgents(admin);
        this.lookupServices = new DefaultLookupServices(admin);
        this.gridServiceManagers = new DefaultGridServiceManagers(admin);
        this.elasticServiceManagers = new DefaultElasticServiceManagers(admin);
        this.gridServiceContainers = new DefaultGridServiceContainers(admin);
        this.virtualMachines = new DefaultVirtualMachines(admin);
        this.processingUnitInstances = new DefaultProcessingUnitInstances(admin);
        this.spaceInstances = new DefaultSpaceInstances(admin);
        this.transports = new DefaultTransports(admin);
    }

    public String getName() {
        return this.name;
    }

    public Machines getMachines() {
        return this.machines;
    }

    public GridServiceAgents getGridServiceAgents() {
        return this.gridServiceAgents;
    }

    public LookupServices getLookupServices() {
        return lookupServices;
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }
    
    public ElasticServiceManagers getElasticServiceManagers() {
        return elasticServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public boolean hasGridComponents() {
        return !gridServiceAgents.isEmpty() || !gridServiceManagers.isEmpty() || !elasticServiceManagers.isEmpty() || !gridServiceContainers.isEmpty() || !lookupServices.isEmpty();
    }

    public Transports getTransports() {
        return transports;
    }

    public VirtualMachines getVirtualMachines() {
        return this.virtualMachines;
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.getSpaceInstances();
    }

    public SpaceInstanceAddedEventManager getSpaceInstanceAdded() {
        return spaceInstances.getSpaceInstanceAdded();
    }

    public SpaceInstanceRemovedEventManager getSpaceInstanceRemoved() {
        return spaceInstances.getSpaceInstanceRemoved();
    }

    public void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.addLifecycleListener(eventListener);
    }

    public void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.removeLifecycleListener(eventListener);
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstance(uid);
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.addSpaceInstance(spaceInstance);
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.removeSpaceInstance(uid);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (ElasticServiceManager esm : elasticServiceManagers) {
            dumpResult.add(esm.generateDump(cause, context, processor));
        }
        for (GridServiceManager gsm : gridServiceManagers) {
            dumpResult.add(gsm.generateDump(cause, context, processor));
        }
        for (GridServiceContainer gsc : gridServiceContainers) {
            dumpResult.add(gsc.generateDump(cause, context, processor));
        }
        for (GridServiceAgent gsa : gridServiceAgents) {
            dumpResult.add(gsa.generateDump(cause, context, processor));
        }
        for (LookupService lus : lookupServices) {
            dumpResult.add(lus.generateDump(cause, context, processor));
        }
        return dumpResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultZone that = (DefaultZone) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
