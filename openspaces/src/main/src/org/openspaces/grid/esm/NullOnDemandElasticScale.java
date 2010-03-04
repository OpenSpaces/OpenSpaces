package org.openspaces.grid.esm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openspaces.admin.machine.Machine;

public class NullOnDemandElasticScale implements OnDemandElasticScale {

    public void init(ElasticScaleConfig config) {

    }

    public boolean accept(Machine machine) {
        try {
            if (InetAddress.getLocalHost().getHostName().equals(machine.getHostName())) {
                return false;
            }
        } catch (UnknownHostException e) {
        }
        
        return true;
    }
    
    public void scaleOut(ElasticScaleCommand command) {
    }

    public void scaleIn(Machine machine) {
    }
}
