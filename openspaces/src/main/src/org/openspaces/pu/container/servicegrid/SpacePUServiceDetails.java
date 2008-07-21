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

    private String name;

    private String containerName;

    private ServiceID serviceID;

    private SpaceMode spaceMode;

    public SpacePUServiceDetails(String name, String containerName, ServiceID serviceID, SpaceMode spaceMode) {
        this.name = name;
        this.containerName = containerName;
        this.serviceID = serviceID;
        this.spaceMode = spaceMode;
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

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(containerName);
        out.writeLong(serviceID.getMostSignificantBits());
        out.writeLong(serviceID.getLeastSignificantBits());
        out.writeObject(spaceMode);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        containerName = in.readUTF();
        serviceID = new ServiceID(in.readLong(), in.readLong());
        spaceMode = (SpaceMode) in.readObject();
    }
}
