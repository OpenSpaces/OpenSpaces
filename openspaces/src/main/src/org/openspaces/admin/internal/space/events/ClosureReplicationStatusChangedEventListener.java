package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureReplicationStatusChangedEventListener extends AbstractClosureEventListener implements ReplicationStatusChangedEventListener {

    public ClosureReplicationStatusChangedEventListener(Object closure) {
        super(closure);
    }

    public void replicationStatusChanged(ReplicationStatusChangedEvent event) {
        getClosure().call(event);
    }
}