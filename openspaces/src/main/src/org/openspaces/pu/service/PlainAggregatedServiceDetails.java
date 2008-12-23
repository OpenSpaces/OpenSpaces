package org.openspaces.pu.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple and generic implementation of the {@link org.openspaces.pu.service.AggregatedServiceDetails}
 * interface.
 *
 * @author kimchy
 */
public class PlainAggregatedServiceDetails implements AggregatedServiceDetails, Externalizable {

    private String serviceType;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Just for externalizable.
     */
    public PlainAggregatedServiceDetails() {
    }

    public PlainAggregatedServiceDetails(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(serviceType);
        out.writeInt(attributes.size());
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serviceType = in.readUTF();
        attributes = new HashMap<String, Object>();
        int attributesSize = in.readInt();
        for (int i = 0; i < attributesSize; i++) {
            String key = (String) in.readObject();
            Object value = in.readObject();
            attributes.put(key, value);
        }
    }
}
