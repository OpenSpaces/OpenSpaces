package org.openspaces.admin.internal.alert;

import org.openspaces.admin.alert.Alert;

public interface AlertRepository {
	
    /**
     * Add the alert to the repository.
     * The added alert will be set with an alert UID.
     * @param alert the alert to add to the repository.
     */
    void addAlert(Alert alert);
    
    /** 
     * Get a specific alert by its {@link Alert#getAlertUid()}
     * @param alertUid The alert unique identifier
     * @return The alert matching this UID. <code>null</code> if not match was found.
     */
	Alert getAlertByAlertUid(String alertUid);

    /**
     * Get an array of alerts matching a {@link Alert#getGroupUid()}. The alerts are ordered in a
     * LIFO order. Last alert to arrive of the same group will be in index zero, first alert to
     * arrive of the same group will be in index size -1.
     * 
     * @param groupUid
     *            The group unique identifier
     * @return The alerts belonging to this group UID. Zero-length array if no match was found.
     */
	Alert[] getAlertsByGroupUid(String groupUid);
	
	/**
	 * An Iterator over the alerts in the repository from the first alert to arrive to the last.
	 * @return an iterator in a first-in (the group) and first-out (of iterator) order.
	 */
	Iterable<Alert> iterateFifo();
	
	/**
     * An Iterator over the alerts in the repository from the last alert to arrive to the first.
     * @return an iterator in a last-in (the group) and first-out (of iterator) order.
     */
    Iterable<Alert> iterateLifo();

    /**
     * An Iterator of Iterators over the alerts in the repository, each iterator groups together alerts
     * belonging to the same group UID. The list is ordered by sequence of alert events, first
     * iterator is the last group to be have been updated. There may be more than one Iterator with
     * the same group UID (this happens when a 'group' is resolved, and new alerts with the same
     * group UID arrive at a later time).
     * 
     * @return an iterator in a last-updated (per group) order.
     */
    Iterable<Iterable<Alert>> list();
    
    /**
     * The number of Alerts stored in this alert repository.
     * @return the size of the repository.
     */
    int size();
}
