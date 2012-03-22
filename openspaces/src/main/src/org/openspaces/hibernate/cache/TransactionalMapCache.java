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

import java.util.Map;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import net.jini.core.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.TransactionException;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.client.XAResourceImpl;
import com.j_spaces.core.client.cache.map.MapCache;
import com.j_spaces.map.IMap;
import com.j_spaces.map.MapEntryFactory;

/**
 * Transaction map cache implements Hibenrate second level cache transactionally by
 * integrating with JTA.
 *
 * @author kimchy
 */
public class TransactionalMapCache implements Cache {

    private Log logger = LogFactory.getLog(getClass());
    
    private String regionName;

    private IMap map;

    private long timeToLive;

    private long waitForResponse;

    private TransactionManager transactionManager;

    private net.jini.core.transaction.server.TransactionManager distributedTransactionManager;

    private ISpaceProxy masterSpace;

    public TransactionalMapCache(String regionName, IMap map, long timeToLive, long waitForResponse,
                                 TransactionManager transactionManager, net.jini.core.transaction.server.TransactionManager distributedTransactionManager) {
        this.regionName = regionName;
        this.map = map;
        this.timeToLive = timeToLive;
        this.waitForResponse = waitForResponse;
        this.transactionManager = transactionManager;
        this.distributedTransactionManager = distributedTransactionManager;
        this.masterSpace = (ISpaceProxy) map.getMasterSpace();
    }

    /**
     * Get an item from the cache
     */
    public Object read(Object key) throws CacheException {
        verifyTransaction();
        CacheKey cacheKey = new CacheKey(regionName, key);
        if (logger.isTraceEnabled()) {
            logger.trace("Read [" + cacheKey + "] under transaction [" + masterSpace.getContextTransaction() + "]");
        }
        return map.get(cacheKey, waitForResponse);
    }

    /**
     * Get an item from the cache, nontransactionally
     */
    public Object get(Object key) throws CacheException {
        Transaction.Created tx = masterSpace.replaceContextTransaction(null);
        try {
            CacheKey cacheKey = new CacheKey(regionName, key);
            if (logger.isTraceEnabled()) {
                logger.trace("Get [" + cacheKey + "] under no transaction");
            }
            return map.get(cacheKey);
        } finally {
            masterSpace.replaceContextTransaction(tx);
        }
    }

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     */
    public void put(Object key, Object value) throws CacheException {
        Transaction.Created tx = masterSpace.replaceContextTransaction(null);
        try {
            CacheKey cacheKey = new CacheKey(regionName, key);
            if (logger.isTraceEnabled()) {
                logger.trace("Put [" + cacheKey + "] under no transaction");
            }
            map.put(cacheKey, value, timeToLive, waitForResponse);
        } finally {
            masterSpace.replaceContextTransaction(tx);
        }
    }

    /**
     * Add an item to the cache
     */
    public void update(Object key, Object value) throws CacheException {
        verifyTransaction();
        CacheKey cacheKey = new CacheKey(regionName, key);
        if (logger.isTraceEnabled()) {
            logger.trace("Update [" + cacheKey + "] under transaction [" + masterSpace.getContextTransaction() + "]");
        }
        map.put(cacheKey, value, timeToLive, waitForResponse);
    }

    /**
     * Remove an item from the cache
     */
    public void remove(Object key) throws CacheException {
        verifyTransaction();
        CacheKey cacheKey = new CacheKey(regionName, key);
        if (logger.isTraceEnabled()) {
            logger.trace("Remove [" + cacheKey + "] under transaction [" + masterSpace.getContextTransaction() + "]");
        }
        map.remove(cacheKey, waitForResponse);
    }

    /**
     * Clear the cache
     */
    public void clear() throws CacheException {
        verifyTransaction();
        if (logger.isTraceEnabled()) {
            logger.trace("Clearing region [" + regionName + "]");
        }
        try {
            map.getMasterSpace().clear(MapEntryFactory.create(new CacheKey(regionName, null), null), null);
        } catch (Exception e) {
            throw new CacheException("Failed to clear master space with region [" + regionName + "]", e);
        }
        // only clean (non master) map cache, since GSMapImpl calls clean on the remote space (all of it)
        // and we call clear afterwards
        if (map instanceof MapCache) {
            map.clear();
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
            XAResource xaResourceSpace = new XAResourceImpl(distributedTransactionManager, masterSpace);
            try {
                transactionManager.getTransaction().enlistResource(xaResourceSpace);
            } catch (Exception e) {
                throw new TransactionException("Failed to enlist Space resource with JTA transaction manager", e);
            }
        }
    }
}