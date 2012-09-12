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

import com.j_spaces.map.MapEntryFactory;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;
import org.openspaces.core.GigaMap;

import java.util.Map;

/**
 * A hibernate second level cache implemenation using {@link org.openspaces.core.GigaMap}.
 *
 * <p>A note on transactions: Since {@link org.openspaces.core.GigaMap} automatically joins
 * on going Spring mangaged transactions, it will work in a non transactional environment,
 * a Space local transaction managed environment, and a JTA one automatically. Make sure
 * to configure the second level cache accordingly within Hibernate.
 *
 * @author kimchy
 */
public class TransactionalGigaMapCache implements Cache {

    private String regionName;

    private GigaMap gigaMap;

    public TransactionalGigaMapCache(String regionName, GigaMap gigaMap) {
        this.regionName = regionName;
        this.gigaMap = gigaMap;
    }

    /**
     * Get an item from the cache
     */
    public Object read(Object key) throws CacheException {
        return gigaMap.get(new CacheKey(regionName, key));
    }

    /**
     * Get an item from the cache, nontransactionally
     */
    public Object get(Object key) throws CacheException {
        // explicltly use the actual map to perfrom the operation outside of a transaction
        return gigaMap.getMap().get(new CacheKey(regionName, key));
    }

    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     */
    public void put(Object key, Object value) throws CacheException {
        // explicltly use the actual map to perfrom the operation outside of a transaction
        gigaMap.getMap().put(new CacheKey(regionName, key), value);
    }

    /**
     * Add an item to the cache
     */
    public void update(Object key, Object value) throws CacheException {
        gigaMap.put(new CacheKey(regionName, key), value);
    }

    /**
     * Remove an item from the cache
     */
    public void remove(Object key) throws CacheException {
        gigaMap.remove(new CacheKey(regionName, key));
    }

    /**
     * Clear the cache
     */
    public void clear() throws CacheException {
        gigaMap.clear();
        try {
            gigaMap.getMap().getMasterSpace().clear(MapEntryFactory.create(new CacheKey(regionName, null), null), null);
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
        return gigaMap;
    }
}
