package org.openspaces.admin.internal.esm;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventManager;
import org.openspaces.admin.esm.events.ElasticServiceManagerLifecycleEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.events.DefaultElasticServiceManagerAddedEventManager;
import org.openspaces.admin.internal.esm.events.DefaultElasticServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.esm.events.InternalElasticServiceManagerAddedEventManager;
import org.openspaces.admin.internal.esm.events.InternalElasticServiceManagerRemovedEventManager;
import org.openspaces.admin.pu.ProcessingUnit;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author Moran Avigdor
 */
public class DefaultElasticServiceManagers implements InternalElasticServiceManagers {

    private final InternalAdmin admin;
    
    private final Map<String, ElasticServiceManager> elasticServiceManagersByUID = new SizeConcurrentHashMap<String, ElasticServiceManager>();
    
    private final InternalElasticServiceManagerAddedEventManager elasticServiceManagerAddedEventManager;

    private final InternalElasticServiceManagerRemovedEventManager elasticServiceManagerRemovedEventManager;
    
    public DefaultElasticServiceManagers(InternalAdmin admin) {
        this.admin = admin;
        this.elasticServiceManagerAddedEventManager = new DefaultElasticServiceManagerAddedEventManager(this);
        this.elasticServiceManagerRemovedEventManager = new DefaultElasticServiceManagerRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public ElasticServiceManager getManagerByUID(String uid) {
        return elasticServiceManagersByUID.get(uid);
    }

    public ElasticServiceManager[] getManagers() {
        return elasticServiceManagersByUID.values().toArray(new ElasticServiceManager[0]);
    }

    public int getSize() {
        return elasticServiceManagersByUID.size();
    }

    public Map<String, ElasticServiceManager> getUids() {
        return Collections.unmodifiableMap(elasticServiceManagersByUID);
    }

    public boolean isEmpty() {
        return elasticServiceManagersByUID.isEmpty();
    }
    
    public Iterator<ElasticServiceManager> iterator() {
        return Collections.unmodifiableCollection(elasticServiceManagersByUID.values()).iterator();
    }
    
    public ElasticServiceManagerAddedEventManager getElasticServiceManagerAdded() {
        return this.elasticServiceManagerAddedEventManager;
    }

    public ElasticServiceManagerRemovedEventManager getElasticServiceManagerRemoved() {
        return this.elasticServiceManagerRemovedEventManager;
    }

    public void addElasticServiceManager(final InternalElasticServiceManager elasticServiceManager) {
        assertStateChangesPermitted();
        ElasticServiceManager existingESM = elasticServiceManagersByUID.put(elasticServiceManager.getUid(), elasticServiceManager);
        if (existingESM == null) {
            elasticServiceManagerAddedEventManager.elasticServiceManagerAdded(elasticServiceManager);
        }
    }

    public InternalElasticServiceManager removeElasticServiceManager(String uid) {
        assertStateChangesPermitted();
        final InternalElasticServiceManager existingESM = (InternalElasticServiceManager) elasticServiceManagersByUID.remove(uid);
        if (existingESM != null) {
            elasticServiceManagerRemovedEventManager.elasticServiceManagerRemoved(existingESM);
        }
        return existingESM;
    }
    
    public void addLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener) {
        getElasticServiceManagerAdded().add(eventListener);
        getElasticServiceManagerRemoved().add(eventListener);
    }
    
    public void removeLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener) {
        getElasticServiceManagerAdded().remove(eventListener);
        getElasticServiceManagerRemoved().remove(eventListener);        
    }

    public boolean waitFor(int numberOfElasticServiceManagers) {
        return waitFor(numberOfElasticServiceManagers, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfElasticServiceManagers, long timeout, TimeUnit timeUnit) {
        if (numberOfElasticServiceManagers == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            ElasticServiceManagerRemovedEventListener removed = new ElasticServiceManagerRemovedEventListener() {
                public void elasticServiceManagerRemoved(ElasticServiceManager elasticServiceManager) {
                    latch.countDown();
                }
            };
            getElasticServiceManagerRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getElasticServiceManagerRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfElasticServiceManagers);
            ElasticServiceManagerAddedEventListener added = new ElasticServiceManagerAddedEventListener() {
                public void elasticServiceManagerAdded(ElasticServiceManager elasticServiceManager) {
                    latch.countDown();
                }
            };
            getElasticServiceManagerAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getElasticServiceManagerAdded().remove(added);
            }
        }
    }

    public ElasticServiceManager waitForAtLeastOne() {
        return waitForAtLeastOne(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ElasticServiceManager waitForAtLeastOne(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<ElasticServiceManager> ref = new AtomicReference<ElasticServiceManager>();
        ElasticServiceManagerAddedEventListener added = new ElasticServiceManagerAddedEventListener() {
            public void elasticServiceManagerAdded(ElasticServiceManager elasticServiceManager) {
                ref.set(elasticServiceManager);
                latch.countDown();
            }
        };
        getElasticServiceManagerAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getElasticServiceManagerAdded().remove(added);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor)
            throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (ElasticServiceManager esm : this) {
            dumpResult.add(esm.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    
    private ElasticServiceManager getElasticServiceManager() {
        Iterator<ElasticServiceManager> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public ProcessingUnit deploy(ElasticDataGridDeployment deployment) {
        ElasticServiceManager elasticServiceManager = getElasticServiceManager();
        if (elasticServiceManager == null) {
            throw new AdminException("No Elastic Service Manager found to deploy [" + deployment.getDataGridName() + "]");
        }
        return elasticServiceManager.deploy(deployment);
    }

    public ProcessingUnit deploy(ElasticDataGridDeployment deployment, long timeout, TimeUnit timeUnit) {
        ElasticServiceManager elasticServiceManager = getElasticServiceManager();
        if (elasticServiceManager == null) {
            throw new AdminException("No Elastic Service Manager found to deploy [" + deployment.getDataGridName() + "]");
        }
        return elasticServiceManager.deploy(deployment, timeout, timeUnit);
    }
    
    protected void assertStateChangesPermitted() {
        this.admin.assertStateChangesPermitted();
    }
}
