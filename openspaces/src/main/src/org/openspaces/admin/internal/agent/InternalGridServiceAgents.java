package org.openspaces.admin.internal.agent;

import org.openspaces.admin.agent.GridServiceAgents;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgents extends GridServiceAgents {

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent);

    InternalGridServiceAgent removeGridServiceAgent(String uid);
}