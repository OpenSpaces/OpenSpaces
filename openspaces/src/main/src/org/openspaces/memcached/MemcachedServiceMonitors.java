package org.openspaces.memcached;

import org.openspaces.pu.service.PlainServiceMonitors;

/**
 * @author kimchy (shay.banon)
 */
public class MemcachedServiceMonitors extends PlainServiceMonitors {

    public static class Attributes {
        public static final String GET_CMDS = "get-cmds";
        public static final String SET_CMDS = "set-cmds";
        public static final String GET_HITS = "get-hits";
        public static final String GET_MISSES = "get-misses";
    }

    public MemcachedServiceMonitors() {
        super();
    }

    public MemcachedServiceMonitors(String id, long getCmds, long setCmds, long getHits, long getMisses) {
        super(id);
        getMonitors().put(Attributes.GET_CMDS, getCmds);
        getMonitors().put(Attributes.SET_CMDS, setCmds);
        getMonitors().put(Attributes.GET_HITS, getHits);
        getMonitors().put(Attributes.GET_MISSES, getMisses);
    }

    public long getGetCmds() {
        return (Long) getMonitors().get(Attributes.GET_CMDS);
    }

    public long getSetCmds() {
        return (Long) getMonitors().get(Attributes.SET_CMDS);
    }

    public long getGetHits() {
        return (Long) getMonitors().get(Attributes.GET_HITS);
    }

    public long getGetMisses() {
        return (Long) getMonitors().get(Attributes.GET_MISSES);
    }
}
