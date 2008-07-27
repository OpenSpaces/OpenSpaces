/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.jee.sessions.jetty;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.j_spaces.core.IJSpace;
import com.j_spaces.kernel.ConcurrentHashSet;
import net.jini.core.lease.Lease;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;
import java.util.Set;

/**
 * GigaspacesSessionIdManager
 *
 * A Jetty SessionIDManager where the in-use session ids are stored
 * in a data grid "cloud".
 */
public class GigaSessionIdManager extends AbstractSessionIdManager {

    private final Set<Id> sessionIds = new ConcurrentHashSet<Id>();

    protected String spaceUrl;

    protected IJSpace space;

    private long lease = Lease.FOREVER;

    public GigaSessionIdManager(Server server) {
        super(server);
    }

    public GigaSessionIdManager(Server server, Random random) {
        super(server, random);
    }

    public void setSpaceUrl(String url) {
        spaceUrl = url;
    }

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public IJSpace getSpace() {
        return space;
    }

    /**
     * Sets the lease in seconds of Session Ids written to the Space.
     */
    public void setLease(long lease) {
        this.lease = lease * 1000;
    }

    public void addSession(HttpSession session) {
        if (session == null)
            return;

        if (session instanceof GigaSessionManager.Session) {
            String id = ((GigaSessionManager.Session) session).getClusterId();
            try {
                Id theId = new Id(id);
                add(theId);
                sessionIds.add(theId);
                if (Log.isDebugEnabled()) Log.debug("Added id " + id);
            } catch (Exception e) {
                Log.warn("Problem storing session id=" + id, e);
            }
        } else
            throw new IllegalStateException("Session is not a Gigaspaces session");
    }

    public String getClusterId(String nodeId) {
        int dot = nodeId.lastIndexOf('.');
        return (dot > 0) ? nodeId.substring(0, dot) : nodeId;
    }

    public String getNodeId(String clusterId, HttpServletRequest request) {
        if (_workerName != null)
            return clusterId + '.' + _workerName;

        return clusterId;
    }

    public boolean idInUse(String id) {
        if (id == null)
            return false;

        String clusterId = getClusterId(id);
        Id theId = new Id(clusterId);
        if (sessionIds.contains(theId))
            return true; //optimisation - if this session is one we've been managing, we can check locally

        //otherwise, we need to go to the space to check
        try {
            return exists(theId);
        } catch (Exception e) {
            Log.warn("Problem checking inUse for id=" + clusterId, e);
            return false;
        }
    }

    public void invalidateAll(String id) {
        //take the id out of the list of known sessionids for this node
        removeSession(id);

        //tell all contexts that may have a session object with this id to
        //get rid of them
        Handler[] contexts = _server.getChildHandlersByClass(WebAppContext.class);
        for (int i = 0; contexts != null && i < contexts.length; i++) {
            AbstractSessionManager manager = ((AbstractSessionManager) ((WebAppContext) contexts[i]).getSessionHandler().getSessionManager());
            if (manager instanceof GigaSessionManager) {
                ((GigaSessionManager) manager).invalidateSession(id);
            }
        }
    }

    public void removeSession(HttpSession session) {
        if (session == null)
            return;

        removeSession(((GigaSessionManager.Session) session).getClusterId());
    }

    public void removeSession(String id) {

        if (id == null)
            return;

        if (Log.isDebugEnabled()) Log.debug("Removing session id=" + id);
        try {
            Id theId = new Id(id);
            sessionIds.remove(theId);
            delete(theId);
        } catch (Exception e) {
            Log.warn("Problem removing session id=" + id, e);
        }
    }


    /**
     * Start up the id manager.
     */
    public void doStart() {
        try {
            if (space == null) {
                initSpace();
            }
            super.doStart();
        } catch (Exception e) {
            Log.warn("Problem initialising session ids", e);
            throw new IllegalStateException(e);
        }
    }

    protected void initSpace()
            throws Exception {
        if (space != null) {
            return;
        }
        if (spaceUrl == null) {
            throw new IllegalStateException("No url for space and actual instnace not injected");
        }
        space = JettySpaceLocator.locate(spaceUrl);
    }

    protected void add(Id id) throws Exception {
        space.write(id, null, lease);
    }

    protected void delete(Id id) throws Exception {
        space.take(id, null, 0);
        if (Log.isDebugEnabled()) Log.debug("Deleted id from space: id=" + id);
    }

    protected boolean exists(Id id) throws Exception {
        Id idFromSpace = (Id) space.read(id, null, 0);
        if (Log.isDebugEnabled()) Log.debug("Id [" + id + "] " + (idFromSpace == null ? "does not exist" : "exists") + " in space");
        return idFromSpace != null;
    }

    /**
     * Id
     *
     * Class to hold the session id. This is really only needed
     * for gigaspaces so that we can annotate routing information etc.
     */
    @SpaceClass(persist = false)
    public static class Id implements Externalizable {

        private String id;

        public Id() {
        }

        public Id(String id) {
            this.id = id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @SpaceId(autoGenerate = false)
        @SpaceRouting
        public String getId() {
            return this.id;
        }

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            Id targetId = (Id) o;
            return targetId.getId().equals(id);

        }

        public String toString() {
            return "SessionID [" + id + "]";
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(id);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readUTF();
        }
    }
}
