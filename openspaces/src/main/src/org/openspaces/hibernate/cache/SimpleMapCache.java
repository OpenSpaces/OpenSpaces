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

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.cache.map.MapCache;
import com.j_spaces.map.Envelope;
import com.j_spaces.map.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;
import org.openspaces.core.map.LockHandle;
import org.openspaces.core.map.LockManager;

import java.util.Map;

/**
 * Simple map cache implements Hibenrate second level cache non-transactionally. Supports
 * concurrency strategies of <code>read-only</code>, <code>read-write</code>.
 *
 * @author kimchy
 */
public class SimpleMapCache implements Cache {

    final static private Log logger = LogFactory.getLog(SimpleMapCache.class);

    final private String regionName;

    final private IMap map;

    final private long timeToLive;

    final private long waitForResponse;

    final private LockManager lockManager;

    private static final ThreadLocal<LockHandle> lockHandlerContext = new ThreadLocal<LockHandle>();

    public SimpleMapCache(String regionName, IMap map, long timeToLive, long waitForResponse) {
        this.regionName = regionName;
        this.map = map;
        this.lockManager = new LockManager(map);
        this.timeToLive = timeToLive;
        this.waitForResponse = waitForResponse;
    }

    /**
     * Get an item from the cache
     */
    public Object read(Object key) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        LockHandle lockHandle = lockHandlerContext.get();
        if (lockHandle != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Read [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            }
            Object retVal = map.get(cacheKey, lockHandle.getTransaction(), waitForResponse, ReadModifiers.REPEATABLE_READ);
            if (retVal instanceof String && ((String) retVal).length() == 0) {
                // this is a null value put there as a marker for the lock
                // since there was no entry for this mentioned key when tried to lock
                return null;
            }
            return retVal;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Read [" + cacheKey + "]");
            }
            return map.get(cacheKey, waitForResponse);
        }
    }

    /**
     * Get an item from the cache, nontransactionally
     */
    public Object get(Object key) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        LockHandle lockHandle = lockHandlerContext.get();
        if (lockHandle != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Get [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            }
            Object retVal = map.get(cacheKey, lockHandle.getTransaction(), waitForResponse, ReadModifiers.REPEATABLE_READ);
            if (retVal instanceof String && ((String) retVal).length() == 0) {
                // this is a null value put there as a marker for the lock
                // since there was no entry for this mentioned key when tried to lock
                return null;
            }
            return retVal;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Get [" + cacheKey + "]");
            }
            return map.get(cacheKey, waitForResponse);
        }
    }

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     */
    public void put(Object key, Object value) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        LockHandle lockHandle = lockHandlerContext.get();
        if (lockHandle != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Put [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            }
            map.put(cacheKey, value, lockHandle.getTransaction(), timeToLive, waitForResponse);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Put [" + cacheKey + "]");
            }
            map.put(cacheKey, value, timeToLive, waitForResponse);
        }
    }

    /**
     * Add an item to the cache
     */
    public void update(Object key, Object value) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        LockHandle lockHandle = lockHandlerContext.get();
        if (lockHandle != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Update [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            }
            map.put(cacheKey, value, lockHandle.getTransaction(), timeToLive, waitForResponse);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Update [" + cacheKey + "]");
            }
            map.put(cacheKey, value, timeToLive, waitForResponse);
        }
    }

    /**
     * Remove an item from the cache
     */
    public void remove(Object key) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        LockHandle lockHandle = lockHandlerContext.get();
        if (lockHandle != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Remove [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            }
            map.remove(cacheKey, lockHandle.getTransaction(), waitForResponse);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Remove [" + cacheKey + "]");
            }
            map.remove(cacheKey, waitForResponse);
        }
    }

    /**
     * Clear the cache
     */
    public void clear() throws CacheException {
        if (logger.isTraceEnabled()) {
            logger.trace("Clearing region [" + regionName + "]");
        }
        try {
            map.getMasterSpace().clear(new Envelope(new CacheKey(regionName, null), null), null);
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
        CacheKey cacheKey = new CacheKey(regionName, key);
        if (logger.isTraceEnabled()) {
            logger.trace("Trying to lock [" + cacheKey + "]");
        }
        LockHandle lockHandle = lockManager.lock(cacheKey, getTimeout(), getTimeout());
        if (logger.isTraceEnabled()) {
            logger.trace("Lock [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
        }
        lockHandlerContext.set(lockHandle);
    }

    /**
     * If this is a clustered cache, unlock the item
     */
    public void unlock(Object key) throws CacheException {
        CacheKey cacheKey = new CacheKey(regionName, key);
        if (logger.isTraceEnabled()) {
            LockHandle lockHandle = lockHandlerContext.get();
            if (lockHandle != null) {
                logger.trace("Unlock [" + cacheKey + "] under a lock [" + lockHandle.getTransaction() + "]");
            } else {
                logger.trace("Unlock [" + cacheKey + "] not under lock transaction, might be due to an internal bug");
            }
        }
        lockHandlerContext.remove();
        lockManager.unlock(cacheKey);
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
}