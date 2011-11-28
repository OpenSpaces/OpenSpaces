package org.openspaces.admin.internal.pu;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.DefaultSpaceInstances;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstances;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.internal.utils.NameUtils;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.events.EventContainerServiceDetails;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceDetails;
import org.openspaces.events.notify.NotifyEventContainerServiceDetails;
import org.openspaces.events.polling.PollingEventContainerServiceDetails;
import org.openspaces.memcached.MemcachedServiceDetails;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUMonitors;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.pu.service.PlainServiceMonitors;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.remoting.RemotingServiceDetails;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstance extends AbstractGridComponent implements InternalProcessingUnitInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puServiceBean;

    private final PUDetails puDetails;

    private final ServiceDetails[] serviceDetails;

    private volatile InternalProcessingUnit processingUnit;

    private volatile GridServiceContainer gridServiceContainer;

    private volatile ProcessingUnitPartition processingUnitPartition;

    private final SpaceServiceDetails[] embeddedSpacesDetails;

    private final SpaceServiceDetails[] spacesDetails;

    private final Map<String, EventContainerServiceDetails> eventContainerServiceDetails;
    private final Map<String, PollingEventContainerServiceDetails> pollingEventContainerServiceDetails;
    private final Map<String, NotifyEventContainerServiceDetails> notifyEventContainerServiceDetails;
    private final Map<String, AsyncPollingEventContainerServiceDetails> asyncPollingEventContainerServiceDetails;

    private final RemotingServiceDetails remotingServiceDetails;

    private final JeeServiceDetails jeeDetails;

    private final MemcachedServiceDetails memcachedDetails;

    private final Map<String, ServiceDetails> servicesDetailsByServiceId;
    private final Map<String, ServiceDetails[]> servicesDetailsByServiceType;

    private final InternalSpaceInstances spaceInstances;


    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private ProcessingUnitInstanceStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;
    
    private int scheduledStatisticsRefCount = 0;

    private final InternalProcessingUnitInstanceStatisticsChangedEventManager statisticsChangedEventManager;

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUDetails puDetails, PUServiceBean puServiceBean, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puDetails = puDetails;
        this.puServiceBean = puServiceBean;

        this.spaceInstances = new DefaultSpaceInstances(admin);

        this.statisticsChangedEventManager = new DefaultProcessingUnitInstanceStatisticsChangedEventManager(admin);

        this.serviceDetails = new ServiceDetails[puDetails.getDetails().length];
        for (int i = 0; i < puDetails.getDetails().length; i++) {
            this.serviceDetails[i] = (ServiceDetails) puDetails.getDetails()[i];
        }

        ArrayList<SpaceServiceDetails> embeddedSpacesEmbeddedList = new ArrayList<SpaceServiceDetails>();
        ArrayList<SpaceServiceDetails> spacesDetailsList = new ArrayList<SpaceServiceDetails>();
        JeeServiceDetails jeeDetailsX = null;
        MemcachedServiceDetails memcachedDetailsX = null;
        RemotingServiceDetails remotingServiceDetailsX = null;

        Map<String, List<ServiceDetails>> servicesDetailsByServiceTypeList = new HashMap<String, List<ServiceDetails>>();

        Map<String, ServiceDetails> serviceDetailsByServiceId = new HashMap<String, ServiceDetails>();
        
        Map<String, EventContainerServiceDetails> eventContainerServiceDetails = new HashMap<String, EventContainerServiceDetails>();
        Map<String, PollingEventContainerServiceDetails> pollingEventContainerServiceDetails = new HashMap<String, PollingEventContainerServiceDetails>();
        Map<String, NotifyEventContainerServiceDetails> notifyEventContainerServiceDetails = new HashMap<String, NotifyEventContainerServiceDetails>();
        Map<String, AsyncPollingEventContainerServiceDetails> asyncPollingEventContainerServiceDetails = new HashMap<String, AsyncPollingEventContainerServiceDetails>();

        for (ServiceDetails serviceDetails : this.serviceDetails) {
            serviceDetailsByServiceId.put(serviceDetails.getId(), serviceDetails);
            List<ServiceDetails> list = servicesDetailsByServiceTypeList.get(serviceDetails.getServiceType());
            if (list == null) {
                list = new ArrayList<ServiceDetails>();
                servicesDetailsByServiceTypeList.put(serviceDetails.getServiceType(), list);
            }
            list.add(serviceDetails);

            if (serviceDetails instanceof SpaceServiceDetails) {
                SpaceServiceDetails spaceDetails = (SpaceServiceDetails) serviceDetails;
                spacesDetailsList.add(spaceDetails);
                if (spaceDetails.getSpaceType() == SpaceType.EMBEDDED) {
                    embeddedSpacesEmbeddedList.add((SpaceServiceDetails) serviceDetails);
                }
            } else if (serviceDetails instanceof JeeServiceDetails) {
                jeeDetailsX = (JeeServiceDetails) serviceDetails;
            } else if (serviceDetails instanceof MemcachedServiceDetails) {
                memcachedDetailsX = (MemcachedServiceDetails) serviceDetails;
            } else if (serviceDetails instanceof RemotingServiceDetails) {
                remotingServiceDetailsX = (RemotingServiceDetails) serviceDetails;
            } else if (serviceDetails instanceof EventContainerServiceDetails) {
                eventContainerServiceDetails.put(serviceDetails.getId(), (EventContainerServiceDetails) serviceDetails);
                if (serviceDetails instanceof PollingEventContainerServiceDetails) {
                    pollingEventContainerServiceDetails.put(serviceDetails.getId(), (PollingEventContainerServiceDetails) serviceDetails);
                } else if (serviceDetails instanceof NotifyEventContainerServiceDetails) {
                    notifyEventContainerServiceDetails.put(serviceDetails.getId(), (NotifyEventContainerServiceDetails) serviceDetails);
                } else if (serviceDetails instanceof AsyncPollingEventContainerServiceDetails) {
                    asyncPollingEventContainerServiceDetails.put(serviceDetails.getId(), (AsyncPollingEventContainerServiceDetails) serviceDetails);
                }
            }
        }
        this.eventContainerServiceDetails = Collections.unmodifiableMap(eventContainerServiceDetails);
        this.pollingEventContainerServiceDetails = Collections.unmodifiableMap(pollingEventContainerServiceDetails);
        this.notifyEventContainerServiceDetails = Collections.unmodifiableMap(notifyEventContainerServiceDetails);
        this.asyncPollingEventContainerServiceDetails = Collections.unmodifiableMap(asyncPollingEventContainerServiceDetails);

        jeeDetails = jeeDetailsX;
        memcachedDetails = memcachedDetailsX;
        remotingServiceDetails = remotingServiceDetailsX;
        embeddedSpacesDetails = embeddedSpacesEmbeddedList.toArray(new SpaceServiceDetails[embeddedSpacesEmbeddedList.size()]);
        spacesDetails = spacesDetailsList.toArray(new SpaceServiceDetails[spacesDetailsList.size()]);

        this.servicesDetailsByServiceId = Collections.unmodifiableMap(serviceDetailsByServiceId);
        Map<String, ServiceDetails[]> servicesDetailsTemp = new HashMap<String, ServiceDetails[]>();
        for (Map.Entry<String, List<ServiceDetails>> entry : servicesDetailsByServiceTypeList.entrySet()) {
            servicesDetailsTemp.put(entry.getKey(), entry.getValue().toArray(new ServiceDetails[entry.getValue().size()]));
        }
        servicesDetailsByServiceType = Collections.unmodifiableMap(servicesDetailsTemp);
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
    
    public String getProcessingUnitInstanceName() {
        
        return NameUtils.getSpaceInstanceName( getName(), getClusterInfo().getInstanceId(), 
                                   getBackupId(), getClusterInfo().getNumberOfBackups() );
    }

    public void setProcessingUnit(ProcessingUnit processingUnit) {
        assertStateChangesPermitted();
        this.processingUnit = (InternalProcessingUnit) processingUnit;
    }

    public ClusterInfo getClusterInfo() {
        return puDetails.getClusterInfo();
    }

    public BeanLevelProperties getProperties() {
        return puDetails.getBeanLevelProperties();
    }

    public Iterator<ServiceDetails> iterator() {
        return Arrays.asList(this.serviceDetails).iterator();
    }

    public Map<String, EventContainerServiceDetails> getEventContainerDetails() {
        return this.eventContainerServiceDetails;
    }

    public Map<String, PollingEventContainerServiceDetails> getPollingEventContainerDetails() {
        return this.pollingEventContainerServiceDetails;
    }

    public Map<String, NotifyEventContainerServiceDetails> getNotifyEventContainerDetails() {
        return this.notifyEventContainerServiceDetails;
    }

    public Map<String, AsyncPollingEventContainerServiceDetails> getAsyncPollingEventContainerDetails() {
        return this.asyncPollingEventContainerServiceDetails;
    }

    public SpaceServiceDetails[] getSpaceDetails() {
        return spacesDetails;
    }

    public SpaceServiceDetails getEmbeddedSpaceDetails() {
        if (embeddedSpacesDetails.length == 0) {
            return null;
        }
        return embeddedSpacesDetails[0];
    }

    public SpaceServiceDetails[] getEmbeddedSpacesDetails() {
        return embeddedSpacesDetails;
    }

    public ServiceDetails[] getServicesDetails() {
        return this.serviceDetails;
    }

    public ServiceID getGridServiceContainerServiceID() {
        return puDetails.getGscServiceID();
    }

    public void setGridServiceContainer(GridServiceContainer gridServiceContainer) {
        assertStateChangesPermitted();
        this.gridServiceContainer = gridServiceContainer;
    }

    public GridServiceContainer getGridServiceContainer() {
        return this.gridServiceContainer;
    }

    public void setProcessingUnitPartition(ProcessingUnitPartition processingUnitPartition) {
        assertStateChangesPermitted();
        this.processingUnitPartition = processingUnitPartition;
    }

    public ProcessingUnitPartition getPartition() {
        return this.processingUnitPartition;
    }

    public PUServiceBean getPUServiceBean() {
        return this.puServiceBean;
    }

    public boolean isEmbeddedSpaces() {
        return spaceInstances.size() != 0;
    }

    public SpaceInstance getSpaceInstance() {
        Iterator<SpaceInstance> it = spaceInstances.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.getSpaceInstances();
    }

    public SpaceInstance waitForSpaceInstance() {
        return waitForSpaceInstance(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public SpaceInstance waitForSpaceInstance(long timeout, TimeUnit timeUnit) {
        final AtomicReference<SpaceInstance> ref = new AtomicReference<SpaceInstance>();
        ref.set(getSpaceInstance());
        if (ref.get() != null) {
            return ref.get();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        SpaceInstanceAddedEventListener listener = new SpaceInstanceAddedEventListener() {
            public void spaceInstanceAdded(SpaceInstance spaceInstance) {
                ref.set(spaceInstance);
                latch.countDown();
            }
        };
        spaceInstances.getSpaceInstanceAdded().add(listener);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (Exception e) {
            throw new AdminException("Failed to wait for space instance", e);
        } finally {
            spaceInstances.getSpaceInstanceAdded().remove(listener);
        }
    }

    public void addSpaceInstanceIfMatching(SpaceInstance spaceInstance) {
        for (SpaceServiceDetails spaceDetails : embeddedSpacesDetails) {
            if (((InternalSpaceInstance) spaceInstance).getServiceID().equals(spaceDetails.getServiceID())) {
                spaceInstances.addSpaceInstance(spaceInstance);
                processingUnit.addEmbeddedSpace(spaceInstance.getSpace());
            }
        }
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.removeSpaceInstance(uid);
    }

    public boolean isJee() {
        return jeeDetails != null;
    }

    public JeeServiceDetails getJeeDetails() {
        return jeeDetails;
    }

    public MemcachedServiceDetails getMemcachedDetails() {
        return memcachedDetails;
    }

    public RemotingServiceDetails getRemotingDetails() {
        return remotingServiceDetails;
    }

    public ServiceDetails getServiceDetailsByServiceId(String serviceId) {
        return servicesDetailsByServiceId.get(serviceId);
    }

    public Map<String, ServiceDetails> getServiceDetailsByServiceId() {
        return servicesDetailsByServiceId;
    }

    public ServiceDetails[] getServicesDetailsByServiceType(String serviceType) {
        return servicesDetailsByServiceType.get(serviceType);
    }

    public Map<String, ServiceDetails[]> getServiceDetailsByServiceType() {
        return servicesDetailsByServiceType;
    }

    public void destroy() {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).destroyInstance(this);
    }

    public void relocate(GridServiceContainer gridServiceContainerToRelocateTo) {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).relocate(this, gridServiceContainerToRelocateTo);
    }

    public ProcessingUnitInstance relocateAndWait(GridServiceContainer gridServiceContainerToRelocateTo) {
        return relocateAndWait(gridServiceContainerToRelocateTo, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnitInstance relocateAndWait(GridServiceContainer gridServiceContainerToRelocateTo, long timeout, TimeUnit timeUnit) {
        final AtomicReference<ProcessingUnitInstance> ref = new AtomicReference<ProcessingUnitInstance>();
        final CountDownLatch latch = new CountDownLatch(2);
        ProcessingUnitInstanceLifecycleEventListener added = new ProcessingUnitInstanceLifecycleEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                // check that its the same unique number, and not the same uid (since we might get an event for this one)
                if (!processingUnitInstance.getUid().equals(getUid()) && processingUnitInstance.getClusterInfo().getRunningNumber() == getClusterInfo().getRunningNumber()) {
                    ref.set(processingUnitInstance);
                    latch.countDown();
                }
            }

            public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
                if (processingUnitInstance.getUid().equals(getUid())) {
                    latch.countDown();
                }
            }
        };
        processingUnit.addLifecycleListener(added);
        try {
            relocate(gridServiceContainerToRelocateTo);
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            processingUnit.removeLifecycleListener(added);
        }
    }

    public void relocate() {
        relocate(null); //null to relocate to any suitable GSC
    }

    public ProcessingUnitInstance relocateAndWait() {
        return relocateAndWait(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnitInstance relocateAndWait(long timeout, TimeUnit timeUnit) {
        return relocateAndWait(null, timeout, timeUnit);
    }

    public void restart() {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).relocate(this, getGridServiceContainer());
    }

    public ProcessingUnitInstance restartAndWait() {
        return restartAndWait(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnitInstance restartAndWait(long timeout, TimeUnit timeUnit) {
        final AtomicReference<ProcessingUnitInstance> ref = new AtomicReference<ProcessingUnitInstance>();
        final CountDownLatch latch = new CountDownLatch(2);
        ProcessingUnitInstanceLifecycleEventListener added = new ProcessingUnitInstanceLifecycleEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                // check that its the same unique number, and not the same uid (since we might get an event for this one)
                if (!processingUnitInstance.getUid().equals(getUid()) && processingUnitInstance.getClusterInfo().getRunningNumber() == getClusterInfo().getRunningNumber()) {
                    ref.set(processingUnitInstance);
                    latch.countDown();
                }
            }

            public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
                if (processingUnitInstance.getUid().equals(getUid())) {
                    latch.countDown();
                }
            }
        };
        processingUnit.addLifecycleListener(added);
        try {
            restart();
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            processingUnit.removeLifecycleListener(added);
        }
    }

    public void decrement() {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).decrementInstance(this);
    }

    public synchronized ProcessingUnitInstanceStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        ProcessingUnitInstanceStatistics previousStatistics = lastStatistics;
        Map<String, ServiceMonitors> serviceMonitorsById = Collections.EMPTY_MAP;
        lastStatisticsTimestamp = currentTime;
        PUMonitors puMonitors;
        try {
            puMonitors = puServiceBean.getPUMonitors();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get montiors for processing unit instance", e);
        }
        for (Object monitor : puMonitors.getMonitors()) {
            ServiceMonitors serviceMonitors = (ServiceMonitors) monitor;
            if (serviceMonitors instanceof PlainServiceMonitors) {
                ((PlainServiceMonitors) serviceMonitors).setDetails(servicesDetailsByServiceId.get(serviceMonitors.getId()));
            }
            if (serviceMonitorsById == Collections.EMPTY_MAP) {
                serviceMonitorsById = new HashMap<String, ServiceMonitors>();
            }
            serviceMonitorsById.put(serviceMonitors.getId(), serviceMonitors);
        }
        lastStatistics = new DefaultProcessingUnitInstanceServiceStatistics(puMonitors.getTimestamp(), serviceMonitorsById, previousStatistics, statisticsHistorySize,
                getVirtualMachine().getMachine().getOperatingSystem().getTimeDelta());
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMonitor();
            startStatisticsMonitor();
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final ProcessingUnitInstance processingUnitInstance = this;
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                ProcessingUnitInstanceStatistics stats = processingUnitInstance.getStatistics();
                ProcessingUnitInstanceStatisticsChangedEvent event = new ProcessingUnitInstanceStatisticsChangedEvent(processingUnitInstance, stats);
                statisticsChangedEventManager.processingUnitInstanceStatisticsChanged(event);
                ((InternalProcessingUnitInstanceStatisticsChangedEventManager) processingUnit.getProcessingUnitInstanceStatisticsChanged()).processingUnitInstanceStatisticsChanged(event);
                ((InternalProcessingUnitInstanceStatisticsChangedEventManager) processingUnit.getProcessingUnits().getProcessingUnitInstanceStatisticsChanged()).processingUnitInstanceStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    // info providers

    public NIODetails getNIODetails() throws RemoteException {
        return puServiceBean.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return puServiceBean.getNIOStatistics();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        return puServiceBean.getCurrentTimestamp();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return puServiceBean.getOSDetails();
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

    public void runGc() throws RemoteException {
        puServiceBean.runGc();
    }

    public Future<Object> invoke(String serviceBeanName, Map<String, Object> namedArgs) {
        try {
            return (Future<Object>) puServiceBean.invoke(serviceBeanName, namedArgs);
        } catch (RemoteException e) {
            throw new AdminException("Failed to invoke processing unit instance", e);
        }
    }

    @Override
    public boolean isUndeploying() {
        try {
            boolean startedUndeploying = !isDiscovered();
            boolean endedUndeploying = !puServiceBean.isStopping();
            
            return startedUndeploying && !endedUndeploying;
        }
        catch (RemoteException e) {
            throw new AdminException("Failed to check if processing unit instance is alive", e);
        }
    }
}
