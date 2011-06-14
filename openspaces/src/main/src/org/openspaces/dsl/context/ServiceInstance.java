package org.openspaces.dsl.context;

import org.openspaces.admin.pu.ProcessingUnitInstance;

public class ServiceInstance {

    private final ProcessingUnitInstance pui;

    ServiceInstance(final ProcessingUnitInstance pui) {
        this.pui = pui;
    }

    public int getInstanceID() {
        return pui.getInstanceId();
    }
    
    public String getHostAddress() {
        return pui.getMachine().getHostAddress();
    }
    
    public String getHostName() {        
        return pui.getMachine().getHostName();
    }
    
    public void invoke(String commandName) {
        throw new UnsupportedOperationException("Invoke not supported yet!");
    }

}
