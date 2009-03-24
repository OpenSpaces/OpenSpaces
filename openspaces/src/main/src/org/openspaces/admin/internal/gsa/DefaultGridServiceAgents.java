package org.openspaces.admin.internal.gsa;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventManager;
import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.DefaultGridServiceAgentAddedEventManager;
import org.openspaces.admin.internal.gsa.events.DefaultGridServiceAgentRemovedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalGridServiceAgentAddedEventManager;
import org.openspaces.admin.internal.gsa.events.InternalGridServiceAgentRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    public DefaultGridServiceAgents(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceAgentAddedEventManager = new DefaultGridServiceAgentAddedEventManager(this);
        this.gridServiceAgentRemovedEventManager = new DefaultGridServiceAgentRemovedEventManager(this);
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
        return agents.values().iterator();
    }

    public GridServiceAgent waitForAtLeastOne() {
        return waitForAtLeastOne(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
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
        return waitFor(numberOfAgents, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(int numberOfAgents, long timeout, TimeUnit timeUnit) {
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
        GridServiceAgent existing = agents.put(gridServiceAgent.getUid(), gridServiceAgent);
        agentsByHostAddress.put(gridServiceAgent.getTransport().getHostAddress(), gridServiceAgent);
        agentsByHostNames.put(gridServiceAgent.getTransport().getHostName(), gridServiceAgent);
        if (existing == null) {
            gridServiceAgentAddedEventManager.gridServiceAgentAdded(gridServiceAgent);
        }
    }

    public InternalGridServiceAgent removeGridServiceAgent(String uid) {
        InternalGridServiceAgent existing = (InternalGridServiceAgent) agents.remove(uid);
        if (existing != null) {
            agentsByHostAddress.remove(existing.getTransport().getHostAddress());
            agentsByHostAddress.remove(existing.getTransport().getHostName());
            gridServiceAgentRemovedEventManager.gridServiceAgentRemoved(existing);
        }
        return existing;
    }
}
