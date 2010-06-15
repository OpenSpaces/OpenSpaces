package org.openspaces.pu.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple implementation of {@link ServiceMonitors}.
 *
 * @author kimchy
 */
public class PlainServiceMonitors implements ServiceMonitors, Externalizable {

    protected String id;

    protected ServiceDetails details;

    final protected Map<String, Object> monitors = new LinkedHashMap<String, Object>();

    // Just for externalizable
    public PlainServiceMonitors() {
    }

    public PlainServiceMonitors(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Map<String, Object> getMonitors() {
        return this.monitors;
    }

    public ServiceDetails getDetails() {
        return details;
    }

    public void setDetails(ServiceDetails details) {
        this.details = details;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        if (monitors == null) {
            out.writeInt(0);
        } else {
            out.writeInt(monitors.size());
            for (Map.Entry<String, Object> entry : monitors.entrySet()) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        int attributesSize = in.readInt();
        for (int i = 0; i < attributesSize; i++) {
            String key = (String) in.readObject();
            Object value = in.readObject();
            monitors.put(key, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id[").append(id).append("] ");
        for (Map.Entry<String, Object> entry : monitors.entrySet()) {
            sb.append(entry.getKey()).append("[").append(entry.getValue()).append("] ");
        }
        return sb.toString();
    }
}