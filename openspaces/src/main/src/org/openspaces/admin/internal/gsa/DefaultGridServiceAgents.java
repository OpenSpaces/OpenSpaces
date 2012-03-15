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
package org.openspaces.admin.internal.gsa;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEventManager;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventManager;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventManager;
import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningFailureEventManager;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.gsa.events.DefaultGridServiceAgentAddedEventManager;
import org.openspaces.admin.internal.gsa.events.DefaultGridServiceAgentRemovedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalElasticGridServiceAgentProvisioningFailureEventManager;
import org.openspaces.admin.internal.gsa.events.InternalElasticGridServiceAgentProvisioningProgressChangedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalGridServiceAgentAddedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalGridServiceAgentRemovedEventManager;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgents implements InternalGridServiceAgents {

    private final InternalAdmin admin;

    private final Map<String, GridServiceAgent> agents = new SizeConcurrentHashMap<String, GridServiceAgent>();

    private final Map<String, GridServiceAgent> agentsByHostAddress = new SizeConcurrentHashMap<String, GridServiceAgent>();

    private final Map<String, GridServiceAgent> agentsByHostNames = new SizeConcurrentHashMap<String, GridServiceAgent>();

    private final InternalGridServiceAgentAddedEventManager gridServiceAgentAddedEventManager;

    private final InternalGridServiceAgentRemovedEventManager gridServiceAgentRemovedEventManager;
    
    private final InternalElasticGridServiceAgentProvisioningFailureEventManager elasticGridServiceAgentProvisioningFailureEventManager;
    
    private final InternalElasticGridServiceAgentProvisioningProgressChangedEventManager elasticGridServiceAgentProvisioningProgressChangedEventManager;

    public DefaultGridServiceAgents(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceAgentAddedEventManager = new DefaultGridServiceAgentAddedEventManager(this);
        this.gridServiceAgentRemovedEventManager = new DefaultGridServiceAgentRemovedEventManager(this);
        this.elasticGridServiceAgentProvisioningFailureEventManager = new DefaultElasticGridServiceAgentProvisioningFailureEventManager(admin);
        this.elasticGridServiceAgentProvisioningProgressChangedEventManager = new DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public GridServiceAgent[] getAgents() {
        return agents.values().toArray(new GridServiceAgent[0]);
    }

    public GridServiceAgent getAgentByUID(String uid) {
        return agents.get(uid);
    }

    public Map<String, GridServiceAgent> getUids() {
        return Collections.unmodifiableMap(agents);
    }

    public Map<String, GridServiceAgent> getHostAddress() {
        return Collections.unmodifiableMap(agentsByHostAddress);
    }

    public Map<String, GridServiceAgent> getHostNames() {
        return Collections.unmodifiableMap(agentsByHostNames);
    }

    public int getSize() {
        return agents.size();
    }

    public boolean isEmpty() {
        return agents.size() == 0;
    }

    public Iterator<GridServiceAgent> iterator() {
        return Collections.unmodifiableCollection(agents.values()).iterator();
    }

    public GridServiceAgent waitForAtLeastOne() {
        return waitForAtLeastOne(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GridServiceAgent waitForAtLeastOne(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceAgent> ref = new AtomicReference<GridServiceAgent>();
        GridServiceAgentAddedEventListener added = new GridServiceAgentAddedEventListener() {
            public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
                ref.set(gridServiceAgent);
                latch.countDown();
            }
        };
        getGridServiceAgentAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getGridServiceAgentAdded().remove(added);
        }
    }

    public boolean waitFor(int numberOfAgents) {
        return waitFor(numberOfAgents, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfAgents, long timeout, TimeUnit timeUnit) {
        if (numberOfAgents == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            GridServiceAgentRemovedEventListener removed = new GridServiceAgentRemovedEventListener() {
                public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
                    latch.countDown();
                }
            };
            getGridServiceAgentRemoved().remove(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceAgentRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfAgents);
            GridServiceAgentAddedEventListener added = new GridServiceAgentAddedEventListener() {
                public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
                    latch.countDown();
                }
            };
            getGridServiceAgentAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceAgentAdded().remove(added);
            }
        }
    }

    public GridServiceAgentAddedEventManager getGridServiceAgentAdded() {
        return this.gridServiceAgentAddedEventManager;
    }

    public GridServiceAgentRemovedEventManager getGridServiceAgentRemoved() {
        return this.gridServiceAgentRemovedEventManager;
    }

    public void addLifecycleListener(GridServiceAgentLifecycleEventListener eventListener) {
        gridServiceAgentAddedEventManager.add(eventListener);
        gridServiceAgentRemovedEventManager.add(eventListener);
    }

    public void removeLifecycleListener(GridServiceAgentLifecycleEventListener eventListener) {
        gridServiceAgentAddedEventManager.remove(eventListener);
        gridServiceAgentRemovedEventManager.remove(eventListener);
    }

    public void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent) {
        assertStateChangesPermitted();
        GridServiceAgent existing = agents.put(gridServiceAgent.getUid(), gridServiceAgent);
        agentsByHostAddress.put(gridServiceAgent.getTransport().getHostAddress(), gridServiceAgent);
        agentsByHostNames.put(gridServiceAgent.getTransport().getHostName(), gridServiceAgent);
        if (existing == null) {
            gridServiceAgentAddedEventManager.gridServiceAgentAdded(gridServiceAgent);
        }
    }

    public InternalGridServiceAgent removeGridServiceAgent(String uid) {
        assertStateChangesPermitted();
        InternalGridServiceAgent existing = (InternalGridServiceAgent) agents.remove(uid);
        if (existing != null) {
            agentsByHostAddress.remove(existing.getTransport().getHostAddress());
            agentsByHostAddress.remove(existing.getTransport().getHostName());
            gridServiceAgentRemovedEventManager.gridServiceAgentRemoved(existing);
        }
        return existing;
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (GridServiceAgent gsa : this) {
            dumpResult.add(gsa.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    @Override
    public ElasticGridServiceAgentProvisioningFailureEventManager getElasticGridServiceAgentProvisioningFailure() {
        return elasticGridServiceAgentProvisioningFailureEventManager;
    }

    @Override
    public ElasticGridServiceAgentProvisioningProgressChangedEventManager getElasticGridServiceAgentProvisioningProgressChanged() {
        return elasticGridServiceAgentProvisioningProgressChangedEventManager;
    }

    @Override
    public void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event) {
        if (event instanceof ElasticGridServiceAgentProvisioningFailureEvent) {
            elasticGridServiceAgentProvisioningFailureEventManager.elasticGridServiceAgentProvisioningFailure((ElasticGridServiceAgentProvisioningFailureEvent)event);
        }
        else if (event instanceof ElasticGridServiceAgentProvisioningProgressChangedEvent) {
            elasticGridServiceAgentProvisioningProgressChangedEventManager.elasticGridServiceAgentProvisioningProgressChanged((ElasticGridServiceAgentProvisioningProgressChangedEvent)event);
        }
    }

}
