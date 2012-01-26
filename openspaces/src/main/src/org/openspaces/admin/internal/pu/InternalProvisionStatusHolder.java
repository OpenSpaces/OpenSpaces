package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProvisionStatus;

public class InternalProvisionStatusHolder {
    private ProvisionStatus prevProvisionStatus;
    private ProvisionStatus newProvisionStatus;
    
    public void setNewProvisionStatus(ProvisionStatus newProvisionStatus) {
        this.newProvisionStatus = newProvisionStatus;
    }

    public ProvisionStatus getNewProvisionStatus() {
        return newProvisionStatus;
    }
    
    public void setPrevProvisionStatus(ProvisionStatus prevProvisionStatus) {
        this.prevProvisionStatus = prevProvisionStatus;
    }

    
    public ProvisionStatus getPrevProvisionStatus() {
        return prevProvisionStatus;
    }
}
