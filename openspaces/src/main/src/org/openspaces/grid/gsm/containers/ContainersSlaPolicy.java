package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    int containers;
    
    GridServiceContainerOptions newContainerOptions;

    //private MachineIsolation machineIsolation;

    //private String machineZone;

    private int reservedInMB;

    private int maxNumberOfContainersPerMachine;

   
    public void setTargetNumberOfContainers(int containers) {
        this.containers = containers;
    }
    
    public int getTargetNumberOfContainers() {
        return this.containers;
    }
    
    public void setNewContainerOptions(GridServiceContainerOptions options) {
        this.newContainerOptions = options;
    }
    
    public GridServiceContainerOptions getNewContainerOptions() {
        return this.newContainerOptions;
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
    
 /*
    public void setMachineIsolation(MachineIsolation machineIsolation) {
        this.machineIsolation = machineIsolation;
        
    }
    public MachineIsolation getMachineIsolation() {
        return this.machineIsolation;
    }

    public void setMachineZone(String machineZone) {
        this.machineZone = machineZone;
    }
    
    public String getMachineZone() {
        return this.machineZone;
    }
*/
    
    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
               ((ContainersSlaPolicy)other).containers == this.containers &&
               ((ContainersSlaPolicy)other).newContainerOptions.equals(this.newContainerOptions) &&
               //((ContainersAdminServiceLevelAgreement)other).machineZone.equals(this.machineZone) &&
               //((ContainersAdminServiceLevelAgreement)other).machineIsolation.equals(this.machineIsolation) &&
               ((ContainersSlaPolicy)other).reservedInMB == this.reservedInMB &&
               ((ContainersSlaPolicy)other).maxNumberOfContainersPerMachine == this.maxNumberOfContainersPerMachine;
    }

    

}
