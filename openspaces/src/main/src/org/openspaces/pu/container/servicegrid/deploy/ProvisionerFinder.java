package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceItem;

import java.rmi.MarshalledObject;
import java.rmi.RMISecurityManager;
import java.security.Permission;

/**
 * Date: Jul 30, 2005
 * Time: 5:56:08 PM
 */
public class ProvisionerFinder {
// -------------------------- STATIC METHODS --------------------------

    public static GSM find(String name, long wait, String[] groups) {
        ServiceItem result = ServiceFinder.find(name, GSM.class, wait, groups);
        if (result != null) {
            try {
                result = (ServiceItem) new MarshalledObject(result).get();
                return (GSM) result.service;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        System.setSecurityManager(
                new RMISecurityManager() {
                    public void checkPermission(Permission perm) {
                        //all everything for testing
                    }
                }
        );


        System.out.println(find(null, 10000, null));
    }
}

