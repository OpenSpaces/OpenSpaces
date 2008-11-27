package org.openspaces.pu.container.servicegrid;

import net.jini.core.lookup.ServiceID;
import org.openspaces.core.cluster.ClusterInfo;

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

    public PUDetails() {
    }

    public PUDetails(ServiceID gscServiceID, ClusterInfo clusterInfo) {
        this.gscServiceID = gscServiceID;
        this.clusterInfo = clusterInfo;
    }

    public ServiceID getGscServiceID() {
        return gscServiceID;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gscServiceID);
        out.writeObject(clusterInfo);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        gscServiceID = (ServiceID) in.readObject();
        clusterInfo = (ClusterInfo) in.readObject();
    }
}
