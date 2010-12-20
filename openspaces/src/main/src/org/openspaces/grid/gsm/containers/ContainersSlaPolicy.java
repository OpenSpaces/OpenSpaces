package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    int containers;
    
    GridServiceContainerConfig newContainerConfig;

    //private MachineIsolation machineIsolation;

    //private String machineZone;

    private int reservedInMB;

    private int maxNumberOfContainersPerMachine;

    private GridServiceAgent[] gridServiceAgents;

   
    public void setTargetNumberOfContainers(int containers) {
        this.containers = containers;
    }
    
    public int getTargetNumberOfContainers() {
        return this.containers;
    }
    
    public void setNewContainerConfig(GridServiceContainerConfig config) {
        this.newContainerConfig = config;
    }
    
    public GridServiceContainerConfig getNewContainerConfig() {
        return this.newContainerConfig;
    }

    public int getReservedPhysicalMemoryPerMachineInMB() {
        return reservedInMB;
    }
    
    public void setReservedPhysicalMemoryPerMachineInMB(int reservedInMB) {
        this.reservedInMB =reservedInMB ; 
    }
    
    public int getMaximumNumberOfContainersPerMachine() {
        return maxNumberOfContainersPerMachine;
    }
    
    public void setMaximumNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        this.maxNumberOfContainersPerMachine = maxNumberOfContainersPerMachine;
    }
    

    public GridServiceAgent[] getGridServiceAgents() {
        return this.gridServiceAgents;
    }
    
    public void setGridServiceAgents(GridServiceAgent[] gridServiceAgents) {
        this.gridServiceAgents = gridServiceAgents;
    }
    
    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
               ((ContainersSlaPolicy)other).containers == this.containers &&
               ((ContainersSlaPolicy)other).newContainerConfig.equals(this.newContainerConfig) &&
               //((ContainersAdminServiceLevelAgreement)other).machineZone.equals(this.machineZone) &&
               //((ContainersAdminServiceLevelAgreement)other).machineIsolation.equals(this.machineIsolation) &&
               ((ContainersSlaPolicy)other).reservedInMB == this.reservedInMB &&
               ((ContainersSlaPolicy)other).maxNumberOfContainersPerMachine == this.maxNumberOfContainersPerMachine;
    }

}
