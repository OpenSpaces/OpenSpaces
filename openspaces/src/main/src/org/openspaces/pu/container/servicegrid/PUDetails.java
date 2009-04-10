package org.openspaces.pu.container.servicegrid;

import net.jini.core.lookup.ServiceID;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
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

    private BeanLevelProperties beanLevelProperties;

    private Object[] details;

    public PUDetails() {
    }

    public PUDetails(ServiceID gscServiceID, ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties, Object[] details) {
        this.gscServiceID = gscServiceID;
        this.clusterInfo = clusterInfo;
        this.beanLevelProperties = beanLevelProperties;
        this.details = details;
        if (details == null) {
            this.details = new ServiceDetails[0];
        }
    }

    public ServiceID getGscServiceID() {
        return gscServiceID;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public BeanLevelProperties getBeanLevelProperties() {
        return beanLevelProperties;
    }

    public Object[] getDetails() {
        return this.details;
    }

    /**
     * Return the name representing this Processing Unit (as shown in the UI).
     * 
     * @return <tt>service-name</tt>.<tt>instance-id</tt> [<tt>backup-id</tt>] or
     *         <tt>service-name</tt> [<tt>instance-id</tt>]
     */
    public String getPresentationName() {
        String name = "null";
        if (clusterInfo != null) {
            name = clusterInfo.getName();
            Integer id = clusterInfo.getInstanceId();
            if (clusterInfo.getNumberOfBackups() > 0) {
                Integer bid = clusterInfo.getBackupId();
                if (bid == null) {
                    bid = Integer.valueOf(0);
                }
                name += "."+id+" ["+(bid+1)+"]";
            } else {
                name += " ["+id+"]";
            }
        }
        return name;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gscServiceID);
        out.writeObject(clusterInfo);
        out.writeInt(details.length);
        for (Object details : this.details) {
            out.writeObject(details);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        gscServiceID = (ServiceID) in.readObject();
        clusterInfo = (ClusterInfo) in.readObject();
        int size = in.readInt();
        this.details = new ServiceDetails[size];
        for (int i = 0; i < size; i++) {
            details[i] = in.readObject();
        }
    }
}
