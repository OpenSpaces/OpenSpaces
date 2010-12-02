package org.openspaces.admin.internal.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.openspaces.admin.alerts.Alert;

public class DefaultAlertRepository implements InternalAlertRepository {

    public volatile int historySize = 200;
    private final AtomicInteger id = new AtomicInteger();
    private final ConcurrentHashMap<String, AlertChain> alertsByType = new ConcurrentHashMap<String, AlertChain>();

    public void setAlertsHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public void addAlert(Alert alert) {
        alert.setAlertId(id.incrementAndGet() + "@" + Integer.toHexString(hashCode()));

        AlertChain chain = alertsByType.get(alert.getAlertStrategyBeanClassName());
        if (chain == null) {
            AlertChain newChain = new AlertChain(historySize);
            chain = alertsByType.putIfAbsent(alert.getAlertStrategyBeanClassName(), newChain);
            if (chain == null) {
                chain = newChain;
            }
        }
        chain.addAlert(alert);
    }

    public Alert getAlertById(String id) {
        for (AlertChain alertChain : alertsByType.values()) {
            Alert alert = alertChain.getAlertById(id);
            if (alert != null) {
                return alert;
            }
        }
        return null;
    }

    public Alert[] getAlertsByType(String type) {
        AlertChain chain = alertsByType.get(type);
        if (chain == null) {
            return new Alert[0];
        } else {
            return chain.toArray();
        }
    }

    public AlertGroup[] getAlertsGroupedByType() {
        ArrayList<AlertGroup> groupedByType = new ArrayList<AlertGroup>();
        for (AlertChain chain : alertsByType.values()) {
            Alert[] alerts = chain.toArray();
            DefaultAlertGroup alertGroup = new DefaultAlertGroup(alerts);
            groupedByType.add(alertGroup);
        }
        return groupedByType.toArray(new AlertGroup[groupedByType.size()]);
    }

    public Iterator<Alert> iterator() {
        List<Alert> alerts = new ArrayList<Alert>();
        for (AlertChain chain : alertsByType.values()) {
            alerts.addAll(chain.asList());
        }
        return alerts.iterator();
    }

    public void removeAlert(Alert alert) {
        alertsByType.remove(alert.getAlertStrategyBeanClassName());
    }

    /*
     * Holds a negative alert, a history chain of negative alerts after the first negative alert was
     * fired, and a positive alert.
     */
    private static final class AlertChain {
        private Alert negativeAlert;
        private Alert positiveAlert;
        private List<Alert> negativeChain;
        private int historyIndex = 0;
        private final Map<String, Alert> alertById = new HashMap<String, Alert>();
        private final int alertsHistorySize;

        public AlertChain(int historySize) {
            this.alertsHistorySize = historySize;
        }

        private void initialize() {
            positiveAlert = null;
            negativeAlert = null;
            alertById.clear();
            if (negativeChain != null) {
                negativeChain.clear();
                historyIndex = 0;
            }
        }

        public synchronized void addAlert(Alert alert) {
            if (alert.isPositive()) {
                positiveAlert = alert;
            } else {
                if (positiveAlert != null) {
                    initialize();
                }

                if (negativeAlert == null) {
                    negativeAlert = alert;
                } else {
                    if (negativeChain == null) {
                        negativeChain = new ArrayList<Alert>();
                    }
                    if (negativeChain.size() < alertsHistorySize) {
                        negativeChain.add(alert);
                    } else {
                        /*
                         * Try to keep history uniformed in cases of overflow, by removing using a
                         * cyclic moving index. - remove alert at index 0, adding new alert to the
                         * end - remove alert at index 1, keeping alert at index 0, and adding new
                         * alert to the end - remove alert at index 2, keeping alerts at index 0 and
                         * 1, and adding new alert to the end ... - and if index reaches the end,
                         * will cycle again starting at index 0.
                         */
                        Alert removed = negativeChain.remove(historyIndex);
                        negativeChain.add(alert);
                        historyIndex = (++historyIndex % negativeChain.size());
                        alertById.remove(removed.getAlertId());
                    }
                }
            }
            alertById.put(alert.getAlertId(), alert);
        }

        public Alert getAlertById(String id) {
            return alertById.get(id);
        }

        /**
         * @return an array which it's first element is the first negative alert, followed by all
         *         consecutive negative alerts, ending in a positive alert.
         */
        public Alert[] toArray() {
            List<Alert> list = asList();
            return list.toArray(new Alert[list.size()]);
        }

        private List<Alert> asList() {
            List<Alert> list = new ArrayList<Alert>();
            if (negativeAlert != null) {
                list.add(negativeAlert);
            }
            if (negativeChain != null) {
                list.addAll(negativeChain);
            }
            if (positiveAlert != null) {
                list.add(positiveAlert);
            }
            return list;
        }
    }
}
