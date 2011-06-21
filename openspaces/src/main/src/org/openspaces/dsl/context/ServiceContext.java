package org.openspaces.dsl.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.dsl.Service;

public class ServiceContext {

    private Service service;
    private Admin admin;
    private String dir;
    private ClusterInfo clusterInfo;
     

    public ServiceContext(final Service service, final Admin admin, final String dir, ClusterInfo clusterInfo) {
        super();
        this.service = service;
        this.admin = admin;
        this.dir = dir;
        this.clusterInfo = clusterInfo;

    }    
    
    public int getInstanceId() {
        return clusterInfo.getRunningNumber();
    }
    
    public org.openspaces.dsl.context.Service getService() {
        final String name = this.service.getName();
        return getService(name);
    }
    public org.openspaces.dsl.context.Service getService(String name) {
        
        ProcessingUnit pu = getProcessingUnitFromAdmin(name);

        return new org.openspaces.dsl.context.Service(pu);
        
    }
    

    public List<ProcessingUnitInstance> getServiceInstances() {
        final ProcessingUnit pu = getProcessingUnitFromAdmin();

        return Arrays.asList(pu.getInstances());
    }

    public List<GridServiceAgent> getAgents() {

        final List<ProcessingUnitInstance> puis = getServiceInstances();
        final List<GridServiceAgent> gsas = new ArrayList<GridServiceAgent>(puis.size());
        for (final ProcessingUnitInstance pui : puis) {
            final GridServiceAgent agent = pui.getGridServiceContainer().getGridServiceAgent();
            gsas.add(agent);
        }

        return gsas;

    }

    public List<String> getIPs() {
        final List<GridServiceAgent> agents = getAgents();

        final List<String> ips = new ArrayList<String>(agents.size());
        for (final GridServiceAgent agent : agents) {
            ips.add(agent.getMachine().getHostAddress());
        }

        return ips;
    }

    private ProcessingUnit getProcessingUnitFromAdmin() {
        return getProcessingUnitFromAdmin(this.service.getName());
    }
    
    private ProcessingUnit getProcessingUnitFromAdmin(String name) {

        final ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(name);
        if (pu == null) {
            throw new IllegalStateException("Processing unit with name: " + name
                    + " was not found in the cluster. Are you running in an IntegratedProcessingUnitContainer?");
        }

        return pu;
    }

    public String getDir() {
        return dir;
    }

    public Admin getAdmin() {
        return admin;
    }


    void setService(Service service) {
        this.service = service;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }
  

}
