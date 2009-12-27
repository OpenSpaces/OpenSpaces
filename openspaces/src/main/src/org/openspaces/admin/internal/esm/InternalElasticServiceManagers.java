package org.openspaces.admin.internal.esm;

import org.openspaces.admin.esm.ElasticServiceManagers;

/**
 * @author Moran Avigdor
 */
public interface InternalElasticServiceManagers extends ElasticServiceManagers {

    void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager);

    InternalElasticServiceManager removeElasticServiceManager(String uid);
}