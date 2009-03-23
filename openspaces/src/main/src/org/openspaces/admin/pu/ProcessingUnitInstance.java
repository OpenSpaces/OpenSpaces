/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.pu;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.events.EventContainerServiceDetails;
import org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceDetails;
import org.openspaces.events.notify.NotifyEventContainerServiceDetails;
import org.openspaces.events.polling.PollingEventContainerServiceDetails;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.remoting.RemotingServiceDetails;

import java.util.Map;

/**
 * A processing unit instance is an actual running instance of a processing unit. For example, when deploying
 * a processing unit with 4 instance, there will be eventually 4 instances of the processing unit.
 *
 * @author kimchy
 */
public interface ProcessingUnitInstance extends GridComponent, Iterable<ServiceDetails>, StatisticsMonitor {

    /**
     * Destroy the instance. If breaches the SLA, will instantiate the instance again.
     */
    void destroy();

    /**
     * Decrements the instance (and destroying it in the process). Will not attempt to create it again.
     *
     * @see ProcessingUnit#canDecrementInstance()
     */
    void decrement();

    /**
     * Relocates the instance to the provided {@link org.openspaces.admin.gsc.GridServiceContainer}.
     */
    void relocate(GridServiceContainer gridServiceContainerToRelocateTo);

    /**
     * Relocates the instance to any suitable {@link org.openspaces.admin.gsc.GridServiceContainer}.
     */
    void relocate();

    /**
     * Returns the instance id of the processing unit instance.
     */
    int getInstanceId();

    /**
     * Returns the backup id of the processing unit instance.
     */
    int getBackupId();

    /**
     * Returns the processing unit this processing unit instance belongs to.
     */
    ProcessingUnit getProcessingUnit();

    /**
     * Returns the name of the processing unit.
     */
    String getName();

    /**
     * Returns the cluster info of the processing unit instance.
     */
    ClusterInfo getClusterInfo();

    /**
     * Return the properties the processing unit was deployed with.
     */
    BeanLevelProperties getProperties();

    /**
     * Returns the {@link org.openspaces.admin.gsc.GridServiceContainer} the processing unit is running on.
     */
    GridServiceContainer getGridServiceContainer();

    /**
     * Returns the processing unit partition this processing unit instance is part of.
     */
    ProcessingUnitPartition getPartition();

    /**
     * Returns the service details for a specific service id.
     */
    ServiceDetails getServiceDetailsByServiceId(String serviceId);

    /**
     * Returns a map of service details by service id.
     */
    Map<String, ServiceDetails> getServiceDetailsByServiceId();

    /**
     * Returns the service details by a service type {@link org.openspaces.pu.service.ServiceDetails#getServiceType()}.
     */
    ServiceDetails[] getServicesDetailsByServiceType(String serviceType);

    /**
     * Returns a map of service details by service type.
     */
    Map<String, ServiceDetails[]> getServiceDetailsByServiceType();

    /**
     * Returns a map of {@link org.openspaces.events.EventContainerServiceDetails} keyed by their
     * {@link org.openspaces.pu.service.ServiceDetails#getId()}.
     */
    Map<String, EventContainerServiceDetails> getEventContainerServiceDetails();

    /**
     * Returns a map of {@link org.openspaces.events.polling.PollingEventContainerServiceDetails} keyed by their
     * {@link org.openspaces.pu.service.ServiceDetails#getId()}.
     */
    Map<String, PollingEventContainerServiceDetails> getPollingEventContainerServiceDetails();

    /**
     * Returns a map of {@link org.openspaces.events.notify.NotifyEventContainerServiceDetails} keyed by their
     * {@link org.openspaces.pu.service.ServiceDetails#getId()}.
     */
    Map<String, NotifyEventContainerServiceDetails> getNotifyEventContainerServiceDetails();

    /**
     * Returns a map of {@link org.openspaces.events.asyncpolling.AsyncPollingEventContainerServiceDetails} keyed by their
     * {@link org.openspaces.pu.service.ServiceDetails#getId()}.
     */
    Map<String, AsyncPollingEventContainerServiceDetails> getAsyncPollingEventContainerServiceDetails();

    /**
     * Returns the remoting service details (the exporter) if configured within the processing unit.
     */
    RemotingServiceDetails getRemotingServiceDetails();

    /**
     * Returns the space service details as described by the service started within the processing unit.
     */
    SpaceServiceDetails[] getSpaceServiceDetails();

    /**
     * Returns the embedded space service details as described by the service started within the processing unit.
     * <code>null</code> if no embedded space was started within the processing unit.
     */
    SpaceServiceDetails getEmbeddedSpaceServiceDetails();

    /**
     * Returns the embedded space service details as described by the service started within the processing unit.
     */
    SpaceServiceDetails[] getEmbeddedSpacesServiceDetails();

    /**
     * Returns <code>true</code> if there are embedded spaces started within this processing
     * unit.
     */
    boolean isEmbeddedSpaces();

    /**
     * Returns a space instance that was started within the processing unit instance. Will
     * return <code>null</code> if no embedded space instances were started (or none has been detected yet).
     */
    SpaceInstance getSpaceInstance();

    /**
     * Returns all the space instances that were stared within the processing unit instance.
     * Will return an empty array if no space instances were started within this processing unit (or none has
     * been detected yet).
     */
    SpaceInstance[] getSpaceInstances();

    /**
     * Returns <code>true</code> if this processing unit is a jee processing unit.
     */
    boolean isJee();

    /**
     * Returns the jee service details of the jee container that was started within this processing unit.
     */
    JeeServiceDetails getJeeDetails();

    ProcessingUnitInstanceStatistics getStatistics();
}
