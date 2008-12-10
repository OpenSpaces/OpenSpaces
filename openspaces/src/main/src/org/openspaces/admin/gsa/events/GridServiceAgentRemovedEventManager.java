package org.openspaces.admin.gsa.events;

/**
 * @author kimchy
 */
public interface GridServiceAgentRemovedEventManager {

    void add(GridServiceAgentRemovedEventListener eventListener);

    void remove(GridServiceAgentRemovedEventListener eventListener);

}