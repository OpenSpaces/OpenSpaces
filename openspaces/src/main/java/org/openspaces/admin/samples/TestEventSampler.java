/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerLifecycleEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.events.ZoneLifecycleEventListener;

/**
 * @author kimchy
 */
public class TestEventSampler implements MachineLifecycleEventListener,
        ElasticServiceManagerLifecycleEventListener,
        GridServiceContainerLifecycleEventListener,
        GridServiceManagerLifecycleEventListener,
        ProcessingUnitLifecycleEventListener,
        ProcessingUnitInstanceLifecycleEventListener,
        LookupServiceLifecycleEventListener,
        VirtualMachineLifecycleEventListener,
        SpaceLifecycleEventListener,
        SpaceInstanceLifecycleEventListener,
        SpaceModeChangedEventListener,
        ReplicationStatusChangedEventListener,
        ZoneLifecycleEventListener {

    public static void main(String[] args) throws Exception {
        TestEventSampler eventSampler = new TestEventSampler();
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.addEventListener(eventSampler);

        Thread.sleep(10000000);
    }

    public void machineAdded(Machine machine) {
        System.out.println("Machine Added [" + machine.getUid() + "]");
    }

    public void machineRemoved(Machine machine) {
        System.out.println("Machine Removed [" + machine.getUid() + "]");
    }

    public void processingUnitAdded(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit Added [" + processingUnit.getName() + "]");
    }

    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        System.out.println("Processing Unit Removed [" + processingUnit.getName() + "]");
    }

    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Added [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        System.out.println("Processing Unit Instance Removed [" + processingUnitInstance.getClusterInfo() + "]");
    }

    public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
        System.out.println("GSM Added [" + gridServiceManager.getUid() + "]");
    }

    public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
        System.out.println("GSM Removed [" + gridServiceManager.getUid() + "]");
    }

    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Added [" + gridServiceContainer.getUid() + "]");
    }

    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        System.out.println("GSC Removed [" + gridServiceContainer.getUid() + "]");
    }

    public void lookupServiceAdded(LookupService lookupService) {
        System.out.println("LUS Added [" + lookupService.getUid() + "]");
    }

    public void lookupServiceRemoved(LookupService lookupService) {
        System.out.println("LUS Removed [" + lookupService.getUid() + "]");
    }

    public void virtualMachineAdded(VirtualMachine virtualMachine) {
        System.out.println("VM Added [" + virtualMachine.getUid() + "]");
    }

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {
        System.out.println("VM Removed [" + virtualMachine.getUid() + "]");
    }

    public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        System.out.println("PU [" + event.getProcessingUnit().getName() + "] Status changed from [" + event.getPreviousStatus() + "] to [" + event.getNewStatus() + "]");
    }

    public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
        if (event.isUnknown()) {
            System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] managin GSM UNKNOWN");
        } else {
            System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] new managing GSM [" + event.getNewGridServiceManager().getUid() + "]");
        }
    }

    public void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event) {
        System.out.println("Processing Unit [" + event.getProcessingUnit().getName() + "] Backup GSM [" + event.getType() + "] with uid [" + event.getGridServiceManager().getUid() + "]");
    }

    public void spaceAdded(Space space) {
        System.out.println("Space Added [" + space.getUid() + "]");
    }

    public void spaceRemoved(Space space) {
        System.out.println("Space Removed [" + space.getUid() + "]");
    }

    public void spaceInstanceAdded(SpaceInstance spaceInstance) {
        System.out.println("Space Instance Added [" + spaceInstance.getUid() + "]");
    }

    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        System.out.println("Space Instance Removed [" + spaceInstance.getUid() + "]");
    }

    public void spaceModeChanged(SpaceModeChangedEvent event) {
        System.out.println("Space Instance [" + event.getSpaceInstance().getUid() + "] changed mode from [" + event.getPreviousMode() + "] to [" + event.getNewMode() + "]");
    }

    public void replicationStatusChanged(ReplicationStatusChangedEvent event) {
        System.out.println("Space Instance [" + event.getSpaceInstance().getUid() + "] replication status changed from [" + event.getPreviousStatus() + "] to [" + event.getNewStatus() + "]");
    }

    public void zoneAdded(Zone zone) {
        System.out.println("Zone Added [" + zone.getName() + "]");
    }

    public void zoneRemoved(Zone zone) {
        System.out.println("Zone Removed [" + zone.getName() + "]");
    }

    public void elasticServiceManagerAdded(ElasticServiceManager elasticServiceManager) {
        System.out.println("ESM Added [" + elasticServiceManager.getUid() + "]");
    }

    public void elasticServiceManagerRemoved(ElasticServiceManager elasticServiceManager) {
        System.out.println("ESM Removed [" + elasticServiceManager.getUid() + "]");
    }
}
