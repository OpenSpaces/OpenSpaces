package org.openspaces.admin.gsa;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface GridServiceAgent extends GridComponent {

    void startGridServiceManager();

    GridServiceManager startGridServiceManagerAndWait();

    GridServiceManager startGridServiceManagerAndWait(long timeout, TimeUnit timeUnit);

    void startGridServiceContainer();

    GridServiceContainer startGridServiceContainerAndWait();

    GridServiceContainer startGridServiceContainerAndWait(long timeout, TimeUnit timeUnit);

    void startLookupService();

    LookupService startLookupServiceAndWait();

    LookupService startLookupServiceAndWait(long timeout, TimeUnit timeUnit);
}
