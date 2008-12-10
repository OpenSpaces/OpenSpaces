package org.openspaces.admin.gsa.events;

/**
 * @author kimchy
 */
public interface GridServiceAgentAddedEventManager {

    void add(GridServiceAgentAddedEventListener eventListener);

    void remove(GridServiceAgentAddedEventListener eventListener);
}