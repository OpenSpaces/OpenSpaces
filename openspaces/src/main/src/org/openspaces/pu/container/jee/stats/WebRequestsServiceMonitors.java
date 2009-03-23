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
public class WebRequestsServiceMonitors extends PlainServiceMonitors {

    public static class Attributes {
        public static final String TOTAL = "total";
        public static final String ACTIVE = "active";
        public static final String TOTAL_DURATION = "duration-total";
    }

    public WebRequestsServiceMonitors() {
    }

    public WebRequestsServiceMonitors(String id, long requests, long requestsActive, long requestsDurationTotal) {
        super(id);
        getMonitors().put(Attributes.TOTAL, requests);
        getMonitors().put(Attributes.ACTIVE, requestsActive);
        getMonitors().put(Attributes.TOTAL_DURATION, requestsDurationTotal);
    }

    /**
     * Returns the total number of requests processed.
     */
    public long getTotal() {
        return (Long) getMonitors().get(Attributes.TOTAL);
    }

    /**
     * Returns the current number of requests that are active.
     */
    public long getActive() {
        return (Long) getMonitors().get(Attributes.ACTIVE);
    }

    /**
     * Returns the total time (in milliseconds) that it took to process requests.
     */
    public long getTotalDuration() {
        return (Long) getMonitors().get(Attributes.TOTAL_DURATION);
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
