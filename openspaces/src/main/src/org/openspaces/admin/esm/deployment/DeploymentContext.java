package org.openspaces.admin.esm.deployment;

import java.io.Serializable;

public class DeploymentContext implements Serializable {
    
    private IsolationLevel isolationLevel = IsolationLevel.DEDICATED;
    private String minMemory = "1GB";
    private String maxMemory = "10GB";
    private String jvmSize = "512MB";
    private boolean highlyAvailable;
    
    DeploymentContext(){
    }

    public void setMinMemory(String minMemory) {
        this.minMemory = minMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    public void setJvmSize(String jvmSize) {
        this.jvmSize = jvmSize;
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

    public String getJvmSize() {
        return jvmSize;
    };
}
