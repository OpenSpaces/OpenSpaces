package org.openspaces.admin.esm.deployment;

import java.io.Serializable;

public class DeploymentContext implements Serializable {
    
    private DeploymentIsolationLevel deploymentIsolationLevel = DeploymentIsolationLevel.DEDICATED;
    private String minMemory = "1g";
    private String maxMemory = "10g";
    private boolean highlyAvailable = true;
    private String slaDescriptors = "";
    private String initialJavaHeapSize = "512m";
    private String maximumJavaHeapSize = "512m";
    private String vmInputArguments = null;
    private String tenant ="";
    
    DeploymentContext(){
    }

    public void setMinMemory(String minMemory) {
        this.minMemory = minMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    public void setDeploymentIsolationLevel(DeploymentIsolationLevel isolationLevel) {
        this.deploymentIsolationLevel = isolationLevel;
    }

    public boolean isHighlyAvailable() {
        return highlyAvailable;
    }

    public void setHighlyAvailable(boolean highlyAvailable) {
        this.highlyAvailable = highlyAvailable;
    }

    public DeploymentIsolationLevel getDeploymentIsolationLevel() {
        return deploymentIsolationLevel;
    }

    public String getMinMemory() {
        return minMemory;
    }

    public String getMaxMemory() {
        return maxMemory;
    }

    public void addSla(String descriptor) {
        slaDescriptors += descriptor;
    };
    
    public String getSlaDescriptors() {
        return slaDescriptors;
    }

    public void setInitialJavaHeapSize(String size) {
        this.initialJavaHeapSize = size;
    }
    
    public String getInitialJavaHeapSize() {
        return initialJavaHeapSize;
    }

    public void setMaximumJavaHeapSize(String size) {
        this.maximumJavaHeapSize = size;
    }
    
    public String getMaximumJavaHeapSize() {
        return maximumJavaHeapSize;
    }
    
    public void addVmArgument(String arg) {
        if (vmInputArguments == null) {
            vmInputArguments = arg;
        } else {
            vmInputArguments += "," + arg;
        }
    }
    
    public String getVmInputArguments() {
        return vmInputArguments;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public String getTenant() {
        return tenant;
    }
}
