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

import org.openspaces.core.GigaMap;

import java.util.Map;

/**
 * A hibernate second level cache implementation using {@link org.openspaces.core.GigaMap}
 * that works with no transactions. Supports concurrency strategies of <code>read-only</code>,
 * <code>read-write</code>.
 *
 * @author kimchy
 */
public class SimpleGigaMapCache extends SimpleMapCache {

    private GigaMap gigaMap;

    public SimpleGigaMapCache(String regionName, GigaMap gigaMap) {
        super(regionName, gigaMap.getMap(), gigaMap.getDefaultTimeToLive(), gigaMap.getDefaultWaitForResponse());
        this.gigaMap = gigaMap;
    }

    /**
     * optional operation
     */
    public Map toMap() {
        return gigaMap;
    }
}