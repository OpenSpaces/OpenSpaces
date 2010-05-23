package org.openspaces.memcached;

import com.gigaspaces.client.IReadByIdsResult;
import com.j_spaces.core.client.UpdateModifiers;
import org.openspaces.core.EntryAlreadyInSpaceException;
import org.openspaces.core.EntryNotInSpaceException;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceOptimisticLockingFailureException;
import org.openspaces.memcached.util.BufferUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.valueOf;

/**
 * @author kimchy (shay.banon)
 */
public class SpaceCache implements Cache {

    private final GigaSpace space;

    protected final AtomicLong started = new AtomicLong();
    protected final AtomicLong getCmds = new AtomicLong();
    protected final AtomicLong setCmds = new AtomicLong();
    protected final AtomicLong getHits = new AtomicLong();
    protected final AtomicLong getMisses = new AtomicLong();

    public SpaceCache(GigaSpace space) {
        this.space = space;
        started.set(System.currentTimeMillis());
    }

    public DeleteResponse delete(Key key, int time) {
        if (time > 0) {
            // expire it later, removed from memcached, buts lets do it anyhow (but we do not block it!)
            MemcachedEntry entry = space.readById(MemcachedEntry.class, key);
            if (entry == null) {
                return DeleteResponse.NOT_FOUND;
            }
            space.write(entry, time);
            return DeleteResponse.DELETED;
        } else {
            MemcachedEntry entry = space.takeById(MemcachedEntry.class, key);
            return entry == null ? DeleteResponse.NOT_FOUND : DeleteResponse.DELETED;
        }
    }

    public StoreResponse add(CacheElement e) {
        try {
            MemcachedEntry entry = new MemcachedEntry(e.getKey(), e.getData());
            entry.setFlags(e.getFlags());
            space.write(entry, e.getExpire(), 0, UpdateModifiers.WRITE_ONLY);
            return StoreResponse.STORED;
        } catch (EntryAlreadyInSpaceException e1) {
            return StoreResponse.NOT_FOUND;
        }
    }

    public StoreResponse replace(CacheElement e) {
        try {
            MemcachedEntry entry = new MemcachedEntry(e.getKey(), e.getData());
            entry.setFlags(e.getFlags());
            space.write(entry, e.getExpire(), 0, UpdateModifiers.UPDATE_ONLY);
            return StoreResponse.STORED;
        } catch (EntryNotInSpaceException e1) {
            return StoreResponse.NOT_FOUND;
        }
    }

    public StoreResponse append(CacheElement cacheElement) {
        // binary protocol allows to pass cas value, take it into account?
        while (true) {
            MemcachedEntry entry = space.readById(MemcachedEntry.class, cacheElement.getKey());
            if (entry == null) {
                getMisses.incrementAndGet();
                return StoreResponse.NOT_FOUND;
            }
            byte[] newData = new byte[entry.getValue().length + cacheElement.getData().length];
            System.arraycopy(entry.getValue(), 0, newData, 0, entry.getValue().length);
            System.arraycopy(cacheElement.getData(), 0, newData, entry.getValue().length, cacheElement.getData().length);
            entry.setValue(newData);
            try {
                space.write(entry);
            } catch (SpaceOptimisticLockingFailureException e) {
                continue;
            }
            return StoreResponse.STORED;
        }
    }

    public StoreResponse prepend(CacheElement cacheElement) {
        // binary protocol allows to pass cas value, take it into account?
        while (true) {
            MemcachedEntry entry = space.readById(MemcachedEntry.class, cacheElement.getKey());
            if (entry == null) {
                getMisses.incrementAndGet();
                return StoreResponse.NOT_FOUND;
            }
            byte[] newData = new byte[entry.getValue().length + cacheElement.getData().length];
            System.arraycopy(cacheElement.getData(), 0, newData, 0, cacheElement.getData().length);
            System.arraycopy(entry.getValue(), 0, newData, cacheElement.getData().length, entry.getValue().length);
            entry.setValue(newData);
            try {
                space.write(entry);
            } catch (SpaceOptimisticLockingFailureException e) {
                continue;
            }
            return StoreResponse.STORED;
        }
    }

    public StoreResponse set(CacheElement e) {
        setCmds.incrementAndGet();//update stats
        MemcachedEntry entry = new MemcachedEntry(e.getKey(), e.getData());
        entry.setFlags(e.getFlags());
        space.write(entry, e.getExpire());
        return StoreResponse.STORED;
    }

    public StoreResponse cas(Long cas_key, CacheElement e) {
        try {
            MemcachedEntry entry = new MemcachedEntry(e.getKey(), e.getData());
            entry.setFlags(e.getFlags());
            entry.setVersion(cas_key.intValue());
            space.write(entry, e.getExpire(), 0, UpdateModifiers.UPDATE_ONLY);
            return StoreResponse.STORED;
        } catch (SpaceOptimisticLockingFailureException e1) {
            return StoreResponse.EXISTS;
        } catch (EntryNotInSpaceException e1) {
            getMisses.incrementAndGet();
            return StoreResponse.NOT_FOUND;
        }
    }

    public Integer get_add(Key key, int mod) {
        while (true) {
            MemcachedEntry entry = space.readById(MemcachedEntry.class, key);
            if (entry == null) {
                getMisses.incrementAndGet();
                return null;
            }
            int val = BufferUtils.atoi(entry.getValue()) + mod; // change value
            if (val < 0) {
                val = 0;

            } // check for underflow

            entry.setValue(BufferUtils.itoa(val));

            try {
                space.write(entry);
            } catch (SpaceOptimisticLockingFailureException e) {
                continue;
            }
            return val;
        }
    }

    public CacheElement[] get(Key... keys) {
        getCmds.incrementAndGet();//updates stats
        try {
            if (keys.length == 1) {
                MemcachedEntry entry = space.readById(MemcachedEntry.class, keys[0]);
                if (entry == null) {
                    getMisses.incrementAndGet();
                    return new CacheElement[]{null};
                } else {
                    getHits.incrementAndGet();
                    return new CacheElement[]{convert(entry)};
                }
            } else {
                int hits = 0;
                int misses = 0;
                CacheElement[] retVal = new CacheElement[keys.length];
                IReadByIdsResult<MemcachedEntry> result = space.readByIds(MemcachedEntry.class, keys);
                for (int i = 0; i < result.getResultsArray().length; i++) {
                    MemcachedEntry entry = result.getResultsArray()[i];
                    if (entry == null) {
                        misses++;
                        retVal[i] = null;
                    } else {
                        hits++;
                        retVal[i] = convert(entry);
                    }
                }
                getMisses.addAndGet(misses);
                getHits.addAndGet(hits);
                return retVal;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean flush_all() {
        return false;
    }

    public boolean flush_all(int expire) {
        return false;
    }

    public void close() throws IOException {
    }

    public long getCurrentItems() {
        return space.count(new MemcachedEntry());
    }

    public long getLimitMaxBytes() {
        return -1;
    }

    public long getCurrentBytes() {
        return -1;
    }

    public long getGetCmds() {
        return getCmds.get();
    }

    public long getSetCmds() {
        return setCmds.get();
    }

    public long getGetHits() {
        return getHits.get();
    }

    public long getGetMisses() {
        return getMisses.get();
    }

    public Map<String, Set<String>> stat(String arg) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

        // stats we know
        multiSet(result, "version", MemCacheDaemon.memcachedVersion);
        multiSet(result, "cmd_gets", valueOf(getGetCmds()));
        multiSet(result, "cmd_sets", valueOf(getSetCmds()));
        multiSet(result, "get_hits", valueOf(getGetHits()));
        multiSet(result, "get_misses", valueOf(getGetMisses()));
        multiSet(result, "time", valueOf(valueOf(System.currentTimeMillis())));
        multiSet(result, "uptime", valueOf(System.currentTimeMillis() - this.started.longValue()));
        multiSet(result, "cur_items", valueOf(this.getCurrentItems()));
        multiSet(result, "limit_maxbytes", valueOf(this.getLimitMaxBytes()));
        multiSet(result, "current_bytes", valueOf(this.getCurrentBytes()));
        multiSet(result, "free_bytes", valueOf(Runtime.getRuntime().freeMemory()));

        // Not really the same thing precisely, but meaningful nonetheless. potentially this should be renamed
        multiSet(result, "pid", valueOf(Thread.currentThread().getId()));

        // stuff we know nothing about; gets faked only because some clients expect this
        multiSet(result, "rusage_user", "0:0");
        multiSet(result, "rusage_system", "0:0");
        multiSet(result, "connection_structures", "0");

        // TODO we could collect these stats
        multiSet(result, "bytes_read", "0");
        multiSet(result, "bytes_written", "0");

        return result;
    }

    private void multiSet(Map<String, Set<String>> map, String key, String val) {
        Set<String> cur = map.get(key);
        if (cur == null) {
            cur = new HashSet<String>();
        }
        cur.add(val);
        map.put(key, cur);
    }

    public void asyncEventPing() {
        // nothing to do here
    }

    private CacheElement convert(MemcachedEntry entry) throws UnsupportedEncodingException {
        LocalCacheElement element = new LocalCacheElement(entry.getKey(), entry.getFlags(), -1 /* not relevant, not sent back */, entry.getVersion());
        element.setData(entry.getValue());
        return element;
    }
}
