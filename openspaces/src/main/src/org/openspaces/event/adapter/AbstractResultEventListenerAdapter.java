package org.openspaces.event.adapter;

import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpace;
import org.openspaces.event.SpaceDataEventListener;

/**
 * @author kimchy
 */
public abstract class AbstractResultEventListenerAdapter implements SpaceDataEventListener {

    private long writeLease = Lease.FOREVER;

    public void setWriteLease(long writeLease) {
        this.writeLease = writeLease;
    }

    public void onEvent(Object data, GigaSpace gigaSpace, Object source) {
        Object result = onEventWithResult(data, gigaSpace, source);
        if (result != null) {
            gigaSpace.write(result, writeLease);
        }
    }

    protected abstract Object onEventWithResult(Object data, GigaSpace gigaSpace, Object source);
}
