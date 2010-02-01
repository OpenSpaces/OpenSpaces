package org.openspaces.admin.internal.support;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;
import org.openspaces.admin.machine.events.MachineAddedEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventListener;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;
import org.openspaces.admin.space.events.SpaceAddedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.space.events.SpaceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEventListener;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventListener;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventListener;
import org.openspaces.admin.zone.events.ZoneAddedEventListener;
import org.openspaces.admin.zone.events.ZoneRemovedEventListener;

/**
 * @author kimchy
 */
public abstract class EventRegistrationHelper {

    public static void addEventListener(InternalAdmin admin, AdminEventListener eventListener) {
        if (eventListener instanceof ZoneAddedEventListener) {
            admin.getZones().getZoneAdded().add((ZoneAddedEventListener) eventListener);
        }
        if (eventListener instanceof ZoneRemovedEventListener) {
            admin.getZones().getZoneRemoved().add((ZoneRemovedEventListener) eventListener);
        }
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().add((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().add((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().add((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().add((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineStatisticsChangedEventListener) {
            admin.getVirtualMachines().getVirtualMachineStatisticsChanged().add((VirtualMachineStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachinesStatisticsChangedEventListener) {
            admin.getVirtualMachines().getStatisticsChanged().add((VirtualMachinesStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportsStatisticsChangedEventListener) {
            admin.getTransports().getStatisticsChanged().add((TransportsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportStatisticsChangedEventListener) {
            admin.getTransports().getTransportStatisticsChanged().add((TransportStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemsStatisticsChangedEventListener) {
            admin.getOperatingSystems().getStatisticsChanged().add((OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemStatisticsChangedEventListener) {
            admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().add((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().add((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().add((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().add((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentAddedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentAdded().add((GridServiceAgentAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentRemovedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentRemoved().add((GridServiceAgentRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerAddedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerAdded().add((ElasticServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerRemovedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerRemoved().add((ElasticServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().add((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().add((GridServiceContainerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().add((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().add((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().add((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().add((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().add((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().add((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().add((BackupGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceAddedEventListener) {
            admin.getSpaces().getSpaceAdded().add((SpaceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceRemovedEventListener) {
            admin.getSpaces().getSpaceRemoved().add((SpaceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceAddedEventListener) {
            admin.getSpaces().getSpaceInstanceAdded().add((SpaceInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceRemovedEventListener) {
            admin.getSpaces().getSpaceInstanceRemoved().add((SpaceInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceModeChangedEventListener) {
            admin.getSpaces().getSpaceModeChanged().add((SpaceModeChangedEventListener) eventListener);
        }
        if (eventListener instanceof ReplicationStatusChangedEventListener) {
            admin.getSpaces().getReplicationStatusChanged().add((ReplicationStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceStatisticsChanged().add((SpaceStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceInstanceStatisticsChanged().add((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public static void removeEventListener(InternalAdmin admin, AdminEventListener eventListener) {
        if (eventListener instanceof ZoneAddedEventListener) {
            admin.getZones().getZoneAdded().remove((ZoneAddedEventListener) eventListener);
        }
        if (eventListener instanceof ZoneRemovedEventListener) {
            admin.getZones().getZoneRemoved().remove((ZoneRemovedEventListener) eventListener);
        }
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().remove((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().remove((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().remove((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().remove((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineStatisticsChangedEventListener) {
            admin.getVirtualMachines().getVirtualMachineStatisticsChanged().remove((VirtualMachineStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachinesStatisticsChangedEventListener) {
            admin.getVirtualMachines().getStatisticsChanged().remove((VirtualMachinesStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportsStatisticsChangedEventListener) {
            admin.getTransports().getStatisticsChanged().remove((TransportsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportStatisticsChangedEventListener) {
            admin.getTransports().getTransportStatisticsChanged().remove((TransportStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemsStatisticsChangedEventListener) {
            admin.getOperatingSystems().getStatisticsChanged().remove((OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemStatisticsChangedEventListener) {
            admin.getOperatingSystems().getOperatingSystemStatisticsChanged().remove((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().remove((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().remove((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().remove((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().remove((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerAddedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerAdded().remove((ElasticServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerRemovedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerRemoved().remove((ElasticServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().remove((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().remove((GridServiceContainerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().remove((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().remove((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().remove((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().remove((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().remove((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().remove((BackupGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceAddedEventListener) {
            admin.getSpaces().getSpaceAdded().remove((SpaceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceRemovedEventListener) {
            admin.getSpaces().getSpaceRemoved().remove((SpaceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceAddedEventListener) {
            admin.getSpaces().getSpaceInstanceAdded().remove((SpaceInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceRemovedEventListener) {
            admin.getSpaces().getSpaceInstanceRemoved().remove((SpaceInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceModeChangedEventListener) {
            admin.getSpaces().getSpaceModeChanged().remove((SpaceModeChangedEventListener) eventListener);
        }
        if (eventListener instanceof ReplicationStatusChangedEventListener) {
            admin.getSpaces().getReplicationStatusChanged().remove((ReplicationStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceStatisticsChanged().remove((SpaceStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceInstanceStatisticsChanged().remove((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }
}
