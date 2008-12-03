package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceModeChangedEventListener {

    void spaceModeChanged(SpaceModeChangedEvent event);
}