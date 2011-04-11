package org.openspaces.admin.internal.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.AbstractAlert;


public class DefaultAlertRepository implements AlertRepository {

    public class AlertGroup {
        private final ArrayList<Alert> alertsInGroupList = new ArrayList<Alert>();
        public void addAlert(Alert alert) {
            alertsInGroupList.add(0,alert);
        }
        
        public boolean isResolved() {
            return alertsInGroupList.get(0).getStatus().isResolved();
        }
        
        public boolean isUnResolved() {
            return alertsInGroupList.get(0).getStatus().getValue() > AlertStatus.RESOLVED.getValue();
        }
        
        public String getGroupUid() {
            return alertsInGroupList.get(0).getGroupUid();
        }
        public Alert[] toArray() {
            return alertsInGroupList.toArray(new Alert[alertsInGroupList.size()]);
        }
    }

    public final static int STORE_LIMIT = 2000;
    
    private int incrementalAlertUid = 0;

    /**
     * Maps an {@link Alert#getGroupUid()} to an {@link AlertGroup}. Holds only mapping of new groups.
     * Old groups, with same group Uid, are located in {@link #alertGroupList}.
     */
    private final HashMap<String, AlertGroup> alertGroupByGroupUidMapping = new HashMap<String, AlertGroup>();
    
    /**
     * Groups together alerts belonging to the same group Uid. Note that a new group may be created,
     * if the former group was already resolved.  Any updated group gets bumped-up to the head. If the
     * list reaches the limit, we use the tail to remove old alert groups.
     */
    private final LinkedList<AlertGroup> alertGroupList = new LinkedList<AlertGroup>();

    //called under "this" lock
    public synchronized void addAlert(Alert alert) {
        
        setAlertUid(alert);
        mapAlertByGroupUid(alert);
        ensureStoreLimit();
    }

    /**
     * Sets an incremental id as the alert UID.
     */
    //called under "this" lock
    private void setAlertUid(Alert alert) {
        InternalAlert internalAlert = null;
        
        if (alert instanceof AbstractAlert) {
            alert = ((AbstractAlert)alert).getAlert();
        }
        
        if (alert instanceof InternalAlert) {
            internalAlert = (InternalAlert)alert;
        }
        
        if (internalAlert == null) {
            throw new IllegalStateException("Can't set alert Uid, Alert must implement InternalAlert interface.");
        }
        
        ++incrementalAlertUid;
        internalAlert.setAlertUid((incrementalAlertUid + "@" + Integer.toHexString(System.identityHashCode(alert))));
    }

    /**
     * Map alerts to their respective group. A new group is needed when the old group is already resolved,
     * or if a group never existed.
     * @param alert the new alert.
     */
    //called under "this" lock
    private void mapAlertByGroupUid(Alert alert) {
        AlertGroup alertGroup = alertGroupByGroupUidMapping.get(alert.getGroupUid());
        boolean needsNewAlertGroup = (alertGroup != null && alertGroup.isResolved());
        if (alertGroup == null || needsNewAlertGroup) {
            alertGroup = new AlertGroup();
            alertGroup.addAlert(alert);
            alertGroupByGroupUidMapping.put(alert.getGroupUid(), alertGroup);
            alertGroupList.addFirst(alertGroup);
        } else {
            alertGroup.addAlert(alert);
            alertGroupList.remove(alertGroup);
            alertGroupList.addFirst(alertGroup);
        }
    }

    /**
     * If store reaches limit, start removing from the tail -
     * alert that were resolved/NA we can remove completely.
     * alerts that are still unresolved, we can remove only alerts inside the group.
     */
    //called under "this" lock
    private void ensureStoreLimit() {
        if (size() <= STORE_LIMIT) {
            return;
        }
        
        /*
         * Look for candidate - an alert still unresolved - we can remove only alerts inside the group.
         */
        boolean removed = false;
        for (int i=(alertGroupList.size() -1); i>=0; --i) {
            AlertGroup alertGroup = alertGroupList.get(i);
            if (alertGroup.isUnResolved()) {

                /*
                 * don't remove first and last alert!
                 * First alert (index 0) indicates the last alert from the group.
                 * Last alert (index size -1) indicates the first alert to open the group. 
                 */
                for (int j=(alertGroup.alertsInGroupList.size() -2); j>0; j-=2) {
                    alertGroup.alertsInGroupList.remove(j);
                    removed = true;
                }

                if (removed) {
                    break;
                }
            }
        }
        
        if (!removed) {
            //remove whatever is in last position.
            AlertGroup lastAlertGroup = alertGroupList.removeLast();

            /*
             * last alert group may differ from the mapped one since a new group may have been created
             * since then. Only remove if they equal!
             */
            AlertGroup mappedAlertGroup = alertGroupByGroupUidMapping.get(lastAlertGroup.getGroupUid());
            if (mappedAlertGroup == lastAlertGroup) {
                alertGroupByGroupUidMapping.remove(lastAlertGroup.getGroupUid());
            }
        }
    }

    //called under "this" lock
    public synchronized Alert getAlertByAlertUid(String alertUid) {
        for (AlertGroup alertGroup : alertGroupList) {
            for (Alert alert : alertGroup.alertsInGroupList) {
                if (alert.getAlertUid().equals(alertUid)) {
                    return alert;
                }
            }
        }
        return null;
    }

    //called under "this" lock
    public synchronized Alert[] getAlertsByGroupUid(String groupUid) {
        for (AlertGroup alertGroup : alertGroupList) {
            if (alertGroup.getGroupUid().equals(groupUid)) {
                return alertGroup.toArray();
            }
        }
        return new Alert[0];
    }

    //called under "this" lock
    public synchronized Iterable<Alert> iterateFifo() {
        ArrayList<Alert> list = new ArrayList<Alert>(Math.max(10, alertGroupList.size()));
        for (int i=(alertGroupList.size() -1); i>=0; --i) {
            AlertGroup alertGroup = alertGroupList.get(i);
            for (int j=(alertGroup.alertsInGroupList.size() -1); j>=0; --j) {
                Alert alert = alertGroup.alertsInGroupList.get(j);
                list.add(alert);
            }
        }
        return list;
    }
    
    //called under "this" lock
    public synchronized Iterable<Alert> iterateLifo() {
        ArrayList<Alert> list = new ArrayList<Alert>(Math.max(10, alertGroupList.size()));
        for (int i=0; i<alertGroupList.size(); ++i) {
            AlertGroup alertGroup = alertGroupList.get(i);
            for (int j=0; j<alertGroup.alertsInGroupList.size(); ++j) {
                Alert alert = alertGroup.alertsInGroupList.get(j);
                list.add(alert);
            }
        }
        return list;
    }
    
    //called under "this" lock
    public synchronized Iterable<Iterable<Alert>> list() {
        ArrayList<Iterable<Alert>> list = new ArrayList<Iterable<Alert>>(Math.max(10, alertGroupList.size()));
        for (int i=0; i<alertGroupList.size(); ++i) {
            AlertGroup alertGroup = alertGroupList.get(i);
            ArrayList<Alert> alertsInGroup = new ArrayList<Alert>(alertGroup.alertsInGroupList);
            list.add(alertsInGroup);
        }
        return list;
    }

    //called under "this" lock
    public synchronized int size() {
        int size = 0;
        for (AlertGroup alertGroup : alertGroupList) {
            size += alertGroup.alertsInGroupList.size();
        }
        return size;
    }
}
