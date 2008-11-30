package org.openspaces.admin.internal.pu;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import com.j_spaces.kernel.SizeConcurrentHashMap;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.SpaceProcessingUnitServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.pu.container.jee.JeeProcessingUnitServiceDetails;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

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

    private volatile ProcessingUnitPartition processingUnitPartition;

    private final SpaceProcessingUnitServiceDetails[] spacesDetails;

    private final JeeProcessingUnitServiceDetails jeeDetails;

    private final Map<String, SpaceInstance> spaceInstances = new SizeConcurrentHashMap<String, SpaceInstance>();

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUDetails puDetails, PUServiceBean puServiceBean, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puDetails = puDetails;
        this.puServiceBean = puServiceBean;

        ArrayList<SpaceProcessingUnitServiceDetails> spacesDetailsList = new ArrayList<SpaceProcessingUnitServiceDetails>();
        JeeProcessingUnitServiceDetails jeeDetailsX = null;
        for (ProcessingUnitServiceDetails serviceDetails : puDetails.getDetails()) {
            if (serviceDetails instanceof SpaceProcessingUnitServiceDetails) {
                SpaceProcessingUnitServiceDetails space = (SpaceProcessingUnitServiceDetails) serviceDetails;
                if (space.getSpaceType() == SpaceType.EMBEDDED) {
                    spacesDetailsList.add((SpaceProcessingUnitServiceDetails) serviceDetails);
                }
            } else if (serviceDetails instanceof JeeProcessingUnitServiceDetails) {
                jeeDetailsX = (JeeProcessingUnitServiceDetails) serviceDetails;
            }
        }
        jeeDetails = jeeDetailsX;
        spacesDetails = spacesDetailsList.toArray(new SpaceProcessingUnitServiceDetails[spacesDetailsList.size()]);
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

    public void setProcessingUnitPartition(ProcessingUnitPartition processingUnitPartition) {
        this.processingUnitPartition = processingUnitPartition;
    }

    public ProcessingUnitPartition getPartition() {
        return this.processingUnitPartition;
    }

    public boolean hasEmbeddedSpaces() {
        return spaceInstances.size() != 0;
    }

    public SpaceInstance getSpaceInstance() {
        Iterator<SpaceInstance> it = spaceInstances.values().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public void addSpaceInstnaceIfMatching(SpaceInstance spaceInstance) {
        for (SpaceProcessingUnitServiceDetails spaceDetails : spacesDetails) {
            if (((InternalSpaceInstance) spaceInstance).getServiceID().equals(spaceDetails.getServiceID())) {
                spaceInstances.put(spaceInstance.getUID(), spaceInstance);
            }
        }
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.remove(uid);
    }

    public boolean isJee() {
        return jeeDetails != null;
    }

    public JeeProcessingUnitServiceDetails getJeeDetails() {
        return jeeDetails;
    }

// info providers

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
