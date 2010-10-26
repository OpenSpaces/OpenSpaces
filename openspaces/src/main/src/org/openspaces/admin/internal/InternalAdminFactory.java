package org.openspaces.admin.internal;

import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;

public class InternalAdminFactory extends AdminFactory {

    /**
     * Enables a single event loop threading model in which all
     * event listeners and admin state updates are done on the same thread.
     * The underlying assumption is that event listeners do not perform an I/O operation
     * so they won't block the single event thread.
     * @return this (fluent API)
     */
    public AdminFactory singleThreadedEventListeners() {
        ((InternalAdmin)super.getAdmin()).singleThreadedEventListeners();
        return this;
    }

}
