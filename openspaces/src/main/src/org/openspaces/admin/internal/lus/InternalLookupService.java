package org.openspaces.admin.internal.lus;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.lus.LookupService;

/**
 * @author kimchy
 */
public interface InternalLookupService extends LookupService, InternalAgentGridComponent {

    ServiceID getServiceID();

    ServiceRegistrar getRegistrar();
}
