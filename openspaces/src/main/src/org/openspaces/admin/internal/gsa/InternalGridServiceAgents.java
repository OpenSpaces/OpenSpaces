package org.openspaces.admin.internal.gsa;

import org.openspaces.admin.gsa.GridServiceAgents;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgents extends GridServiceAgents {

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent);

    InternalGridServiceAgent removeGridServiceAgent(String uid);
}