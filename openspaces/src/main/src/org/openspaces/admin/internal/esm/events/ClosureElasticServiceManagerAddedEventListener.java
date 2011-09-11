package org.openspaces.admin.internal.esm.events;

import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author Moran Avigdor
 */
public class ClosureElasticServiceManagerAddedEventListener extends AbstractClosureEventListener implements
        ElasticServiceManagerAddedEventListener {

    public ClosureElasticServiceManagerAddedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void elasticServiceManagerAdded(ElasticServiceManager elasticServiceManager) {
        getClosure().call(elasticServiceManager);
    }
}