package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface SpaceInstanceStatistics {

    boolean isNA();

    long getTimestamp();
    
    long getWriteCount();

    long getReadCount();

    long getTakeCount();

    long getNotifyCount();

    long getCleanCount();

    long getUpdateCount();

    long getReadMultipleCount();

    long getTakeMultipleCount();

    long getNotifyTriggerCount();

    /**
     * Remove happens when an entry is removed due to lease expiration or lease cancel.
     */
    long getRemoveCount();
}
