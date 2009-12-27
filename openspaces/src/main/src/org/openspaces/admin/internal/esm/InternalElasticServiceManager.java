package org.openspaces.admin.internal.esm;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;

/**
 * @author Moran Avigdor
 */
public interface InternalElasticServiceManager extends ElasticServiceManager, InternalAgentGridComponent {
    ServiceID getServiceID();
}