package org.openspaces.admin.gsa;

import org.openspaces.admin.AdminAware;

import java.util.Map;

/**
 * @author kimchy
 */
public interface GridServiceAgents extends AdminAware, Iterable<GridServiceAgent> {

    GridServiceAgent[] getAgents();

    GridServiceAgent getAgentByUID(String uid);

    Map<String, GridServiceAgent> getUids();

    int getSize();

    boolean isEmpty();
}
