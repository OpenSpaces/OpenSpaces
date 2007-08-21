/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.hibernate.cache;

import com.j_spaces.core.client.ISpaceProxy;
import com.j_spaces.core.client.LocalTransactionManager;
import com.j_spaces.core.client.XAResourceImpl;
import com.j_spaces.map.Envelope;
import com.j_spaces.map.IMap;
import net.jini.core.transaction.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.util.Map;

/**
 * Transaction map cache implements Hibenrate second level cache transactionally by
 * integrating with JTA.
 *
 * @author kimchy
 */
public class TransactionalMapCache implements Cache {

    private String regionName;

    private IMap map;

    private TransactionManager transactionManager;

    private LocalTransactionManager localTransactionManager;

    private ISpaceProxy masterSpace;

    public TransactionalMapCache(String regionName, IMap map, TransactionManager transactionManager,
                                 LocalTransactionManager localTransactionManager) {
        this.regionName = regionName;
        this.map = map;
        this.transactionManager = transactionManager;
        this.localTransactionManager = localTransactionManager;
        this.masterSpace = (ISpaceProxy) map.getMasterSpace();
    }

    /**
     * Get an item from the cache
     */
    public Object read(Object key) throws CacheException {
        verifyTransaction();
        return map.get(new CacheKey(regionName, key));
    }

    /**
     * Get an item from the cache, nontransactionally
     */
    public Object get(Object key) throws CacheException {
        Transaction.Created tx = masterSpace.getContextTransaction();
        try {
            masterSpace.setContextTansaction(null);
            return map.get(new CacheKey(regionName, key));
        } finally {
            masterSpace.setContextTansaction(tx);
        }
    }

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     */
    public void put(Object key, Object value) throws CacheException {
        Transaction.Created tx = masterSpace.getContextTransaction();
        try {
            masterSpace.setContextTansaction(null);
            map.put(new CacheKey(regionName, key), value);
        } finally {
            masterSpace.setContextTansaction(tx);
        }
    }

    /**
     * Add an item to the cache
     */
    public void update(Object key, Object value) throws CacheException {
        verifyTransaction();
        map.put(new CacheKey(regionName, key), value);
    }

    /**
     * Remove an item from the cache
     */
    public void remove(Object key) throws CacheException {
        verifyTransaction();
        map.remove(new CacheKey(regionName, key));
    }

    /**
     * Clear the cache
     */
    public void clear() throws CacheException {
        // TODO we only need to clear the specific region
        verifyTransaction();
        map.clear();
        try {
            map.getMasterSpace().clear(new Envelope(new CacheKey(regionName, null), null), null);
        } catch (Exception e) {
            throw new CacheException("Failed to clear master space with region [" + regionName + "]", e);
        }
    }

    /**
     * Clean up
     */
    public void destroy() throws CacheException {

    }

    /**
     * If this is a clustered cache, lock the item
     */
    public void lock(Object key) throws CacheException {
        // no need to implement, since it should be transactional
    }

    /**
     * If this is a clustered cache, unlock the item
     */
    public void unlock(Object key) throws CacheException {
        // no need to implement, since it should be transactional
    }

    /**
     * Generate a timestamp
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * Get a reasonable "lock timeout"
     */
    public int getTimeout() {
        return Timestamper.ONE_MS * 60000;
    }

    /**
     * Get the name of the cache region
     */
    public String getRegionName() {
        return this.regionName;
    }

    /**
     * The number of bytes is this cache region currently consuming in memory.
     *
     * @return The number of bytes consumed by this region; -1 if unknown or
     *         unsupported.
     */
    public long getSizeInMemory() {
        return -1;
    }

    /**
     * The count of entries currently contained in the regions in-memory store.
     *
     * @return The count of entries in memory; -1 if unknown or unsupported.
     */
    public long getElementCountInMemory() {
        // we need to count as per the region
        return -1;
    }

    /**
     * The count of entries currently contained in the regions disk store.
     *
     * @return The count of entries on disk; -1 if unknown or unsupported.
     */
    public long getElementCountOnDisk() {
        return -1;
    }

    /**
     * optional operation
     */
    public Map toMap() {
        return map;
    }

    private void verifyTransaction() {
        if (masterSpace.getContextTransaction() == null) {
            XAResource xaResourceSpace = new XAResourceImpl(localTransactionManager, masterSpace);
            try {
                transactionManager.getTransaction().enlistResource(xaResourceSpace);
            } catch (Exception e) {
                throw new TransactionException("Failed to enlist Space resource with JTA transaction manager", e);
            }
        }
    }
}