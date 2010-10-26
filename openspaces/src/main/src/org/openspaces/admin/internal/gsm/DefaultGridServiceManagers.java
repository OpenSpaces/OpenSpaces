package org.openspaces.admin.internal.gsm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.*;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerRemovedEventManager;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceManager> gridServiceManagersByUID = new SizeConcurrentHashMap<String, GridServiceManager>();

    private final InternalGridServiceManagerAddedEventManager gridServiceManagerAddedEventManager;

    private final InternalGridServiceManagerRemovedEventManager gridServiceManagerRemovedEventManager;

    public DefaultGridServiceManagers(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceManagerAddedEventManager = new DefaultGridServiceManagerAddedEventManager(this);
        this.gridServiceManagerRemovedEventManager = new DefaultGridServiceManagerRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public GridServiceManagerAddedEventManager getGridServiceManagerAdded() {
        return this.gridServiceManagerAddedEventManager;
    }

    public GridServiceManagerRemovedEventManager getGridServiceManagerRemoved() {
        return this.gridServiceManagerRemovedEventManager;
    }

    public GridServiceManager[] getManagers() {
        return gridServiceManagersByUID.values().toArray(new GridServiceManager[0]);
    }

    public GridServiceManager getManagerByUID(String uid) {
        return gridServiceManagersByUID.get(uid);
    }

    public Map<String, GridServiceManager> getUids() {
        return Collections.unmodifiableMap(gridServiceManagersByUID);
    }

    public int getSize() {
        return gridServiceManagersByUID.size();
    }

    public boolean isEmpty() {
        return gridServiceManagersByUID.isEmpty();
    }

    public GridServiceManager waitForAtLeastOne() {
        return waitForAtLeastOne(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GridServiceManager waitForAtLeastOne(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
            public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                ref.set(gridServiceManager);
                latch.countDown();
            }
        };
        getGridServiceManagerAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getGridServiceManagerAdded().remove(added);
        }
    }

    public boolean waitFor(int numberOfGridServiceManagers) {
        return waitFor(numberOfGridServiceManagers, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfGridServiceManagers, long timeout, TimeUnit timeUnit) {
        if (numberOfGridServiceManagers == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            GridServiceManagerRemovedEventListener removed = new GridServiceManagerRemovedEventListener() {
                public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
                    latch.countDown();
                }
            };
            getGridServiceManagerRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceManagerRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfGridServiceManagers);
            GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
                public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                    latch.countDown();
                }
            };
            getGridServiceManagerAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceManagerAdded().remove(added);
            }
        }
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    public ProcessingUnit deploy(SpaceDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceName() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    public ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceName() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }
    
    public ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceUrl() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    public ProcessingUnit deploy(MemcachedDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceUrl() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    private GridServiceManager getGridServiceManager() {
        Iterator<GridServiceManager> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public void addLifecycleListener(GridServiceManagerLifecycleEventListener eventListener) {
        getGridServiceManagerAdded().add(eventListener);
        getGridServiceManagerRemoved().add(eventListener);
    }

    public void removeLifecycleListener(GridServiceManagerLifecycleEventListener eventListener) {
        getGridServiceManagerAdded().remove(eventListener);
        getGridServiceManagerRemoved().remove(eventListener);
    }

    public Iterator<GridServiceManager> iterator() {
        return Collections.unmodifiableCollection(gridServiceManagersByUID.values()).iterator();
    }

    public void addGridServiceManager(final InternalGridServiceManager gridServiceManager) {
        assertStateChangePermitted();
        GridServiceManager existingGSM = gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
        if (existingGSM == null) {
            gridServiceManagerAddedEventManager.gridServiceManagerAdded(gridServiceManager);
        }
    }

    public InternalGridServiceManager removeGridServiceManager(String uid) {
        assertStateChangePermitted();
        final InternalGridServiceManager existingGSM = (InternalGridServiceManager) gridServiceManagersByUID.remove(uid);
        if (existingGSM != null) {
            gridServiceManagerRemovedEventManager.gridServiceManagerRemoved(existingGSM);
        }
        return existingGSM;
    }

    public InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        assertStateChangePermitted();
        return (InternalGridServiceManager) gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (GridServiceManager gsm : this) {
            dumpResult.add(gsm.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    

    private void assertStateChangePermitted() {
        this.admin.assertStateChangesPermitted();
    }
}
