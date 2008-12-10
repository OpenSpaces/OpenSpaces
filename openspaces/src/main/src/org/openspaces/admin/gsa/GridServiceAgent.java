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

    void startGridService(GridServiceManagerOptions options);

    GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options);

    GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options, long timeout, TimeUnit timeUnit);

    void startGridService(GridServiceContainerOptions options);

    GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options);

    GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options, long timeout, TimeUnit timeUnit);

    void startGridService(LookupServiceOptions options);

    LookupService startGridServiceAndWait(LookupServiceOptions options);

    LookupService startGridServiceAndWait(LookupServiceOptions options, long timeout, TimeUnit timeUnit);
}