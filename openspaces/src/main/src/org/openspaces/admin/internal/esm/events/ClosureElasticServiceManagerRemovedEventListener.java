package org.openspaces.admin.internal.esm.events;

import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author Moran Avigdor
 */
public class ClosureElasticServiceManagerRemovedEventListener extends AbstractClosureEventListener implements ElasticServiceManagerRemovedEventListener {

    public ClosureElasticServiceManagerRemovedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void elasticServiceManagerRemoved(ElasticServiceManager elasticServiceManager) {
        getClosure().call(elasticServiceManager);
    }
}