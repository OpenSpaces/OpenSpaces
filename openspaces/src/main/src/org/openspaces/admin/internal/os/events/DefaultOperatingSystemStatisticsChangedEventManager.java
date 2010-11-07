package org.openspaces.admin.internal.os.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemStatisticsChangedEventManager implements InternalOperatingSystemStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final OperatingSystem operatingSystem;

    private final OperatingSystems operatingSystems;

    private final List<OperatingSystemStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<OperatingSystemStatisticsChangedEventListener>();

    public DefaultOperatingSystemStatisticsChangedEventManager(InternalAdmin admin, OperatingSystems operatingSystems) {
        this.admin = admin;
        this.operatingSystems = operatingSystems;
        this.operatingSystem = null;
    }

    public DefaultOperatingSystemStatisticsChangedEventManager(InternalAdmin admin, OperatingSystem operatingSystem) {
        this.admin = admin;
        this.operatingSystem = operatingSystem;
        this.operatingSystems = null;
    }

    public void operatingSystemStatisticsChanged(final OperatingSystemStatisticsChangedEvent event) {
        for (final OperatingSystemStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.operatingSystemStatisticsChanged(event);
                }
            });
        }
    }

    public void add(OperatingSystemStatisticsChangedEventListener eventListener) {
        add(eventListener, false);
    }

    public void add(final OperatingSystemStatisticsChangedEventListener eventListener, boolean withHistory) {
        if (withHistory) {
            List<OperatingSystem> oss = new ArrayList<OperatingSystem>();
            if (operatingSystems != null) {
                oss.addAll(Arrays.asList(operatingSystems.getOperatingSystems()));
            } else if (operatingSystem != null) {
                oss.add(operatingSystem);
            }
            for (final OperatingSystem os: oss) {
                OperatingSystemStatistics stats = os.getStatistics();
                if (!stats.isNA()) {
                    List<OperatingSystemStatistics> timeline = stats.getTimeline();
                    Collections.reverse(timeline);
                    for (final OperatingSystemStatistics osStats : timeline) {
                        admin.raiseEvent(eventListener, new Runnable() {
                            public void run() {
                                eventListener.operatingSystemStatisticsChanged(new OperatingSystemStatisticsChangedEvent(os, osStats));
                            }
                        });
                    }
                }
            }
        }
        listeners.add(eventListener);
    }

    public void remove(OperatingSystemStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureOperatingSystemStatisticsChangedEventListener(eventListener));
        } else {
            add((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureOperatingSystemStatisticsChangedEventListener(eventListener));
        } else {
            remove((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}