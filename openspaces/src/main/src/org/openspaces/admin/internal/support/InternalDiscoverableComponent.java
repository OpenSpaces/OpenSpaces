package org.openspaces.admin.internal.support;

import org.openspaces.admin.DiscoverableComponent;

/**
 * @author kimchy
 */
public interface InternalDiscoverableComponent extends DiscoverableComponent {

    void setDiscovered(boolean discovered);
}
