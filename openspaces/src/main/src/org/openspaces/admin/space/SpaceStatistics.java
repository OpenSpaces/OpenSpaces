package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface SpaceStatistics {

    boolean isNA();

    int getSize();

    long getTimestamp();

    long getWriteCount();

    long getReadCount();

    long getTakeCount();

    long getNotifyRegistrationCount();

    long getCleanCount();

    long getUpdateCount();

    long getNotifyTriggerCount();

    long getNotifyAckCount();

    long getExecuteCount();

    /**
     * Remove happens when an entry is removed due to lease expiration or lease cancel.
     */
    long getRemoveCount();
}