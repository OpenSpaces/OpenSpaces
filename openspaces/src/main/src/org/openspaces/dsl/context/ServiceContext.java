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

    /*********
     * Constructor used when running in IntegratedProcessingUnitContainer.
     * 
     * @param service
     *            .
     * @param dir
     *            .
     */
    public ServiceContext(final Service service, final String dir) {
        this.service = service;
        this.dir = dir;
        this.clusterInfo = new ClusterInfo(null, 1, 0, 1, 0);
        this.clusterInfo.setName(service.getName());

    }

    public int getInstanceId() {
        return clusterInfo.getInstanceId();
    }

    public org.openspaces.dsl.context.Service getService() {
        final String name = this.clusterInfo.getName();
        return getService(name);
    }

    public org.openspaces.dsl.context.Service getService(String name) {

        if (this.admin != null) {
            ProcessingUnit pu = getProcessingUnitFromAdmin(name);

            return new org.openspaces.dsl.context.Service(pu);
        } else {
            // running in integrated container
            if(name.equals(this.service.getName())) {
                return new org.openspaces.dsl.context.Service(name, service.getNumInstances());
            } else {
                return null;
            }
        }

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

    @Override
    public String toString() {
        return "ServiceContext [dir=" + dir + ", clusterInfo=" + clusterInfo + ", getService()=" + getService() + "]";
    }

    
}
