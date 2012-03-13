package org.openspaces.admin.internal.pu;

import java.util.Map;
import java.util.concurrent.Future;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.monitor.ServiceFaultDetectionEvent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatisticsTimeAggregator;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.pu.container.servicegrid.PUServiceBean;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstance extends ProcessingUnitInstance, InternalGridComponent {

    ServiceID getServiceID();

    ServiceID getGridServiceContainerServiceID();

    void setProcessingUnit(ProcessingUnit processingUnit);

    void setGridServiceContainer(GridServiceContainer gridServiceContainer);

    void setProcessingUnitPartition(ProcessingUnitPartition processingUnitPartition);

    /**
     * Adds a space instance only if it is one that the processing unit has started.
     */
    void addSpaceInstanceIfMatching(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);

    PUServiceBean getPUServiceBean();

    Future<Object> invoke(String serviceBeanName, Map<String, Object> namedArgs);

    boolean isUndeploying();

    /**
     * @return Return instance name without prefix of application name ( if exists )
     * @since 8.0.6
     */
    String getProcessingUnitInstanceSimpleName();
    
    void setMemberAliveIndicatorStatus(ServiceFaultDetectionEvent serviceFaultDetectionEvent);
    
    /**
     * This method is non-blocking and should used in conjunction with {@link #getStatistics()} or {@link #setStatisticsInterval(long, java.util.concurrent.TimeUnit)}
     * @return the last statistics (regardless of its timestamp), or null if no statistics has been ever collected.
     * @since 8.0.6
     */
    ProcessingUnitInstanceStatistics getLastStatistics();
    
    void enableTimeAggregatedServiceMonitors(String serviceMonitorsId, ProcessingUnitInstanceStatisticsTimeAggregator[] aggregators);
    
    void disableTimeAggregatedServiceMonitors(String serviceMonitorsId);
}