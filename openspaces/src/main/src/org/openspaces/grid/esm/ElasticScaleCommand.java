package org.openspaces.grid.esm;

import java.util.List;

import org.openspaces.admin.machine.Machine;

public class ElasticScaleCommand {
    private List<Machine> machines;
    
    //package level
    void setMachines(List<Machine> machines) {
        this.machines = machines;
    }
    
    public List<Machine> getMachines() {
        return machines;
    }
    
}
