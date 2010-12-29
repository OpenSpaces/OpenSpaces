package org.openspaces.utest.admin.internal.admin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.jini.core.discovery.LookupLocator;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.dump.DumpGeneratedListener;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.zone.Zones;

public class NullMockAdmin implements Admin {

    public void addEventListener(AdminEventListener eventListener) {
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

}
