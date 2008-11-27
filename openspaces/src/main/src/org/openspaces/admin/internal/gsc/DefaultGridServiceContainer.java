package org.openspaces.admin.internal.gsc;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainer extends AbstractGridComponent implements InternalGridServiceContainer {

    private final ServiceID serviceID;

    private final GSC gsc;

    private final InternalProcessingUnitInstances processingUnitInstances = new DefaultProcessingUnitInstances();

    public DefaultGridServiceContainer(ServiceID serviceID, GSC gsc, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.gsc = gsc;
    }

    public String getUID() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSC getGSC() {
        return this.gsc;
    }

    public Iterator<ProcessingUnitInstance> iterator() {
        return processingUnitInstances.getInstancesIt();
    }

    public ProcessingUnitInstance[] getProcessingUnitInsances() {
        return processingUnitInstances.getInstances();
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstnace(uid);
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsc.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsc.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsc.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return gsc.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return gsc.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return gsc.getJVMStatistics();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGridServiceContainer that = (DefaultGridServiceContainer) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
}