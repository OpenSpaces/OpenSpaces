package org.openspaces.admin.internal.alert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.alerts.AbstractAlert;

public class DefaultAlertRepository implements InternalAlertRepository {

    public volatile int groupHistorySize = 200;
    private volatile int resolvedHistorySize = 100;
    
    private final AtomicInteger incrementalId = new AtomicInteger();
    private final ConcurrentHashMap<String, AlertChain> alertsByGroupUid = new ConcurrentHashMap<String, AlertChain>();

    public void setGroupAlertHistorySize(int groupHistorySize) {
        this.groupHistorySize = groupHistorySize;
    }
    
    public void setResolvedAlertHistorySize(int resolvedHistorySize) {
        this.resolvedHistorySize = resolvedHistorySize;
    }

    public boolean addAlert(Alert alert) {

        AlertChain chain = alertsByGroupUid.get(alert.getGroupUid());
        if (chain == null) {
            if (alert.getStatus().isResolved() || alert.getStatus().isNotAvailable()) {
                return false;
            }
            
            AlertChain newChain = new AlertChain(groupHistorySize);
            chain = alertsByGroupUid.putIfAbsent(alert.getGroupUid(), newChain);
            if (chain == null) {
                chain = newChain;
            }
        }
        ensureResolvedHistoryCapacity();
        setAlertUid(alert);

        return chain.addAlert(alert);
    }

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
        
        internalAlert.setAlertUid((incrementalId.incrementAndGet() + "@" + Integer.toHexString(System.identityHashCode(alert))));
    }

    private void ensureResolvedHistoryCapacity() {
        //fast exit check
        if (alertsByGroupUid.size() < resolvedHistorySize) {
            return;
        }
        
        int countResolved = 0;
        AlertChain olderTimestampAlertChain = null;
        for (AlertChain chain : alertsByGroupUid.values()) {
            if (chain.isResolved()) {
                countResolved++;
                //replace with older timestamp
                if (olderTimestampAlertChain == null || olderTimestampAlertChain.resolvedAlert.getTimestamp() > chain.resolvedAlert.getTimestamp()) {
                    olderTimestampAlertChain = chain;
                }
            }
        }
        
        if (countResolved >= resolvedHistorySize && olderTimestampAlertChain != null) {
            removeAlertHistoryByGroupUid(olderTimestampAlertChain.resolvedAlert.getGroupUid());
        }
    }

    public Alert getAlertByUid(String uid) {
        for (AlertChain alertChain : alertsByGroupUid.values()) {
            Alert alert = alertChain.getAlertByUid(uid);
            if (alert != null) {
                return alert;
            }
        }
        return null;
    }
    
    public AlertHistory getAlertHistoryByGroupUid(String groupUid) {
        AlertChain alertChain = alertsByGroupUid.get(groupUid);
        if (alertChain == null) {
            return new DefaultAlertHistory(new Alert[0]);
        } else {
            DefaultAlertHistory alerts = new DefaultAlertHistory(alertChain.toArray());
            return alerts;
        }
    }

    public AlertHistory[] getAlertHistory() {
        TreeMap<Alert, AlertHistory> treeMap = new TreeMap<Alert, AlertHistory>(new Comparator<Alert>() {
            public int compare(Alert a1, Alert a2) {
                if (a1.getTimestamp() <= a2.getTimestamp()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        
        for (AlertChain chain : alertsByGroupUid.values()) {
            Alert[] alerts = chain.toArray();
            DefaultAlertHistory alertHistory = new DefaultAlertHistory(alerts);
            treeMap.put(alerts[alerts.length-1], alertHistory);
        }

        AlertHistory[] result = treeMap.values().toArray(new AlertHistory[treeMap.size()]);
        return result;
    }
    
    public boolean isAlertResolvedByGroupUid(String groupUid) {
        AlertChain alertChain = alertsByGroupUid.get(groupUid);
        if (alertChain == null) return true;
        return (alertChain.isResolved());
    }

    public Iterator<Alert> iterator() {
        List<Alert> alerts = new ArrayList<Alert>();
        for (AlertChain chain : alertsByGroupUid.values()) {
            alerts.addAll(chain.asList());
        }
        return alerts.iterator();
    }
 
    public boolean removeAlertHistoryByGroupUid(String groupUid) {
        AlertChain alertChain = alertsByGroupUid.remove(groupUid);
        return alertChain != null;
    }

    /*
     * Holds unresolved alerts, a history chain of negative alerts after the first unresolved alert
     * was fired, and a resolution alert.
     */
    private static final class AlertChain {
        private Alert unresolvedAlert;
        private Alert resolvedAlert;
        private List<Alert> unresolvedAlertsChain;
        private int historyIndex = 0;
        private final Map<String, Alert> alertByUid = new HashMap<String, Alert>();
        private final int groupHistorySize;

        public AlertChain(int groupHistorySize) {
            this.groupHistorySize = groupHistorySize;
        }

        private void initialize() {
            resolvedAlert = null;
            unresolvedAlert = null;
            alertByUid.clear();
            if (unresolvedAlertsChain != null) {
                unresolvedAlertsChain.clear();
                historyIndex = 0;
            }
        }
        
        public synchronized boolean isResolved() {
            return (resolvedAlert != null); 
        }

        public synchronized boolean addAlert(Alert alert) {
            if (alert.getStatus().isResolved() || alert.getStatus().isNotAvailable()) {
                if (resolvedAlert != null)
                    return false;
                resolvedAlert = alert;
            } else {
                if (resolvedAlert != null) {
                    initialize();
                }

                if (unresolvedAlert == null) {
                    unresolvedAlert = alert;
                } else {
                    if (unresolvedAlertsChain == null) {
                        unresolvedAlertsChain = new ArrayList<Alert>();
                    }
                    if (unresolvedAlertsChain.size() < groupHistorySize) {
                        unresolvedAlertsChain.add(alert);
                    } else {
                        /*
                         * Try to keep history uniformed in cases of overflow, by removing using a
                         * cyclic moving index. - remove alert at index 0, adding new alert to the
                         * end - remove alert at index 1, keeping alert at index 0, and adding new
                         * alert to the end - remove alert at index 2, keeping alerts at index 0 and
                         * 1, and adding new alert to the end ... - and if index reaches the end,
                         * will cycle again starting at index 0.
                         */
                        Alert removed = unresolvedAlertsChain.remove(historyIndex);
                        unresolvedAlertsChain.add(alert);
                        historyIndex = (++historyIndex % unresolvedAlertsChain.size());
                        alertByUid.remove(removed.getAlertUid());
                    }
                }
            }
            alertByUid.put(alert.getAlertUid(), alert);
            return true;
        }

        public Alert getAlertByUid(String uid) {
            return alertByUid.get(uid);
        }

        /**
         * @return an array which it's first element is the first unresolved alert, followed by all
         *         consecutive unresolved alerts, ending in a resolved alert (if resolved).
         */
        public Alert[] toArray() {
            List<Alert> list = asList();
            return list.toArray(new Alert[list.size()]);
        }

        private List<Alert> asList() {
            List<Alert> list = new ArrayList<Alert>();
            if (unresolvedAlert != null) {
                list.add(unresolvedAlert);
            }
            if (unresolvedAlertsChain != null) {
                list.addAll(unresolvedAlertsChain);
            }
            if (resolvedAlert != null) {
                list.add(resolvedAlert);
            }
            return list;
        }
    }
}
