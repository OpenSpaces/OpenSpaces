package org.openspaces.wan.mirror;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceOptimisticLockingFailureException;

public interface CollisionHandler {

    public void handleCollision(Object object, int sourceSiteId, int targetSiteId, 
            SpaceOptimisticLockingFailureException solfe, GigaSpace targetSpace) ;

}
