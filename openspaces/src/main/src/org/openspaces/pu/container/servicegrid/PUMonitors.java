package org.openspaces.pu.container.servicegrid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kimchy
 */
public class PUMonitors implements Externalizable {

    private long timestamp;

    private Object[] monitors;

    public PUMonitors() {
    }

    public PUMonitors(Object[] monitors) {
        this.timestamp = System.currentTimeMillis();
        this.monitors = monitors;
    }

    public Object[] getMonitors() {
        return monitors;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(timestamp);
        out.writeInt(monitors.length);
        for (Object monitor : this.monitors) {
            out.writeObject(monitor);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        timestamp = in.readLong();
        monitors = new Object[in.readInt()];
        for (int i = 0; i < monitors.length; i++) {
            monitors[i] = in.readObject();
        }
    }
}
