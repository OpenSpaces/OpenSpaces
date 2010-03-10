package org.openspaces.admin.esm.deployment;

import java.io.Serializable;

public class DeploymentContext implements Serializable {
    
    private IsolationLevel isolationLevel = IsolationLevel.DEDICATED;
    private String minMemory = "1g";
    private String maxMemory = "10g";
    private boolean highlyAvailable = true;
    private String slaDescriptors = "";
    private String initialJavaHeapSize = "512m";
    private String maximumJavaHeapSize = "512m";
    private String vmInputArguments = null;
    
    DeploymentContext(){
    }

    public void setMinMemory(String minMemory) {
        this.minMemory = minMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public boolean isHighlyAvailable() {
        return highlyAvailable;
    }

    public void setHighlyAvailable(boolean highlyAvailable) {
        this.highlyAvailable = highlyAvailable;
    }

    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
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
}
