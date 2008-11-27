package org.openspaces.pu.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A simple implementation of {@link ProcessingUnitServiceDetails}.
 *
 * @author kimchy
 */
public class PlainProcessingUnitServiceDetails implements ProcessingUnitServiceDetails, Externalizable {

    private String id;

    private String serviceType;

    private String type;

    private String description;

    private String longDescription;

    // Just for externalizable
    public PlainProcessingUnitServiceDetails() {
    }

    public PlainProcessingUnitServiceDetails(String id, String serviceType, String type, String description, String longDescription) {
        this.id = id;
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

    public String getId() {
        return this.id;
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
        out.writeUTF(id);
        out.writeUTF(serviceType);
        out.writeUTF(type);
        out.writeUTF(description);
        out.writeUTF(longDescription);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        serviceType = in.readUTF();
        type = in.readUTF();
        description = in.readUTF();
        longDescription = in.readUTF();
    }
}
