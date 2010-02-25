package org.openspaces.grid.esm;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.zone.Zone;

public class ZoneCorrelator {
    List<Machine> machines = new ArrayList<Machine>();
    List<GridServiceContainer> gridServiceContainers = new ArrayList<GridServiceContainer>();
    
    
    public ZoneCorrelator(Admin admin, String zoneName) {
        Zone zone = admin.getZones().getByName(zoneName);
        if (zone != null) {
            GridServiceContainers gscsInZone = zone.getGridServiceContainers();
            for (GridServiceContainer gsc : gscsInZone) {
                gridServiceContainers.add(gsc);
                if (!machines.contains(gsc.getMachine())) {
                    machines.add(gsc.getMachine());
                }
            }
        }
        
        for (Machine machine : admin.getMachines()) {
            if (!machines.contains(machine))
                machines.add(machine);
        }
    }
    
    public List<Machine> getMachines() {
        return machines;
    }
    
    public List<GridServiceContainer> getGridServiceContainers() {
        return gridServiceContainers;
    }
    
    public List<GridServiceContainer> getGridServiceContainersByMachine(Machine machine) {
        List<GridServiceContainer> list = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer gsc : gridServiceContainers) {
            if (machine.equals(gsc.getMachine())) {
                list.add(gsc);
            }
        }
        return list;
    }
}
