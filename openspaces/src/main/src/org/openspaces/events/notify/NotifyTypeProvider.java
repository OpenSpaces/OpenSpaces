package org.openspaces.events.notify;

import org.openspaces.events.SpaceDataEventListener;

/**
 * An extension of space event listener allowing the listener to control
 * programmatically (without the user having to configure it withing the
 * notify container) which notifications this listener will be invoked on.
 *
 * <p>All markers return <code>Boolean</code> value. <code>null</code> means
 * that it will have no affect on the flag appropiate flag.
 *
 * <p>Note, all flags will only take place if it is not overriden by the user
 * when configuring the notify container. If the user has set, for example, the
 * write notify flag, then this provider {@link #isWrite()} will not be taken
 * into account. This allows for advance users to futher configure a "recommended"
 * notifucation types for a specific listener that implements this interface.
 *
 * @author kimchy
 */
public interface NotifyTypeProvider extends SpaceDataEventListener {

    /**
     * Should this listener be notified on write operations.
     * <code>null</code> will leave the flag un changed.
     */
    Boolean isWrite();

    /**
     * Should this listener be notified on update operations.
     * <code>null</code> will leave the flag un changed.
     */
    Boolean isUpdate();

    /**
     * Should this listener be notified on lease expiration operations.
     * <code>null</code> will leave the flag un changed.
     */
    Boolean isLeaseExpire();

    /**
     * Should this listener be notified on take operations.
     * <code>null</code> will leave the flag un changed.
     */
    Boolean isTake();
}
