/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
    boolean addSpaceInstanceIfMatching(SpaceInstance spaceInstance);

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

    /**
     * @return the statistics interval milliseconds
     * @since 9.0.0
     */
    long getStatisticsIntervalMilliseconds();
}