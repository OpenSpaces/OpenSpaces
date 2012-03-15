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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningFailureEventManager;
import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.machine.events.DefaultMachineAddedEventManager;
import org.openspaces.admin.internal.machine.events.DefaultMachineRemovedEventManager;
import org.openspaces.admin.internal.machine.events.InternalElasticMachineProvisioningFailureEventManager;
import org.openspaces.admin.internal.machine.events.InternalElasticMachineProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.machine.events.InternalMachineAddedEventManager;
import org.openspaces.admin.internal.machine.events.InternalMachineRemovedEventManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventManager;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventManager;
import org.openspaces.admin.machine.events.MachineAddedEventListener;
import org.openspaces.admin.machine.events.MachineAddedEventManager;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventManager;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultMachines implements InternalMachines {

    private final InternalAdmin admin;

    private final InternalMachineAddedEventManager machineAddedEventManager;

    private final InternalMachineRemovedEventManager machineRemovedEventManager;

    private final InternalElasticMachineProvisioningProgressChangedEventManager elasticMachineProvisioningProgressChangedEventManager;
    
    private final InternalElasticMachineProvisioningFailureEventManager elasticMachineProvisioningFailureEventManager;
    
    private final Map<String, Machine> machinesById = new SizeConcurrentHashMap<String, Machine>();

    private final Map<String, Machine> machinesByHostAddress = new ConcurrentHashMap<String, Machine>();

    private final Map<String, Machine> machinesByHostNames = new ConcurrentHashMap<String, Machine>();

    

    public DefaultMachines(InternalAdmin admin) {
        this.admin = admin;
        this.machineAddedEventManager = new DefaultMachineAddedEventManager(this);
        this.machineRemovedEventManager = new DefaultMachineRemovedEventManager(this);
        this.elasticMachineProvisioningProgressChangedEventManager = new DefaultElasticMachineProvisioningProgressChangedEventManager(admin);
        this.elasticMachineProvisioningFailureEventManager = new DefaultElasticMachineProvisioningFailureEventManager(admin);
    }

    @Override
    public Admin getAdmin() {
        return this.admin;
    }

    @Override
    public Machine[] getMachines() {
        return machinesById.values().toArray(new Machine[0]);
    }

    @Override
    public MachineAddedEventManager getMachineAdded() {
        return this.machineAddedEventManager;
    }

    @Override
    public MachineRemovedEventManager getMachineRemoved() {
        return this.machineRemovedEventManager;
    }

    @Override
    public int getSize() {
        return machinesById.size();
    }

    @Override
    public boolean isEmpty() {
        return machinesById.size() == 0;
    }

    @Override
    public boolean waitFor(int numberOfMachines) {
        return waitFor(numberOfMachines, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public boolean waitFor(int numberOfMachines, long timeout, TimeUnit timeUnit) {
        if (numberOfMachines == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            MachineRemovedEventListener removed = new MachineRemovedEventListener() {
                @Override
                public void machineRemoved(Machine machine) {
                    latch.countDown();
                }
            };
            getMachineRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getMachineRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfMachines);
            MachineAddedEventListener added = new MachineAddedEventListener() {
                @Override
                public void machineAdded(Machine machine) {
                    latch.countDown();
                }
            };
            getMachineAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getMachineAdded().remove(added);
            }
        }
    }

    @Override
    public Machine waitFor(String hostAddress) {
        return waitFor(hostAddress, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Machine waitFor(final String hostAddress, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Machine> ref = new AtomicReference<Machine>();
        MachineAddedEventListener added = new MachineAddedEventListener() {
            @Override
            public void machineAdded(Machine machine) {
                if (machine.getHostAddress().equalsIgnoreCase(hostAddress) || machine.getHostName().equalsIgnoreCase(hostAddress)) {
                    ref.set(machine);
                    latch.countDown();
                }
            }
        };
        getMachineAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getMachineAdded().remove(added);
        }
    }

    @Override
    public Iterator<Machine> iterator() {
        return Collections.unmodifiableCollection(machinesById.values()).iterator();
    }

    @Override
    public Machine getMachineByUID(String uid) {
        return machinesById.get(uid);
    }

    @Override
    public Machine getMachineByHostAddress(String ipAddress) {
        return machinesByHostAddress.get(ipAddress);
    }

    @Override
    public Machine getMachineByHostName(String hostName) {
        return machinesByHostNames.get(hostName);
    }

    @Override
    public Map<String, Machine> getUids() {
        return Collections.unmodifiableMap(machinesById);
    }

    @Override
    public Map<String, Machine> getHostsByAddress() {
        return Collections.unmodifiableMap(machinesByHostAddress);
    }

    @Override
    public Map<String, Machine> getHostsByName() {
        return Collections.unmodifiableMap(machinesByHostNames);
    }

    @Override
    public void addLifecycleListener(MachineLifecycleEventListener eventListener) {
        getMachineAdded().add(eventListener);
        getMachineRemoved().add(eventListener);
    }

    @Override
    public void removeLifeycleListener(MachineLifecycleEventListener eventListener) {
        getMachineAdded().remove(eventListener);
        getMachineRemoved().remove(eventListener);
    }

    @Override
    public void addMachine(final InternalMachine machine) {
        assertStateChangesPermitted();
        machinesByHostAddress.put(machine.getHostAddress(), machine);
        machinesByHostNames.put(machine.getHostName(), machine);
        Machine existingMachine = machinesById.put(machine.getUid(), machine);
        if (existingMachine == null) {
            machineAddedEventManager.machineAdded(machine);
        }
    }

    @Override
    public void removeMachine(final Machine machine) {
        assertStateChangesPermitted();
        // if no vms on the machine, we can remove them
        if (machine.getVirtualMachines().isEmpty()) {
            machinesByHostAddress.remove(machine.getHostAddress());
            machinesByHostNames.remove(machine.getHostName());
            final Machine existingMachine = machinesById.remove(machine.getUid());
            if (existingMachine != null) {
                machineRemovedEventManager.machineRemoved(machine);
            }
        }
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    @Override
    public ElasticMachineProvisioningFailureEventManager getElasticMachineProvisioningFailure() {
        return elasticMachineProvisioningFailureEventManager;
    }

    @Override
    public ElasticMachineProvisioningProgressChangedEventManager getElasticMachineProvisioningProgressChanged() {
        return elasticMachineProvisioningProgressChangedEventManager;
    }

    @Override
    public void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event) {
        if (event instanceof ElasticMachineProvisioningFailureEvent) {
            elasticMachineProvisioningFailureEventManager.elasticMachineProvisioningFailure((ElasticMachineProvisioningFailureEvent)event);
        }
        else if (event instanceof ElasticMachineProvisioningProgressChangedEvent) {
            elasticMachineProvisioningProgressChangedEventManager.elasticMachineProvisioningProgressChanged((ElasticMachineProvisioningProgressChangedEvent)event);
        }
    }

}
