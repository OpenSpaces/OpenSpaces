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

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SQLQuery;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledFuture;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import net.jini.core.lease.Lease;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * GigaspacesSessionManager
 *
 * A Jetty SessionManager where the session data is stored in a
 * data grid "cloud".
 *
 * On each request, the session data is looked up in the "cloud"
 * and brought into the local space cache if doesn't already exist,
 * and an entry put into the managers map of sessions. When the request
 * exists, any changes, including changes to the access time of the session
 * are written back out to the grid.
 */
public class GigaSessionManager extends org.mortbay.jetty.servlet.AbstractSessionManager {

    private IJSpace space;

    private String spaceUrl;

    private long lease = Lease.FOREVER;

    protected int _scavengePeriodMs = 1000 * 60 * 20; //20mins

    protected int _scavengeCount = 0;

    protected int _savePeriodMs = 60 * 1000; //60 sec

    protected SQLQuery<SessionData> _query;


    private volatile static ScheduledExecutorService executorService;

    private volatile static int totalNumberOfScavangers = 0;

    // not static, we want to schedule one for each instnace of the session manager
    private volatile ScheduledFuture scavengerFuture;

    private static final Object executorMonitor = new Object();

    private volatile int lastSessionCount = -1;

    private volatile long lastCountSessionsTime = System.currentTimeMillis();

    private int countSessionPeriod = 1000 * 60 * 5; // every 5 minutes do the sessions count

    /**
     * Start the session manager.
     *
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#doStart()
     */
    public void doStart() throws Exception {
        if (space == null) {
            if (spaceUrl == null)
                throw new IllegalStateException("No url for space");
            space = JettySpaceLocator.locate(spaceUrl);
        }

        _query = new SQLQuery<SessionData>(SessionData.class, "expiryTime < ?");

        if (_sessionIdManager == null) {
            _sessionIdManager = new GigaSessionIdManager(getSessionHandler().getServer());
            ((GigaSessionIdManager) _sessionIdManager).setSpace(space);
        }
        if (_sessionIdManager instanceof GigaSessionIdManager) {
            if (((GigaSessionIdManager) _sessionIdManager).getSpace() == null) {
                ((GigaSessionIdManager) _sessionIdManager).setSpace(space);
            }
        }

        super.doStart();

        synchronized (executorMonitor) {
            if (totalNumberOfScavangers == 0) {
                if (Log.isDebugEnabled()) Log.debug("Starting scavenger with period [" + _scavengePeriodMs + "ms]");
                executorService = Executors.newScheduledThreadPool(1);
            }
            totalNumberOfScavangers++;
            scavengerFuture = executorService.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    scavenge();
                }
            }, _scavengePeriodMs, _scavengePeriodMs, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * Stop the session manager.
     *
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#doStop()
     */
    public void doStop() throws Exception {
        synchronized (executorMonitor) {
            scavengerFuture.cancel(true);
            if (--totalNumberOfScavangers == 0) {
                if (Log.isDebugEnabled()) Log.debug("Stopping scavenger");
                executorService.shutdown();
            }
        }
        space = null;
        super.doStop();
    }

    public int getSavePeriod() {
        return _savePeriodMs / 1000;
    }

    public void setSavePeriod(int seconds) {
        if (seconds <= 0)
            seconds = 60;

        _savePeriodMs = seconds * 1000;
    }


    public int getScavengePeriod() {
        return _scavengePeriodMs / 1000;
    }

    public void setScavengePeriod(int seconds) {
        if (seconds <= 0)
            seconds = 60;
    }

    public void setCountSessionPeriod(int seconds) {
        this.countSessionPeriod = seconds * 1000;
    }

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public IJSpace getSpace() {
        return space;
    }

    public void setSpaceUrl(String url) {
        spaceUrl = url;
    }

    public String getSpaceUrl() {
        return spaceUrl;
    }

    /**
     * Sets the lease (in seconds) of sessions written to the Space.
     */
    public void setLease(long lease) {
        this.lease = lease * 1000;
    }

    /**
     * Get a session matching the id.
     *
     * Look in the grid to see if such a session exists, as it may have moved from
     * another node.
     *
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#getSession(java.lang.String)
     */
    public Session getSession(String idInCluster) {
        // TODO do we really need to syncronize on (this) here? It used to be like that
        try {
            SessionData template = new SessionData();
            template.setId(idInCluster);
            SessionData data = fetch(template);

            Session session = null;

            if (data == null) {
                //No session in cloud with matching id and context path.
                session = null;
                if (Log.isDebugEnabled()) Log.debug("No session matching id [" + idInCluster + "]");
            } else {
                session = new Session(data);
                if (Log.isDebugEnabled()) Log.debug("Found matching session [" + idInCluster + "]");
            }
            return session;
        } catch (Exception e) {
            Log.warn("Unable to load session from database", e);
            return null;
        }
    }


    public Map getSessionMap() {
        // TODO we might want to read some sessions and give it back...
        return new HashMap();
    }

    public int getSessions() {
        if (lastSessionCount == -1 || (System.currentTimeMillis() - lastCountSessionsTime) > countSessionPeriod) {
            try {
                lastSessionCount = space.count(new SessionData(), null);
            } catch (Exception e) {
                Log.warn("Failed to execute count of sessions", e);
            }
        }
        return lastSessionCount;
    }

    public void resetStats() {
        lastSessionCount = -1;
        super.resetStats();
    }

    protected void invalidateSessions() {
        //Do nothing - we don't want to remove and
        //invalidate all the sessions because this
        //method is called from doStop(), and just
        //because this context is stopping does not
        //mean that we should remove the session from
        //any other nodes
    }

    protected Session newSession(HttpServletRequest request) {
        return new Session(request);
    }

    protected void removeSession(String idInCluster) {
        try {
            delete(idInCluster);
        } catch (Exception e) {
            Log.warn("Failed to remove session with id [" + idInCluster + "]", e);
        }
    }


    public void removeSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session abstractSession, boolean invalidate) {
        if (!(abstractSession instanceof GigaSessionManager.Session))
            throw new IllegalStateException("Session is not a GigaspacesSessionManager.Session " + abstractSession);

        GigaSessionManager.Session session = (GigaSessionManager.Session) abstractSession;

        //TODO there was synctonize on both sessionIdManager and this here, do we really need it?

        boolean removed = false;
        try {
            removed = delete(getClusterId(session));
        } catch (Exception e) {
            Log.warn("Faield to remove session [" + getClusterId(session) + "]", e);
        }
        if (removed) {
            _sessionIdManager.removeSession(session);
            if (invalidate)
                _sessionIdManager.invalidateAll(getClusterId(session));
        }

        if (invalidate && _sessionListeners != null) {
            HttpSessionEvent event = new HttpSessionEvent(session);
            for (int i = LazyList.size(_sessionListeners); i-- > 0;)
                ((HttpSessionListener) LazyList.get(_sessionListeners, i)).sessionDestroyed(event);
        }
        if (!invalidate) {
            session.willPassivate();
        }
    }


    public void invalidateSession(String idInCluster) {
        Session session = getSession(idInCluster);
        if (session != null) {
            session.invalidate();
        }
    }

    protected void addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session abstractSession) {
        if (abstractSession == null)
            return;

        if (!(abstractSession instanceof GigaSessionManager.Session))
            throw new IllegalStateException("Not a GigaspacesSessionManager.Session " + abstractSession);

        synchronized (this) {
            GigaSessionManager.Session session = (GigaSessionManager.Session) abstractSession;

            try {
                add(session._data);
            } catch (Exception e) {
                Log.warn("Problem writing new SessionData to space ", e);
            }
        }
    }


    /**
     * Look for expired sessions that we know about in our
     * session map, and double check with the grid that
     * it has really expired, or already been removed.
     */
    protected void scavenge() {
        //don't attempt to scavenge if we are shutting down
        if (isStopping() || isStopped())
            return;

        Thread thread = Thread.currentThread();
        ClassLoader origClassLoader = thread.getContextClassLoader();
        _scavengeCount++;

        try {
            if (_loader != null)
                thread.setContextClassLoader(_loader);
            long now = System.currentTimeMillis();
            if (Log.isDebugEnabled())
                Log.debug("Scavenging old sessions, expiring before: " + (now - _scavengePeriodMs));
            Object[] expiredSessions;
            do {
                expiredSessions = findExpiredSessions((now - _scavengePeriodMs));
                for (int i = 0; i < expiredSessions.length; i++) {
                    if (Log.isDebugEnabled()) Log.debug("Timing out expired sesson " + expiredSessions[i]);
                    GigaSessionManager.Session expiredSession = new GigaSessionManager.Session((SessionData) expiredSessions[i]);
                    expiredSession.timeout();
                    if (Log.isDebugEnabled()) Log.debug("Expiring old session " + expiredSession._data);
                }
            } while (expiredSessions.length > 0);

            // force a count
            lastSessionCount = -1;
            int count = getSessions();
            if (count < this._minSessions) {
                this._minSessions = count;
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath)
                throw ((ThreadDeath) t);
            else
                Log.warn("Problem scavenging sessions", t);
        } finally {
            thread.setContextClassLoader(origClassLoader);
        }
    }


    protected void add(SessionData data) throws Exception {
        space.write(data, null, lease);
    }

    protected boolean delete(String id) throws Exception {
        SessionData sd = new SessionData();
        sd.setId(id);
        return space.take(sd, null, 0) != null;
    }

    protected void update(SessionData data) throws Exception {
        space.write(data, null, lease);
        if (Log.isDebugEnabled()) Log.debug("Wrote session " + data.toStringExtended());
    }

    protected SessionData fetch(SessionData template) throws Exception {
        return (SessionData) space.read(template, null, 0);
    }

    protected Object[] findExpiredSessions(long timestamp) throws Exception {
        _query.setParameter(1, timestamp);
        return space.takeMultiple(_query, null, 100);
    }

    /**
     * Session
     *
     * A session in memory of a Context. Adds behaviour around SessionData.
     */
    public class Session extends org.mortbay.jetty.servlet.AbstractSessionManager.Session {
        private SessionData _data;

        private volatile boolean _dirty = false;

        /**
         * Session from a request.
         */
        protected Session(HttpServletRequest request) {
            super(request);
            _data = new SessionData(_clusterId);
            _data.setMaxIdleMs(_dftMaxIdleSecs * 1000);
            _data.setExpiryTime(_maxIdleMs < 0 ? 0 : (System.currentTimeMillis() + _maxIdleMs));
            _data.setCookieSet(0);
            if (_data.getAttributeMap() == null)
                newAttributeMap();
            _values = _data.getAttributeMap();
            if (Log.isDebugEnabled()) Log.debug("New Session from request, " + _data.toStringExtended());
        }

        protected Session(SessionData data) {
            super(data.getCreated(), data.getId());
            _data = data;
            _values = data.getAttributeMap();
            if (Log.isDebugEnabled()) Log.debug("New Session from existing session data " + _data.toStringExtended());
        }

        protected void cookieSet() {
            _data.setCookieSet(_data.getAccessed());
        }

        protected Map newAttributeMap() {
            if (_data.getAttributeMap() == null) {
                _data.setAttributeMap(new ConcurrentHashMap());
            }
            return _data.getAttributeMap();
        }

        public void setAttribute(String name, Object value) {
            super.setAttribute(name, value);
            _dirty = true;
        }

        public void removeAttribute(String name) {
            super.removeAttribute(name);
            _dirty = true;
        }

        /**
         * Entry to session.
         * Called by SessionHandler on inbound request and the session already exists in this node's memory.
         *
         * @see org.mortbay.jetty.servlet.AbstractSessionManager.Session#access(long)
         */
        protected void access(long time) {
            super.access(time);
            _data.setLastAccessed(_data.getAccessed());
            _data.setAccessed(time);
            _data.setExpiryTime(_maxIdleMs < 0 ? 0 : (time + _maxIdleMs));
        }

        /**
         * Exit from session
         *
         * If the session attributes changed then always write the session
         * to the cloud.
         *
         * If just the session access time changed, we don't always write out the
         * session, because the gigaspace will serialize the unchanged sesssion
         * attributes. To save on serialization overheads, we only write out the
         * session when only the access time has changed if the time at which we
         * last saved the session exceeds the chosen save interval.
         *
         * @see org.mortbay.jetty.servlet.AbstractSessionManager.Session#complete()
         */
        protected void complete() {
            super.complete();
            try {
                if (_dirty || (_data._accessed - _data._lastSaved) >= (_savePeriodMs)) {
                    _data.setLastSaved(System.currentTimeMillis());
                    willPassivate();
                    update(_data);
                    didActivate();
                    if (Log.isDebugEnabled())
                        Log.debug("Dirty=" + _dirty + ", accessed-saved=" + _data._accessed + "-" + _data._lastSaved + ", savePeriodMs=" + _savePeriodMs);
                }
            }
            catch (Exception e) {
                Log.warn("Problem persisting changed session data id=" + getId(), e);
            }
            finally {
                _dirty = false;
            }
        }

        protected void timeout() throws IllegalStateException {
            if (Log.isDebugEnabled()) Log.debug("Timing out session id=" + getClusterId());
            super.timeout();
        }

        protected void willPassivate() {
            super.willPassivate();
        }

        protected void didActivate() {
            super.didActivate();
        }

        public String getClusterId() {
            return super.getClusterId();
        }

        public String getNodeId() {
            return super.getNodeId();
        }
    }

    /**
     * SessionData
     *
     * Data about a session.
     *
     * NOTE: we let gigaspaces assign a globally unique identifier for
     * a SessionData object, although we could compose our own, based on:
     * canonicalized(contextPath) + virtualhost[0] + sessionid
     */
    public static class SessionData implements Externalizable {
        private String _id;
        private long _accessed = -1;
        private volatile long _lastAccessed = -1;
        private volatile long _lastSaved = -1;
        private long _maxIdleMs = -1;
        private volatile long _cookieSet = -1;
        private long _created = -1;
        private volatile long _expiryTime = -1;
        private volatile ConcurrentHashMap _attributes = null;


        public SessionData() {
        }

        public SessionData(String sessionId) {
            _id = sessionId;
            _created = System.currentTimeMillis();
            _accessed = _created;
            _lastAccessed = 0;
            _lastSaved = 0;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            // TODO create a null bitmap (-1 is null for longs)
            if (_id == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(_id);
            }
            out.writeLong(_accessed);
            out.writeLong(_lastAccessed);
            out.writeLong(_lastSaved);
            out.writeLong(_maxIdleMs);
            out.writeLong(_cookieSet);
            out.writeLong(_created);
            out.writeLong(_expiryTime);
            if (_attributes == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(_attributes.size());
                for (Iterator it = _attributes.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    out.writeObject(entry.getKey());
                    out.writeObject(entry.getValue());
                }
            }
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            if (in.readBoolean()) {
                _id = in.readUTF();
            }
            _accessed = in.readLong();
            _lastAccessed = in.readLong();
            _lastSaved = in.readLong();
            _maxIdleMs = in.readLong();
            _cookieSet = in.readLong();
            _created = in.readLong();
            _expiryTime = in.readLong();
            int size = in.readInt();
            if (size > -1) {
                _attributes = new ConcurrentHashMap();
                for (int i = 0; i < size; i++) {
                    _attributes.put(in.readObject(), in.readObject());
                }
            }
        }

        @SpaceId(autoGenerate = false)
        @SpaceRouting
        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        @SpaceProperty(nullValue = "-1")
        public long getCreated() {
            return _created;
        }

        public void setCreated(long ms) {
            _created = ms;
        }

        @SpaceProperty(nullValue = "-1")
        public long getAccessed() {
            return _accessed;
        }

        public void setAccessed(long ms) {
            _accessed = ms;
        }

        public void setLastSaved(long ms) {
            _lastSaved = ms;
        }

        @SpaceProperty(nullValue = "-1")
        public long getLastSaved() {
            return _lastSaved;
        }

        public void setMaxIdleMs(long ms) {
            _maxIdleMs = ms;
        }

        @SpaceProperty(nullValue = "-1")
        public long getMaxIdleMs() {
            return _maxIdleMs;
        }

        public void setLastAccessed(long ms) {
            _lastAccessed = ms;
        }

        @SpaceProperty(nullValue = "-1")
        public long getLastAccessed() {
            return _lastAccessed;
        }

        public void setCookieSet(long ms) {
            _cookieSet = ms;
        }

        @SpaceProperty(nullValue = "-1")
        public long getCookieSet() {
            return _cookieSet;
        }

        @SpaceProperty
        protected ConcurrentHashMap getAttributeMap() {
            return _attributes;
        }

        protected void setAttributeMap(ConcurrentHashMap map) {
            _attributes = map;
        }

        public void setExpiryTime(long time) {
            _expiryTime = time;
        }

        @SpaceProperty(nullValue = "-1")
        public long getExpiryTime() {
            return _expiryTime;
        }

        public String toString() {
            return "Session id=" + _id +
                    ",created=" + _created + ",accessed=" + _accessed +
                    ",lastAccessed=" + _lastAccessed +
                    ",cookieSet=" + _cookieSet +
                    ",expiryTime=" + _expiryTime;
        }

        public String toStringExtended() {
            return toString() + "values=" + _attributes;
        }
    }
}
