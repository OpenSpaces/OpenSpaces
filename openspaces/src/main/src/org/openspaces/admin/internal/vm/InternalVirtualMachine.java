package org.openspaces.admin.internal.vm;

import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface InternalVirtualMachine extends VirtualMachine, InternalMachineAware {

    void addVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider);

    void removeVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider);

    boolean hasVirtualMachineInfoProviders();

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);

    GridServiceAgents getGridServiceAgents();

    GridServiceManagers getGridServiceManagers();
    
    ElasticServiceManagers getElasticServiceManagers();

    GridServiceContainers getGridServiceContainers();
}
