package org.openspaces.admin;

import org.openspaces.admin.internal.admin.DefaultAdmin;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class AdminFactory {

    private DefaultAdmin admin = new DefaultAdmin();

    public AdminFactory addGroup(String group) {
        admin.addGroup(group);
        return this;
    }

    public AdminFactory addLocator(String locator) {
        admin.addLocator(locator);
        return this;
    }

    public AdminFactory setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit) {
        admin.setProcessingUnitMonitorInterval(interval, timeUnit);
        return this;
    }

    public Admin createAdmin() {
        admin.begin();
        return admin;
    }
}
