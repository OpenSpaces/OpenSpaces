package org.openspaces.event;

import org.openspaces.core.GigaSpace;

/**
 * @author kimchy
 */
public interface SpaceDataEventListener {

    void onEvent(Object data, GigaSpace gigaSpace, Object source);
}
