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
package org.openspaces.admin.internal.gsc;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEventManager;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.InternalElasticGridServiceContainerProvisioningFailureEventManager;
import org.openspaces.admin.internal.gsc.events.DefaultElasticGridServiceContainerProvisioningFailureEventManager;
import org.openspaces.admin.internal.gsc.events.DefaultElasticGridServiceContainerProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.gsc.events.DefaultGridServiceContainerAddedEventManager;
import org.openspaces.admin.internal.gsc.events.DefaultGridServiceContainerRemovedEventManager;
import org.openspaces.admin.internal.gsc.events.InternalElasticGridServiceContainerProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.gsc.events.InternalGridServiceContainerAddedEventManager;
import org.openspaces.admin.internal.gsc.events.InternalGridServiceContainerRemovedEventManager;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainers implements InternalGridServiceContainers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceContainer> containers = new SizeConcurrentHashMap<String, GridServiceContainer>();

    private final InternalGridServiceContainerAddedEventManager gridServiceContainerAddedEventManager;

    private final InternalGridServiceContainerRemovedEventManager gridServiceContainerRemovedEventManager;

    private final InternalElasticGridServiceContainerProvisioningFailureEventManager elasticGridServiceContainerProvisioningFailureEventManager;
    
    private final InternalElasticGridServiceContainerProvisioningProgressChangedEventManager elasticGridServiceContainerProvisioningProgressChangedEventManager;
    
    public DefaultGridServiceContainers(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceContainerAddedEventManager = new DefaultGridServiceContainerAddedEventManager(this);
        this.gridServiceContainerRemovedEventManager = new DefaultGridServiceContainerRemovedEventManager(this);
        this.elasticGridServiceContainerProvisioningFailureEventManager = new DefaultElasticGridServiceContainerProvisioningFailureEventManager(admin);
        this.elasticGridServiceContainerProvisioningProgressChangedEventManager = new DefaultElasticGridServiceContainerProvisioningProgressChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public GridServiceContainerAddedEventManager getGridServiceContainerAdded() {
        return this.gridServiceContainerAddedEventManager;
    }

    public GridServiceContainerRemovedEventManager getGridServiceContainerRemoved() {
        return this.gridServiceContainerRemovedEventManager;
    }

    public GridServiceContainer[] getContainers() {
        return containers.values().toArray(new GridServiceContainer[0]);
    }

    public GridServiceContainer getContainerByUID(String uid) {
        return containers.get(uid);
    }

    public Map<String, GridServiceContainer> getUids() {
        return Collections.unmodifiableMap(containers);
    }

    public int getSize() {
        return containers.size();
    }

    public boolean isEmpty() {
        return containers.size() == 0;
    }

    public boolean waitFor(int numberOfGridServiceContainers) {
        return waitFor(numberOfGridServiceContainers, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfGridServiceContainers, long timeout, TimeUnit timeUnit) {
        if (numberOfGridServiceContainers == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            GridServiceContainerRemovedEventListener removed = new GridServiceContainerRemovedEventListener() {
                public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
                    latch.countDown();
                }
            };
            getGridServiceContainerRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceContainerRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfGridServiceContainers);
            GridServiceContainerAddedEventListener added = new GridServiceContainerAddedEventListener() {
                public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
                    latch.countDown();
                }
            };
            getGridServiceContainerAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceContainerAdded().remove(added);
            }
        }
    }
    
    public void addLifecycleListener(GridServiceContainerLifecycleEventListener eventListener) {
        getGridServiceContainerAdded().add(eventListener);
        getGridServiceContainerRemoved().add(eventListener);
    }

    public void removeLifecycleListener(GridServiceContainerLifecycleEventListener eventListener) {
        getGridServiceContainerAdded().remove(eventListener);
        getGridServiceContainerRemoved().remove(eventListener);
    }

    public Iterator<GridServiceContainer> iterator() {
        return Collections.unmodifiableCollection(containers.values()).iterator();
    }

    public void addGridServiceContainer(final InternalGridServiceContainer gridServiceContainer) {
        assertStateChangesPermitted();
        final GridServiceContainer existingGSC = containers.put(gridServiceContainer.getUid(), gridServiceContainer);
        if (existingGSC == null) {
            gridServiceContainerAddedEventManager.gridServiceContainerAdded(gridServiceContainer);
        }
    }

    public InternalGridServiceContainer removeGridServiceContainer(String uid) {
        assertStateChangesPermitted();
        final InternalGridServiceContainer existingGSC = (InternalGridServiceContainer) containers.remove(uid);
        if (existingGSC != null) {
            gridServiceContainerRemovedEventManager.gridServiceContainerRemoved(existingGSC);
        }
        return existingGSC;
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (GridServiceContainer gsc : this) {
            dumpResult.add(gsc.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    @Override
    public ElasticGridServiceContainerProvisioningFailureEventManager getElasticGridServiceContainerProvisioningFailure() {
        return elasticGridServiceContainerProvisioningFailureEventManager;
    }

    @Override
    public ElasticGridServiceContainerProvisioningProgressChangedEventManager getElasticGridServiceContainerProvisioningProgressChanged() {
        return elasticGridServiceContainerProvisioningProgressChangedEventManager;
    }

    @Override
    public void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event) {
        if (event instanceof ElasticGridServiceContainerProvisioningFailureEvent) {
            elasticGridServiceContainerProvisioningFailureEventManager.elasticGridServiceContainerProvisioningFailure((ElasticGridServiceContainerProvisioningFailureEvent)event);
        }
        else if (event instanceof ElasticGridServiceContainerProvisioningProgressChangedEvent) {
            elasticGridServiceContainerProvisioningProgressChangedEventManager.elasticGridServiceContainerProvisioningProgressChanged((ElasticGridServiceContainerProvisioningProgressChangedEvent)event);
        }
    }
}
