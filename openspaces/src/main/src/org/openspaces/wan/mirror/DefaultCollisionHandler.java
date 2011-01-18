package org.openspaces.wan.mirror;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceOptimisticLockingFailureException;

public class DefaultCollisionHandler implements CollisionHandler {

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(DefaultCollisionHandler.class.getName());

    public void handleCollision(final Object object, final int sourceSiteId, final int targetSiteId,
            final SpaceOptimisticLockingFailureException solfe, final GigaSpace targetSpace) {
        logger.severe("An Optimistic Locking collision was detected. " +
                "The entry that failed was of type " + object.getClass().getName() + " with ID: " + solfe.getUID()
                + ". Client Version ID: " + solfe.getClientVersionID() + ", Space version ID: "
                + solfe.getSpaceVersionID());
        logger.severe("Failed object toString(): " + object.toString());

    }

}
