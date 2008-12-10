package org.openspaces.admin.gsa;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventManager;
import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface GridServiceAgents extends AdminAware, Iterable<GridServiceAgent> {

    GridServiceAgent[] getAgents();

    GridServiceAgent getAgentByUID(String uid);

    Map<String, GridServiceAgent> getUids();

    int getSize();

    boolean isEmpty();

    GridServiceAgent waitForAtLeastOne();

    GridServiceAgent waitForAtLeastOne(long timeout, TimeUnit timeUnit);

    boolean waitFor(int numberOfAgents);

    boolean waitFor(int numberOfAgents, long timeout, TimeUnit timeUnit);

    GridServiceAgentAddedEventManager getGridServiceAgentAdded();

    GridServiceAgentRemovedEventManager getGridServiceAgentRemoved();

    void addLifecycleListener(GridServiceAgentLifecycleEventListener eventListener);

    void removeLifecycleListener(GridServiceAgentLifecycleEventListener eventListener);
}
