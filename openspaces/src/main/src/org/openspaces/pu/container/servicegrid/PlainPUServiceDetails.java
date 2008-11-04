package org.openspaces.pu.container.servicegrid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A simple implementation of {@link org.openspaces.pu.container.servicegrid.PUServiceDetails}.
 *
 * @author kimchy
 */
public class PlainPUServiceDetails implements PUServiceDetails, Externalizable {

    private String serviceType;

    private String type;

    private String description;

    private String longDescription;

    // Just for externalizable
    public PlainPUServiceDetails() {
    }

    public PlainPUServiceDetails(String serviceType, String type, String description, String longDescription) {
        this.serviceType = serviceType;
        this.type = type;
        this.description = description;
        if (this.description == null) {
            this.description = "";
        }
        this.longDescription = longDescription;
        if (this.longDescription == null) {
            this.longDescription = "";
        }
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLongDescription() {
        return this.longDescription;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(serviceType);
        out.writeUTF(type);
        out.writeUTF(description);
        out.writeUTF(longDescription);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serviceType = in.readUTF();
        type = in.readUTF();
        description = in.readUTF();
        longDescription = in.readUTF();
    }
}
