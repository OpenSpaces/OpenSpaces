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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstance extends AbstractGridComponent implements InternalProcessingUnitInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puServiceBean;

    private final PUDetails puDetails;

    private volatile InternalProcessingUnit processingUnit;

    private volatile GridServiceContainer gridServiceContainer;

    private volatile ProcessingUnitPartition processingUnitPartition;

    private final SpaceProcessingUnitServiceDetails[] embeddedSpacesDetails;

    private final SpaceProcessingUnitServiceDetails[] spacesDetails;

    private final JeeProcessingUnitServiceDetails jeeDetails;

    private final Map<String, ProcessingUnitServiceDetails[]> servicesDetailsByServiceId;

    private final Map<String, SpaceInstance> spaceInstances = new SizeConcurrentHashMap<String, SpaceInstance>();

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUDetails puDetails, PUServiceBean puServiceBean, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puDetails = puDetails;
        this.puServiceBean = puServiceBean;

        ArrayList<SpaceProcessingUnitServiceDetails> embeddedSpacesEmbeddedList = new ArrayList<SpaceProcessingUnitServiceDetails>();
        ArrayList<SpaceProcessingUnitServiceDetails> spacesDetailsList = new ArrayList<SpaceProcessingUnitServiceDetails>();
        JeeProcessingUnitServiceDetails jeeDetailsX = null;

        Map<String, List<ProcessingUnitServiceDetails>> servicesDetailsByServiceIdList = new HashMap<String, List<ProcessingUnitServiceDetails>>();

        for (ProcessingUnitServiceDetails serviceDetails : puDetails.getDetails()) {

            List<ProcessingUnitServiceDetails> list = servicesDetailsByServiceIdList.get(serviceDetails.getServiceType());
            if (list == null) {
                list = new ArrayList<ProcessingUnitServiceDetails>();
                servicesDetailsByServiceIdList.put(serviceDetails.getServiceType(), list);
            }
            list.add(serviceDetails);

            if (serviceDetails instanceof SpaceProcessingUnitServiceDetails) {
                SpaceProcessingUnitServiceDetails spaceDetails = (SpaceProcessingUnitServiceDetails) serviceDetails;
                spacesDetailsList.add(spaceDetails);
                if (spaceDetails.getSpaceType() == SpaceType.EMBEDDED) {
                    embeddedSpacesEmbeddedList.add((SpaceProcessingUnitServiceDetails) serviceDetails);
                }
            } else if (serviceDetails instanceof JeeProcessingUnitServiceDetails) {
                jeeDetailsX = (JeeProcessingUnitServiceDetails) serviceDetails;
            }
        }

        jeeDetails = jeeDetailsX;
        embeddedSpacesDetails = embeddedSpacesEmbeddedList.toArray(new SpaceProcessingUnitServiceDetails[embeddedSpacesEmbeddedList.size()]);
        spacesDetails = spacesDetailsList.toArray(new SpaceProcessingUnitServiceDetails[spacesDetailsList.size()]);

        Map<String, ProcessingUnitServiceDetails[]> servicesDetailsTemp = new HashMap<String, ProcessingUnitServiceDetails[]>();
        for (Map.Entry<String, List<ProcessingUnitServiceDetails>> entry : servicesDetailsByServiceIdList.entrySet()) {
            servicesDetailsTemp.put(entry.getKey(), entry.getValue().toArray(new ProcessingUnitServiceDetails[entry.getValue().size()]));
        }
        servicesDetailsByServiceId = servicesDetailsTemp;
    }

    public String getUid() {
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

    public String getName() {
        return puDetails.getClusterInfo().getName();
    }

    public void setProcessingUnit(ProcessingUnit processingUnit) {
        this.processingUnit = (InternalProcessingUnit) processingUnit;
    }

    public ClusterInfo getClusterInfo() {
        return puDetails.getClusterInfo();
    }

    public Iterator<ProcessingUnitServiceDetails> iterator() {
        return Arrays.asList(puDetails.getDetails()).iterator();
    }

    public SpaceProcessingUnitServiceDetails[] getSpaceServiceDetails() {
        return spacesDetails;
    }

    public SpaceProcessingUnitServiceDetails getEmbeddedSpaceServiceDetails() {
        if (embeddedSpacesDetails.length == 0) {
            return null;
        }
        return embeddedSpacesDetails[0];
    }

    public SpaceProcessingUnitServiceDetails[] getEmbeddedSpacesServiceDetails() {
        return embeddedSpacesDetails;
    }

    public ProcessingUnitServiceDetails[] getServicesDetails() {
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

    public boolean isEmbeddedSpaces() {
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
        for (SpaceProcessingUnitServiceDetails spaceDetails : embeddedSpacesDetails) {
            if (((InternalSpaceInstance) spaceInstance).getServiceID().equals(spaceDetails.getServiceID())) {
                spaceInstances.put(spaceInstance.getUid(), spaceInstance);
                processingUnit.addEmbeddedSpace(spaceInstance.getSpace());
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

    public ProcessingUnitServiceDetails[] getServicesDetailsByServiceType(String serviceType) {
        return servicesDetailsByServiceId.get(serviceType);
    }

    public Map<String, ProcessingUnitServiceDetails[]> getServiceDetailsByServiceType() {
        return Collections.unmodifiableMap(servicesDetailsByServiceId);
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
