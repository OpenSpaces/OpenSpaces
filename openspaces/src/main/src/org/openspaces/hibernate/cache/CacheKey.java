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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A cache key object acting as the key for the cache.
 *
 * @author kimchy
 */
public class CacheKey implements Externalizable {

    private String regionName;

    private Object key;

    private int hashCode = Integer.MIN_VALUE;

    public CacheKey() {
        
    }

    public CacheKey(String regionName, Object key) {
        this.regionName = regionName;
        this.key = key;
    }

    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = 7 * regionName.hashCode();
            if (key != null) {
                hashCode += key.hashCode();
            }
        }
        return hashCode;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CacheKey)) {
            return false;
        }

        CacheKey otherCacheKey = (CacheKey) other;
        if (otherCacheKey.regionName.equals(regionName)) {
            if(otherCacheKey.key == key){
                return true;
            }
            if (key == null || otherCacheKey.key == null) {
                return false;
            }
            
            return otherCacheKey.key.equals(key);
        }
        return false;
    }

    public String toString() {
        return "CacheKey region[" + regionName + "] key [" + key + "]";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(regionName);
        out.writeObject(key);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        regionName = in.readUTF();
        key = in.readObject();
    }
}
