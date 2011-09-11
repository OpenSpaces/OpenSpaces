package org.openspaces.admin.internal.esm.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.InternalElasticServiceManagers;
import org.openspaces.admin.internal.support.GroovyHelper;

/**
 * @author Moran Avigdor
 */
public class DefaultElasticServiceManagerAddedEventManager implements InternalElasticServiceManagerAddedEventManager {

    private final InternalElasticServiceManagers elasticServiceManagers;

    private final InternalAdmin admin;

    private final List<ElasticServiceManagerAddedEventListener> listeners = new CopyOnWriteArrayList<ElasticServiceManagerAddedEventListener>();

    public DefaultElasticServiceManagerAddedEventManager(InternalElasticServiceManagers elasticServiceManagers) {
        this.elasticServiceManagers = elasticServiceManagers;
        this.admin = (InternalAdmin) elasticServiceManagers.getAdmin();
    }

    @Override
    public void elasticServiceManagerAdded(final ElasticServiceManager elasticServiceManager) {
        for (final ElasticServiceManagerAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                @Override
                public void run() {
                    listener.elasticServiceManagerAdded(elasticServiceManager);
                }
            });
        }
    }

    @Override
    public void add(final ElasticServiceManagerAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                @Override
                public void run() {
                    for (ElasticServiceManager elasticServiceManager : elasticServiceManagers) {
                        eventListener.elasticServiceManagerAdded(elasticServiceManager);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    @Override
    public void add(final ElasticServiceManagerAddedEventListener eventListener) {
        add(eventListener, true);
    }

    @Override
    public void remove(ElasticServiceManagerAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureElasticServiceManagerAddedEventListener(eventListener));
        } else {
            add((ElasticServiceManagerAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureElasticServiceManagerAddedEventListener(eventListener));
        } else {
            remove((ElasticServiceManagerAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}