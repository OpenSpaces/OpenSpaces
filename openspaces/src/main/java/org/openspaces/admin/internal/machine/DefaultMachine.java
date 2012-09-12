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
package org.openspaces.admin.internal.machine;

import java.util.Iterator;
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
import org.openspaces.admin.internal.os.DefaultOperatingSystem;
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
import org.openspaces.admin.os.OperatingSystem;
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

import com.gigaspaces.internal.os.OSDetails;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private final InternalAdmin admin;

    private final String uid;

    private final String hostAddress;

    private final InternalLookupServices lookupServices;

    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalGridServiceManagers gridServiceManagers;
    
    private final InternalElasticServiceManagers elasticServiceManagers;

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalTransports transports;

    private final InternalVirtualMachines virtualMachines;

    private final InternalProcessingUnitInstances processingUnitInstances;

    private final InternalSpaceInstances spaceInstances;

    private volatile OperatingSystem operatingSystem;

    public DefaultMachine(InternalAdmin admin, String uid, String hostAddress) {
        this.admin = admin;
        this.uid = uid;
        this.hostAddress = hostAddress;
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

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public String getHostAddress() {
        return this.hostAddress;
    }

    @Override
    public String getHostName() {
        return operatingSystem.getDetails().getHostName();
    }

    @Override
    public GridServiceAgent getGridServiceAgent() {
        Iterator<GridServiceAgent> it = gridServiceAgents.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public GridServiceAgents getGridServiceAgents() {
        return this.gridServiceAgents;
    }

    @Override
    public LookupServices getLookupServices() {
        return lookupServices;
    }

    @Override
    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }
    
    @Override
    public ElasticServiceManagers getElasticServiceManagers() {
        return elasticServiceManagers;
    }

    @Override
    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    @Override
    public boolean hasGridComponents() {
        return !gridServiceAgents.isEmpty() || !gridServiceManagers.isEmpty() || !elasticServiceManagers.isEmpty() || !gridServiceContainers.isEmpty() || !lookupServices.isEmpty();
    }

    @Override
    public Transports getTransports() {
        return transports;
    }

    @Override
    public void setOperatingSystem(OperatingSystem operatingSystem) {
        assertStateChangesPermitted();
        this.operatingSystem = operatingSystem;
    }

    public boolean hasOperatingSystem() {
        return operatingSystem != null;
    }

    private static OperatingSystem NA_OPERATING_SYSTEM = new DefaultOperatingSystem(new OSDetails(), null);

    @Override
    public OperatingSystem getOperatingSystem() {
        if (operatingSystem == null) {
            return NA_OPERATING_SYSTEM;
        }
        return this.operatingSystem;
    }

    @Override
    public VirtualMachines getVirtualMachines() {
        return this.virtualMachines;
    }

    @Override
    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    @Override
    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    @Override
    public void addLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    @Override
    public void removeLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    @Override
    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    @Override
    public ProcessingUnitInstance[] getProcessingUnitInstances(String processingUnitName) {
        return processingUnitInstances.getInstances(processingUnitName);
    }

    @Override
    public boolean contains(ProcessingUnitInstance processingUnitInstance) {
        return processingUnitInstances.contains(processingUnitInstance);
    }

    @Override
    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.getSpaceInstances();
    }

    @Override
    public boolean contains(SpaceInstance spaceInstance) {
        return spaceInstances.contains(spaceInstance);
    }

    @Override
    public SpaceInstanceAddedEventManager getSpaceInstanceAdded() {
        return spaceInstances.getSpaceInstanceAdded();
    }

    @Override
    public SpaceInstanceRemovedEventManager getSpaceInstanceRemoved() {
        return spaceInstances.getSpaceInstanceRemoved();
    }

    @Override
    public void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.addLifecycleListener(eventListener);
    }

    @Override
    public void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.removeLifecycleListener(eventListener);
    }

    @Override
    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    @Override
    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstance(uid);
    }

    @Override
    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.addSpaceInstance(spaceInstance);
    }

    @Override
    public void removeSpaceInstance(String uid) {
        spaceInstances.removeSpaceInstance(uid);
    }

    @Override
    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    @Override
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
        DefaultMachine that = (DefaultMachine) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

}
