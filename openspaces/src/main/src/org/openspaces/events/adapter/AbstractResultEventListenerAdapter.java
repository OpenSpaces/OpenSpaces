package org.openspaces.events.adapter;

import com.j_spaces.core.client.UpdateModifiers;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.events.SpaceDataEventListener;

/**
 * <p>A base class event listener allowing for event listneres result handling by writing it
 * back to the space. Subclasses should implement
 * {@link #onEventWithResult(Object,org.openspaces.core.GigaSpace,Object)} with the result
 * being writting to back to the space. The write lease can be controlled using {@link #setWriteLease(long)}.
 *
 * @author kimchy
 */
//TODO support result of an array type by using writeMultiple
public abstract class AbstractResultEventListenerAdapter implements SpaceDataEventListener {

    private long writeLease = Lease.FOREVER;

    private boolean updateOrWrite = false;

    private long updateTimeout = JavaSpace.NO_WAIT;

    /**
     * The lease time the result will be written under (in milliseconds). Defaults to
     * {@link net.jini.core.lease.Lease#FOREVER}.
     *
     * @param writeLease The lease time the result will be written under
     */
    public void setWriteLease(long writeLease) {
        this.writeLease = writeLease;
    }

    /**
     * Sets if the write operation will perform an update in case the entry result already exists
     * in the space. Default to <code>false</code>.
     */
    public void setUpdateOrWrite(boolean updateOrWrite) {
        this.updateOrWrite = updateOrWrite;
    }

    /**
     * Sets the update timeout in case the flag {@link #setUpdateOrWrite(boolean)} is set to
     * <code>true</code>.
     */
    public void setUpdateTimeout(long updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

    /**
     * Implements the {@link org.openspaces.events.SpaceDataEventListener} by delegating to
     * {@link #onEventWithResult(Object,org.openspaces.core.GigaSpace,Object)} and writing
     * the result back to the space (if it is not <code>null</code>) using
     * {@link #handleResult(Object,org.openspaces.core.GigaSpace)}.
     */
    public void onEvent(Object data, GigaSpace gigaSpace, Object source) {
        Object result = onEventWithResult(data, gigaSpace, source);
        handleResult(result, gigaSpace);
    }

    /**
     * Writes the result back to the space (if not <code>null</code>) under the configured
     * write lease. Allows to be overriden in order to implement more advance result handling.
     *
     * @param result    The result to write back to the space
     * @param gigaSpace The GigaSpace instance to operate against the space
     */
    protected void handleResult(Object result, GigaSpace gigaSpace) throws GigaSpaceException {
        if (result != null) {
            if (updateOrWrite) {
                gigaSpace.write(result, writeLease, updateTimeout, UpdateModifiers.UPDATE_OR_WRITE);
            } else {
                gigaSpace.write(result, writeLease, updateTimeout, UpdateModifiers.WRITE_ONLY);
            }
        }
    }

    /**
     * An event listner callback allowing to return a result that will be written back to the
     * space.
     *
     * @param data      The event data object
     * @param gigaSpace A GigaSpace instance that can be used to perofrm additional operations against the space
     * @param source    Optional additional data or the actual source event data object (where relevant)
     * @return A result object that will be written back to the space
     */
    protected abstract Object onEventWithResult(Object data, GigaSpace gigaSpace, Object source);
}
