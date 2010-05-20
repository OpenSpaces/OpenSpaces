package org.openspaces.memcached;

import java.io.Serializable;

/**
 */
public interface CacheElement extends Serializable {
    public final static int THIRTY_DAYS = 2592000;

    int size();

    int hashCode();

    int getExpire();

    int getFlags();

    byte[] getData();

    void setData(byte[] data);

    Key getKey();

    long getCasUnique();

    void setCasUnique(long casUnique);

    CacheElement append(LocalCacheElement element);

    CacheElement prepend(LocalCacheElement element);

    LocalCacheElement.IncrDecrResult add(int mod);
}