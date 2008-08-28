package org.openspaces.pu.container.servicegrid;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import net.jini.core.lookup.ServiceID;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A Space service defined within a processing unit.
 *
 * @author kimchy
 */
public class SpacePUServiceDetails implements PUServiceDetails, Externalizable {

    private static final long serialVersionUID = 1L;
    
    private String name;

    private String containerName;

    private ServiceID serviceID;

    private SpaceMode spaceMode;

    // can be "embedded", "localview", "localcache", "remote"
    private String type;

    public SpacePUServiceDetails() {
    }

    public SpacePUServiceDetails(String name, String containerName, ServiceID serviceID, SpaceMode spaceMode, String type) {
        this.name = name;
        this.containerName = containerName;
        this.serviceID = serviceID;
        this.spaceMode = spaceMode;
        this.type = type;
    }

    public String getServiceType() {
        return "space";
    }

    public String getDescription() {
        return containerName + ":" + name;
    }

    public String getName() {
        return name;
    }

    public String getContainerName() {
        return containerName;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public SpaceMode getSpaceMode() {
        return spaceMode;
    }

    public String getType() {
        return type;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(containerName);
        out.writeLong(serviceID.getMostSignificantBits());
        out.writeLong(serviceID.getLeastSignificantBits());
        out.writeObject(spaceMode);
        out.writeUTF(type);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        containerName = in.readUTF();
        serviceID = new ServiceID(in.readLong(), in.readLong());
        spaceMode = (SpaceMode) in.readObject();
        type = in.readUTF();
    }
}
