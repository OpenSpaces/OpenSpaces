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

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;
import org.openspaces.core.GigaMap;

import java.util.Properties;

/**
 * An Open Spaces Hibernate second level cache provider using the {@link org.openspaces.core.GigaMap}
 * interface. Allowing to simplify the usage of Hibernate second level cache when
 * using Spring based applications.
 *
 * <p>Relies on the externally constructed {@link org.openspaces.core.GigaMap} which
 * is then passed to this cache provider with thread local context. Before Hibernate
 * is configured within Spring, a bean with this cache provider should configured
 * calling {@link #setMapContext(org.openspaces.core.GigaMap)}.
 *
 * <p>Builds {@link SimpleGigaMapCache} as the cache implementation.
 *
 * @author kimchy
 */
public class SimpleGigaMapCacheProvider implements CacheProvider {

    private static ThreadLocal<GigaMap> mapContext = new ThreadLocal<GigaMap>();

    private GigaMap gigaMap;

    public static void setMapContext(GigaMap gigaMap) {
        mapContext.set(gigaMap);
    }

    public void setGigaMap(GigaMap gigaMap) {
        this.gigaMap = gigaMap;
    }

    public void start(Properties properties) throws CacheException {
        if (gigaMap == null) {
            gigaMap = mapContext.get();
            if (gigaMap == null) {
                throw new CacheException("gigaMap not found in thread context, have you configured to set its conetxt");
            }
            mapContext.set(null);
        }
    }

    public void stop() {
    }

    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return new SimpleGigaMapCache(regionName, gigaMap);
    }

    public long nextTimestamp() {
        return Timestamper.next();
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }
}