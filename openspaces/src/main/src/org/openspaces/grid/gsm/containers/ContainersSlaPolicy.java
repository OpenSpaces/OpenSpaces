package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    int containers;
    
    GridServiceContainerConfig newContainerConfig;

    //private MachineIsolation machineIsolation;

    //private String machineZone;

    private int reservedMemoryPerMachineInMB;

    private GridServiceAgent[] gridServiceAgents;
  
    private long memoryInMB;
    
    private double cpu;

    private int minimumNumberOfMachines;
    
    public void setCpuCapacity(double cpu) {
        this.cpu = cpu;
    }
    
    public double getNumberOfCpuCores() {
        return this.cpu;
    }
    
    public void setMemoryCapacityInMB(long memory) {
        this.memoryInMB = memory;
    }
    
    public long getMemoryCapacityInMB() {
        return this.memoryInMB;
    }
    
    public void setNewContainerConfig(GridServiceContainerConfig config) {
        this.newContainerConfig = config;
    }
    
    public GridServiceContainerConfig getNewContainerConfig() {
        return this.newContainerConfig;
    }

    public int getReservedMemoryCapacityPerMachineInMB() {
        return reservedMemoryPerMachineInMB;
    }
    
    public void setReservedMemoryCapacityPerMachineInMB(int reservedInMB) {
        this.reservedMemoryPerMachineInMB =reservedInMB ; 
    }
    
    public GridServiceAgent[] getGridServiceAgents() {
        return this.gridServiceAgents;
    }
    
    public void setGridServiceAgents(GridServiceAgent[] gridServiceAgents) {
        this.gridServiceAgents = gridServiceAgents;
    }
    
    public int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }
    
    public void setMinimumNumberOfMachines(int minimumNumberOfMachines) {
        this.minimumNumberOfMachines = minimumNumberOfMachines;
    }
    
    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
               ((ContainersSlaPolicy)other).containers == this.containers &&
               ((ContainersSlaPolicy)other).newContainerConfig.equals(this.newContainerConfig) &&
               //((ContainersAdminServiceLevelAgreement)other).machineZone.equals(this.machineZone) &&
               //((ContainersAdminServiceLevelAgreement)other).machineIsolation.equals(this.machineIsolation) &&
               ((ContainersSlaPolicy)other).reservedMemoryPerMachineInMB == this.reservedMemoryPerMachineInMB &&
               ((ContainersSlaPolicy)other).minimumNumberOfMachines == this.minimumNumberOfMachines &&
               ((ContainersSlaPolicy)other).memoryInMB == this.memoryInMB &&
               ((ContainersSlaPolicy)other).cpu == this.cpu;
    }

}
