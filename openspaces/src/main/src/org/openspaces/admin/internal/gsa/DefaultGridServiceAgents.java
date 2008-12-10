package org.openspaces.admin.internal.gsa;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgents implements InternalGridServiceAgents {

    private final InternalAdmin admin;

    private final Map<String, GridServiceAgent> agents = new SizeConcurrentHashMap<String, GridServiceAgent>();

    public DefaultGridServiceAgents(InternalAdmin admin) {
        this.admin = admin;
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

    public int getSize() {
        return agents.size();
    }

    public boolean isEmpty() {
        return agents.size() == 0;
    }

    public Iterator<GridServiceAgent> iterator() {
        return agents.values().iterator();
    }

    public void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent) {
        GridServiceAgent existing = agents.put(gridServiceAgent.getUid(), gridServiceAgent);
        if (existing == null) {

        }
    }

    public InternalGridServiceAgent removeGridServiceAgent(String uid) {
        InternalGridServiceAgent existing = (InternalGridServiceAgent) agents.remove(uid);
        if (existing != null) {

        }
        return existing;
    }
}
