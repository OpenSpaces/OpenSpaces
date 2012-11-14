/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.esm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManager;
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
import org.openspaces.security.AdminFilterHelper;

import com.gigaspaces.internal.jvm.JVMDetails;
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

    @Override
    public Admin getAdmin() {
        return this.admin;
    }
    
    @Override
    public ElasticServiceManager getManagerByUID(String uid) {
        
        ElasticServiceManager elasticServiceManager = elasticServiceManagersByUID.get(uid);
        boolean accept = accept( ( InternalElasticServiceManager )elasticServiceManager );
        return accept ? elasticServiceManager : null;
    }
    
    @Override
    public ElasticServiceManager[] getManagers() {
        Collection<ElasticServiceManager> values = elasticServiceManagersByUID.values();
        List<ElasticServiceManager> filteredManagers = new LinkedList<ElasticServiceManager>();
        for( ElasticServiceManager esm : values ){
            if( accept( ( InternalElasticServiceManager )esm ) ){
                filteredManagers.add( esm );
            }
        }
        return filteredManagers.toArray( new ElasticServiceManager[filteredManagers.size()] );
    }

    @Override
    public int getSize() {
        return getManagers().length;
    }

    @Override
    public Map<String, ElasticServiceManager> getUids() {
        Set<Entry<String, ElasticServiceManager>> entrySet = elasticServiceManagersByUID.entrySet();
        Map<String,ElasticServiceManager> filteredManageresMap = new HashMap<String, ElasticServiceManager>();
        for( Entry<String, ElasticServiceManager> entry : entrySet ){
            ElasticServiceManager esm = entry.getValue();
            if( accept( ( InternalElasticServiceManager)esm ) ){
                filteredManageresMap.put( entry.getKey(), esm);
            }
        }

        return filteredManageresMap;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }
    
   @Override
   public Iterator<ElasticServiceManager> iterator() {
        return Collections.unmodifiableCollection( getUids().values() ).iterator();
    }
    
    @Override
    public ElasticServiceManagerAddedEventManager getElasticServiceManagerAdded() {
        return this.elasticServiceManagerAddedEventManager;
    }

    @Override
    public ElasticServiceManagerRemovedEventManager getElasticServiceManagerRemoved() {
        return this.elasticServiceManagerRemovedEventManager;
    }

    @Override
    public void addElasticServiceManager(final InternalElasticServiceManager elasticServiceManager) {
        assertStateChangesPermitted();
        if (elasticServiceManager == null) {
            throw new IllegalArgumentException("elasticServiceManager cannot be null");
        }
        InternalElasticServiceManager existingESM = (InternalElasticServiceManager)
                elasticServiceManagersByUID.put(elasticServiceManager.getUid(), elasticServiceManager);
        if (existingESM == null && accept(elasticServiceManager)) {
            elasticServiceManagerAddedEventManager.elasticServiceManagerAdded(elasticServiceManager);
        }
    }

    @Override
    public InternalElasticServiceManager removeElasticServiceManager(String uid) {
        assertStateChangesPermitted();
        final InternalElasticServiceManager existingESM = (InternalElasticServiceManager) elasticServiceManagersByUID.remove(uid);
        if (existingESM != null && accept(existingESM)) {
            elasticServiceManagerRemovedEventManager.elasticServiceManagerRemoved(existingESM);
        }
        return existingESM;
    }
    
    @Override
    public void addLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener) {
        getElasticServiceManagerAdded().add(eventListener);
        getElasticServiceManagerRemoved().add(eventListener);
    }
    
    @Override
    public void removeLifecycleListener(ElasticServiceManagerLifecycleEventListener eventListener) {
        getElasticServiceManagerAdded().remove(eventListener);
        getElasticServiceManagerRemoved().remove(eventListener);        
    }

    @Override
    public boolean waitFor(int numberOfElasticServiceManagers) {
        return waitFor(numberOfElasticServiceManagers, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
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

    @Override
    public ElasticServiceManager waitForAtLeastOne() {
        return waitForAtLeastOne(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
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

    @Override
    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    @Override
    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor)
            throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (ElasticServiceManager esm : this) {
            dumpResult.add(esm.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    
    protected void assertStateChangesPermitted() {
        this.admin.assertStateChangesPermitted();
    }

    @Override
    public ElasticServiceManager[] getManagersNonFiltered() {

        return elasticServiceManagersByUID.values().toArray(new ElasticServiceManager[0]);
    }
    
    private boolean accept( InternalElasticServiceManager elasticServiceManager ){
        
        if( elasticServiceManager == null ){
            throw new IllegalArgumentException("elasticServiceManager cannot be null");
        }
        
        JVMDetails jvmDetails = elasticServiceManager.getJVMDetails();
        boolean isAcceptJvm = AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails );
        
        return isAcceptJvm;
    }    
}
