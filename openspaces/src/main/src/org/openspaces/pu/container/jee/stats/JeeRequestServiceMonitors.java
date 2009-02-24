package org.openspaces.pu.container.jee.stats;

import org.openspaces.pu.service.PlainServiceMonitors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Statistics monitor information for JEE servlet requests.
 *
 * @author kimchy
 */
public class JeeRequestServiceMonitors extends PlainServiceMonitors {

    public static class Attributes {
        public static final String REQUESTS = "requests";
        public static final String REQUESTS_ACTIVE = "requests-active";
        public static final String REQUESTS_DURATION_TOTAL = "requests-duration-total";
    }

    public JeeRequestServiceMonitors() {
    }

    public JeeRequestServiceMonitors(String id, long requests, long requestsActive, long requestsDurationTotal) {
        super(id);
        getMonitors().put(Attributes.REQUESTS, requests);
        getMonitors().put(Attributes.REQUESTS_ACTIVE, requestsActive);
        getMonitors().put(Attributes.REQUESTS_DURATION_TOTAL, requestsDurationTotal);
    }

    /**
     * Returns the total number of requests processed.
     */
    public long getRequests() {
        return (Long) getMonitors().get(Attributes.REQUESTS);
    }

    /**
     * Returns the current number of requests that are active.
     */
    public long getRequestsActive() {
        return (Long) getMonitors().get(Attributes.REQUESTS_ACTIVE);
    }

    /**
     * Returns the total time (in milliseconds) that it took to process requests.
     */
    public long getRequestsDurationTotal() {
        return (Long) getMonitors().get(Attributes.REQUESTS_DURATION_TOTAL);
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
