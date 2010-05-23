package org.openspaces.memcached;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * Represents a given key for lookup in the cache.
 *
 * Wraps a byte array with a precomputed hashCode.
 */
public class Key implements Externalizable {

    public byte[] bytes;
    
    private transient int hashCode;

    public Key() {
    }

    public Key(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Key key1 = (Key) o;
        return Arrays.equals(bytes, key1.bytes);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private static final ThreadLocal<StringBuilder> sbCache = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }
    };

    @Override
    public String toString() {
        StringBuilder sb = sbCache.get();
        sb.setLength(0);
        for (byte b : bytes) {
            sb.append(b);
        }
        return sb.toString();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        bytes = new byte[in.readInt()];
        in.readFully(bytes);
        this.hashCode = Arrays.hashCode(bytes);
    }
}