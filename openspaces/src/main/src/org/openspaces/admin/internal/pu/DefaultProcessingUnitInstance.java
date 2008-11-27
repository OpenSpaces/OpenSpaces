package org.openspaces.admin.internal.pu;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstance extends AbstractGridComponent implements InternalProcessingUnitInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puServiceBean;

    private final PUDetails puDetails;

    private volatile ProcessingUnit processingUnit;

    private volatile GridServiceContainer gridServiceContainer;

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUDetails puDetails, PUServiceBean puServiceBean, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puDetails = puDetails;
        this.puServiceBean = puServiceBean;
    }

    public String getUID() {
        return this.uid;
    }

    public int getInstanceId() {
        return puDetails.getClusterInfo().getInstanceId();
    }

    public int getBackupId() {
        if (puDetails.getClusterInfo().getBackupId() == null) {
            return 0;
        }
        return puDetails.getClusterInfo().getBackupId();
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
        return puDetails.getClusterInfo();
    }

    public Iterator<ProcessingUnitServiceDetails> iterator() {
        return Arrays.asList(puDetails.getDetails()).iterator();
    }

    public ProcessingUnitServiceDetails[] getServiceDetails() {
        return puDetails.getDetails();
    }

    public ServiceID getGridServiceContainerServiceID() {
        return puDetails.getGscServiceID();
    }

    public void setGridServiceContainer(GridServiceContainer gridServiceContainer) {
        this.gridServiceContainer = gridServiceContainer;
    }

    public GridServiceContainer getGridServiceContainer() {
        return this.gridServiceContainer;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return puServiceBean.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return puServiceBean.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return puServiceBean.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return puServiceBean.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return puServiceBean.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return puServiceBean.getJVMStatistics();
    }
}
