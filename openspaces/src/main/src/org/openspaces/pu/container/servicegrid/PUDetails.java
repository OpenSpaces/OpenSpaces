package org.openspaces.pu.container.servicegrid;

import net.jini.core.lookup.ServiceID;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.service.ServiceDetails;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kimchy
 */
public class PUDetails implements Externalizable {

    private ServiceID gscServiceID;

    private ClusterInfo clusterInfo;

    private ServiceDetails[] details;

    public PUDetails() {
    }

    public PUDetails(ServiceID gscServiceID, ClusterInfo clusterInfo, ServiceDetails[] details) {
        this.gscServiceID = gscServiceID;
        this.clusterInfo = clusterInfo;
        this.details = details;
        if (details == null) {
            details = new ServiceDetails[0];
        }
    }

    public ServiceID getGscServiceID() {
        return gscServiceID;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public ServiceDetails[] getDetails() {
        return details;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gscServiceID);
        out.writeObject(clusterInfo);
        out.writeInt(details.length);
        for (ServiceDetails details : this.details) {
            out.writeObject(details);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        gscServiceID = (ServiceID) in.readObject();
        clusterInfo = (ClusterInfo) in.readObject();
        int size = in.readInt();
        this.details = new ServiceDetails[size];
        for (int i = 0; i < size; i++) {
            details[i] = (ServiceDetails) in.readObject();
        }
    }
}
