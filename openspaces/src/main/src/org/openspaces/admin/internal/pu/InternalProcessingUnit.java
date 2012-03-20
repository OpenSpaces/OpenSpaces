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

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.application.InternalApplicationAware;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.space.Space;

/**
 * @author kimchy
 */
public interface InternalProcessingUnit extends ProcessingUnit, InternalProcessingUnitInstancesAware , InternalApplicationAware {

    void setNumberOfInstances(int numberOfInstances);

    void setNumberOfBackups(int numberOfBackups);

    void setManagingGridServiceManager(GridServiceManager gridServiceManager);
    
    void addManagingGridServiceManager(GridServiceManager gridServiceManager);

    void addBackupGridServiceManager(GridServiceManager backupGridServiceManager);

    void removeBackupGridServiceManager(String gsmUID);

    boolean setStatus(int statusCode);

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addEmbeddedSpace(Space space);
    
    Map<String, String> getElasticProperties();

    String getApplicationName();
    
    String getClusterSchema();
    
    /**
     * In the case of application name prefix just processing unit name will be returned.
     * Otherwise name will be returned.
     * @return
     * @since 8.0.6
     */
    String getSimpleName();

    /**
     * Retrieves a context property containing the order of dependencies within an application.
     * @return The property value, in a CSV format.
     * @since 8.0.4 
     */
    String getApplicationDependencies();
    
    /**
     * Returns the current scale strategy config
     *
     * @since 8.0.3
     */
    ScaleStrategyConfig getScaleStrategyConfig();

    /**
     * Decrements a planned instance if the number of planned instances is less then the actual instances to maintain.
     * @return <code>true</code> if a planned instance was successfully decremented; <code>false</code> planned instances hasn't changed.
     * @since 8.0.4
     */
    boolean decrementPlannedInstances();

    /**
     * @since 8.0.6
     */
    void processProvisionEvent(ProvisionLifeCycleEvent provisionLifeCycleEvent);
    
    /**
     * @since 8.0.6
     */
    Map<String, InternalProvisionStatusHolder> getProvisionStatusPerInstance();

    /**
     *  Defines calculation of time based statistics for each processing unit instance statistics.
     * 
     * @since 9.0.0
     * @author itaif 
     */
    void setTimeAggregatorServiceMonitorsProviders(TimeAggregatorServiceMonitorsProvider[] timeAggregators);
    
    /**
     *  Defines calculation of time based statistics for each processing unit instance statistics.
     * 
     * @since 9.0.0
     * @author itaif 
     */
    void setClusterAggregatorServiceMonitorsProviders(ClusterAggregatorServiceMonitorsProvider[] clusterAggregators);
    
}
