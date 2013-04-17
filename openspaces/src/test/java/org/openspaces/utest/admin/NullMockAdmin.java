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
package org.openspaces.utest.admin;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jini.core.discovery.LookupLocator;

import org.apache.commons.logging.Log;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.application.Applications;
import org.openspaces.admin.dump.DumpGeneratedListener;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewayProcessingUnits;
import org.openspaces.admin.gateway.Gateways;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.zone.Zones;
import org.openspaces.security.AdminFilter;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.service.SecuredService;

public class NullMockAdmin implements InternalAdmin {

    public Log getAdminLogger() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void addEventListener(AdminEventListener eventListener) {
        // TODO Auto-generated method stub

    }
    
	@Override
	public void addEventListener(AdminEventListener eventListener,
			boolean withStatisticsHistory) {
		// TODO Auto-generated method stub
		
	}    

    public void close() {
        // TODO Auto-generated method stub

    }

    public DumpResult generateDump(Set<DumpProvider> dumpProviders, DumpGeneratedListener listener, String cause,
            Map<String, Object> context, String... processor) throws AdminException {
        // TODO Auto-generated method stub
        return null;
    }

    public AlertManager getAlertManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public ElasticServiceManagers getElasticServiceManagers() {
        // TODO Auto-generated method stub
        return null;
    }

    public GridComponent getGridComponentByUID(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    public GridServiceAgents getGridServiceAgents() {
        // TODO Auto-generated method stub
        return null;
    }

    public GridServiceContainers getGridServiceContainers() {
        // TODO Auto-generated method stub
        return null;
    }

    public GridServiceManagers getGridServiceManagers() {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    public LookupLocator[] getLocators() {
        // TODO Auto-generated method stub
        return null;
    }

    public LookupServices getLookupServices() {
        // TODO Auto-generated method stub
        return null;
    }

    public Machines getMachines() {
        // TODO Auto-generated method stub
        return null;
    }

    public OperatingSystems getOperatingSystems() {
        // TODO Auto-generated method stub
        return null;
    }

    public ProcessingUnits getProcessingUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    public Spaces getSpaces() {
        // TODO Auto-generated method stub
        return null;
    }

    public Transports getTransports() {
        // TODO Auto-generated method stub
        return null;
    }

    public VirtualMachines getVirtualMachines() {
        // TODO Auto-generated method stub
        return null;
    }

    public Zones getZones() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeEventListener(AdminEventListener eventListener) {
        // TODO Auto-generated method stub

    }

    public void setAgentProcessessMonitorInterval(long interval, TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }

    public void setDefaultTimeout(long timeout, TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }

    public void setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }

    public void setSchedulerCorePoolSize(int coreThreads) {
        // TODO Auto-generated method stub

    }

    public void setSpaceMonitorInterval(long interval, TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }

    public boolean isMonitoring() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setStatisticsHistorySize(int historySize) {
        // TODO Auto-generated method stub

    }

    public void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }

    public void startStatisticsMonitor() {
        // TODO Auto-generated method stub

    }

    public void stopStatisticsMonitor() {
        // TODO Auto-generated method stub

    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        // TODO Auto-generated method stub
        return null;
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor)
            throws AdminException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager, NIODetails nioDetails,
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones, boolean acceptVM) {
        // TODO Auto-generated method stub
        
    }

    public void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails,
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones) {
        // TODO Auto-generated method stub
        
    }

    public void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer, NIODetails nioDetails,
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones) {
        // TODO Auto-generated method stub
        
    }

    public void addGridServiceManager(InternalGridServiceManager gridServiceManager, NIODetails nioDetails,
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones, boolean acceptVM) {
        // TODO Auto-generated method stub
        
    }

    public void addLookupService(InternalLookupService lookupService, NIODetails nioDetails, OSDetails osDetails,
            JVMDetails jvmDetails, String jmxUrl, String[] zones) {
        // TODO Auto-generated method stub
        
    }

    public void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails,
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones) {
        // TODO Auto-generated method stub
        
    }

    public void addSpaceInstance(InternalSpaceInstance spaceInstance, NIODetails nioDetails, OSDetails osDetails,
            JVMDetails jvmDetails, String jmxUrl, String[] zones) {
        // TODO Auto-generated method stub
        
    }

    public void assertStateChangesPermitted() {
        // TODO Auto-generated method stub
        
    }

    public long getDefaultTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    public TimeUnit getDefaultTimeoutTimeUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getScheduledSpaceMonitorInterval() {
        // TODO Auto-generated method stub
        return 0;
    }

    public ScheduledThreadPoolExecutor getScheduler() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }
        
    @Override
    public void login(SecuredService service) throws SecurityException, RemoteException {
        // TODO Auto-generated method stub        
    }

    public void pushEvent(Object listener, Runnable notifier) {
        // TODO Auto-generated method stub
        
    }

    public void pushEventAsFirst(Object listener, Runnable notifier) {
        // TODO Auto-generated method stub
        
    }

    public void raiseEvent(Object listener, Runnable notifier) {
        // TODO Auto-generated method stub
        
    }

    public void removeElasticServiceManager(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void removeGridServiceAgent(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void removeGridServiceContainer(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void removeGridServiceManager(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void removeLookupService(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void removeProcessingUnitInstance(String uid, boolean removeEmbeddedSpaces) {
        // TODO Auto-generated method stub
        
    }

    public void removeSpaceInstance(String uid) {
        // TODO Auto-generated method stub
        
    }

    public void scheduleAdminOperation(Runnable runnable) {
        // TODO Auto-generated method stub
        
    }

    public void scheduleNonBlockingStateChange(Runnable runnable) {
        // TODO Auto-generated method stub
        
    }

    public ScheduledFuture<?> scheduleWithFixedDelayNonBlockingStateChange(Runnable command, long initialDelay,
            long delay, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }

    public void singleThreadedEventListeners() {
        // TODO Auto-generated method stub
        
    }

    public ScheduledFuture<?> scheduleOneTimeWithDelayNonBlockingStateChange(Runnable command, long delay, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }

    public ProcessingUnit[] getProcessingUnitsForApplication(String applicationName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getApplicationNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getApplicationName(ProcessingUnit pu) {
        // TODO Auto-generated method stub
        return null;
    }

    public Applications getApplications() {
        // TODO Auto-generated method stub
        return null;
    }

    public Gateways getGateways() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getEventListenersCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isSingleThreadedEventListeners() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.admin.InternalAdmin#getAdminFilter()
     */
    @Override
    public AdminFilter getAdminFilter() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public GatewayProcessingUnits getGatewayProcessingUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addGatewayProcessingUnit( GatewayProcessingUnit gatewayProcessingUnit ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGatewayProcessingUnit( String uid ) {
		// TODO Auto-generated method stub
		
	}
}