package org.openspaces.dsl.context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.pu.ProcessingUnitInstance;


public class ServiceInstance {

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(ServiceInstance.class.getName());
    private final ProcessingUnitInstance pui;

    ServiceInstance(final ProcessingUnitInstance pui) {
        this.pui = pui;
    }

    public int getInstanceID() {
        if (pui != null) {
            return pui.getInstanceId();
        } else {
            return 1;
        }
    }

    public String getHostAddress() {
        if (pui != null) {
            return pui.getMachine().getHostAddress();
        } else {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.log(Level.SEVERE, "Failed to read local host address", e);
                return null;
            }
        }
    }

    public String getHostName() {
        
        if (pui != null) {
            return pui.getMachine().getHostName();
        } else {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.log(Level.SEVERE, "Failed to read local host address", e);
                return null;
            }
        }
    }

    public void invoke(String commandName) {
        throw new UnsupportedOperationException("Invoke not supported yet!");
    }

    @Override
    public String toString() {
        return "ServiceInstance [getInstanceID()=" + getInstanceID() + ", getHostAddress()=" + getHostAddress()
                + ", getHostName()=" + getHostName() + "]";
    }

    
}
