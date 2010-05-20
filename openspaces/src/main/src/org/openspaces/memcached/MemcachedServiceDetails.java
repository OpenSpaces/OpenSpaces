package org.openspaces.memcached;

import org.openspaces.pu.service.PlainServiceDetails;

/**
 * @author kimchy (shay.banon)
 */
public class MemcachedServiceDetails extends PlainServiceDetails {

    public static final String SERVICE_TYPE = "memcached";

    public static class Attributes {
        public static final String SPACE = "space";
        public static final String PORT = "template";
    }

    public MemcachedServiceDetails() {
    }

    public MemcachedServiceDetails(String id, String space, int port) {
        super(id, "memcached", "memcached", "Memcached", "Memcached");
        getAttributes().put(Attributes.SPACE, space);
        getAttributes().put(Attributes.PORT, port);
    }

    public String getSpace() {
        return (String) getAttributes().get(Attributes.SPACE);
    }

    public int getPort() {
        return (Integer) getAttributes().get(Attributes.PORT);
    }
}
