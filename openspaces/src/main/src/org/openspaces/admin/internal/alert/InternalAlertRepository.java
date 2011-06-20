package org.openspaces.admin.internal.alert;

/**
 * Internal alert repository extension API.
 * @author Moran Avigdor
 * @since 8.0.3
 */
public interface InternalAlertRepository extends AlertRepository {

    /**
     * The alert repository storage limit for alerts which are unresolved.
     * @param limit a limit, default is 200;
     */
    void setStoreLimit(int limit);
}
