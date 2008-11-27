package org.openspaces.admin.internal.pu;

import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.servicegrid.PUServiceBean;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstance implements InternalProcessingUnitInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puServiceBean;

    private final ClusterInfo clusterInfo;

    private volatile ProcessingUnit processingUnit;

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUServiceBean puServiceBean, ClusterInfo clusterInfo) {
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puServiceBean = puServiceBean;
        this.clusterInfo = clusterInfo;
    }

    public String getUID() {
        return this.uid;
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public ProcessingUnit getProcessingUnit() {
        return this.processingUnit;
    }

    public void setProcessingUnit(ProcessingUnit processingUnit) {
        this.processingUnit = processingUnit;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }
}
