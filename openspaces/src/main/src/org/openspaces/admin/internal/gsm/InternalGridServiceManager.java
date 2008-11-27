package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.support.InternalGridComponent;

/**
 * @author kimchy
 */
public interface InternalGridServiceManager extends GridServiceManager, InternalGridComponent {

    ServiceID getServiceID();

    GSM getGSM();
}