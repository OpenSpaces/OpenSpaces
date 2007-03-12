package org.openspaces.events.polling.trigger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceException;

/**
 * <p>Allows to perform a trigger receive operation which control if the active receive operation
 * will be perfomed in a polling event container. This feature is mainly used when having polling
 * event operations with transactions where the trigger receive operation is perfomed outside of
 * a transaction thus reducing the creation of transactions did not perform the actual receive
 * operation.
 *
 * <p>If the {@link #triggerReceive(Object,org.openspaces.core.GigaSpace,long)} returns a non
 * <code>null</code> value, it means that the receive operation should take place. If it returns
 * a <code>null</code> value, no receive operation will be attempted.
 *
 * <p>A trigger operation handler can also control if the object returned from
 * {@link #triggerReceive(Object,org.openspaces.core.GigaSpace,long)} will be used as the
 * template for the receive operation by returnning <code>true</code> in
 * {@link #isUseTriggerAsTemplate()}. If <code>false</code> is returned, the actual template
 * configured in the polling event container will be used.
 *
 * @author kimchy
 */
public interface TriggerOperationHandler {

    /**
     * <p>Allows to perform a trigger receive operation which control if the active receive operation
     * will be perfomed in a polling event container. This feature is mainly used when having polling
     * event operations with transactions where the trigger receive operation is perfomed outside of
     * a transaction thus reducing the creation of transactions did not perform the actual receive
     * operation.
     *
     * <p>If this operation returns a non <code>null</code> value, it means that the receive operation
     * should take place. If it returns a <code>null</code> value, no receive operation will be attempted.
     *
     * @param template       The template to use for the receive operation.
     * @param gigaSpace      The GigaSpace interface to perform the receive operations with
     * @param receiveTimeout Receive timeout value
     * @throws org.openspaces.core.GigaSpaceException
     *
     */
    Object triggerReceive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws GigaSpaceException;

    /**
     * Controls if the object returned from
     * {@link #triggerReceive(Object,org.openspaces.core.GigaSpace,long)} will be used as the
     * template for the receive operation by returnning <code>true</code>. If <code>false</code>
     * is returned, the actual template configured in the polling event container will be used.
     */
    boolean isUseTriggerAsTemplate();
}