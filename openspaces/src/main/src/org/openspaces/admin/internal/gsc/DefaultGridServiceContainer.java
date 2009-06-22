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
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainer extends AbstractAgentGridComponent implements InternalGridServiceContainer {

    private final ServiceID serviceID;

    private final GSC gsc;

    private final InternalProcessingUnitInstances processingUnitInstances;

    public DefaultGridServiceContainer(ServiceID serviceID, GSC gsc, InternalAdmin admin, int agentId, String agentUid) {
        super(admin, agentId, agentUid);
        this.serviceID = serviceID;
        this.gsc = gsc;
        this.processingUnitInstances = new DefaultProcessingUnitInstances(admin);
    }

    public String getUid() {
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

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public boolean contains(ProcessingUnitInstance processingUnitInstance) {
        return processingUnitInstances.contains(processingUnitInstance);
    }

    public boolean waitFor(int numberOfProcessingUnitInstances) {
        return waitFor(numberOfProcessingUnitInstances, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                latch.countDown();
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public boolean waitFor(String processingUnitName, int numberOfProcessingUnitInstances) {
        return waitFor(processingUnitName, numberOfProcessingUnitInstances, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(final String processingUnitName, int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (processingUnitInstance.getName().equals(processingUnitName)) {
                    latch.countDown();
                }
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstance(uid);
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsc.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsc.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsc.getOSDetails();
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

    public void runGc() throws RemoteException {
        gsc.runGc();
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