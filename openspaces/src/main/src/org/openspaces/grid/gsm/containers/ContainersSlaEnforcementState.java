package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;



class ContainersSlaEnforcementState {

    // State shared by all endpoints.
    private final Map<ProcessingUnit, List<GridServiceContainer>> containersMarkedForShutdownPerProcessingUnit;
    private final Map<ProcessingUnit, List<FutureGridServiceContainer>> futureContainersPerProcessingUnit;
    private final List<FutureGridServiceContainer> failedFutureContainers;

    public ContainersSlaEnforcementState() {
        this.containersMarkedForShutdownPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceContainer>>();
        this.futureContainersPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureGridServiceContainer>>();
        this.failedFutureContainers = new ArrayList<FutureGridServiceContainer>();
    }

    public void initProcessingUnit(ProcessingUnit pu) {
        containersMarkedForShutdownPerProcessingUnit.put(pu, new ArrayList<GridServiceContainer>());
        futureContainersPerProcessingUnit.put(pu, new ArrayList<FutureGridServiceContainer>());
    }

    public void destroyProcessingUnit(ProcessingUnit pu) {
        containersMarkedForShutdownPerProcessingUnit.remove(pu);
        futureContainersPerProcessingUnit.remove(pu);
    }


    public boolean isProcessingUnitDestroyed(ProcessingUnit pu) {

        return
            containersMarkedForShutdownPerProcessingUnit.get(pu) == null ||
            futureContainersPerProcessingUnit.get(pu) == null;
    }
    /**
     * @return true if there is pending grid service container allocation on the machine.
     */
    public boolean isFutureGridServiceContainerOnMachine(Machine machine) {

        for (List<FutureGridServiceContainer> futures : this.futureContainersPerProcessingUnit.values()) {
            for (FutureGridServiceContainer future : futures) {
                if (future.getGridServiceAgent().getMachine().equals(machine)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<FutureGridServiceContainer> removeAllDoneFutureContainers(ProcessingUnit pu) {
        
        List<FutureGridServiceContainer> removed = new ArrayList<FutureGridServiceContainer>();
        
        final Iterator<FutureGridServiceContainer> iterator = futureContainersPerProcessingUnit.get(pu).iterator();
        while (iterator.hasNext()) {
            FutureGridServiceContainer future = iterator.next();

            if (future.isDone()) {
                removed.add(future);
                iterator.remove();
            }
        }
        
        return removed;
    }


    public Collection<GridServiceContainer> getContainersMarkedForDeallocation(ProcessingUnit pu) {
        return Collections.unmodifiableCollection(
                    new ArrayList<GridServiceContainer>(containersMarkedForShutdownPerProcessingUnit.get(pu)));
    }

    public void failedFutureContainer(FutureGridServiceContainer future) {
        failedFutureContainers.add(future);
    }

    public Collection<FutureGridServiceContainer> getFailedFutureContainers() {
        return Collections.unmodifiableCollection(
                new ArrayList<FutureGridServiceContainer>(failedFutureContainers));
    }

    public void removeFailedFuture(FutureGridServiceContainer future) {
        failedFutureContainers.remove(future);
    }

    public int getNumberOfContainersMarkedForShutdown(ProcessingUnit pu) {
        return containersMarkedForShutdownPerProcessingUnit.get(pu).size();
    }

    public int getNumberOfFutureContainers(ProcessingUnit pu) {
        return futureContainersPerProcessingUnit.get(pu).size();
    }

    public void unmarkForShutdownContainer(ProcessingUnit pu, GridServiceContainer container) {
        containersMarkedForShutdownPerProcessingUnit.get(pu).remove(container);
    }
    
    public void markContainerForDeallocation(ProcessingUnit pu, GridServiceContainer container) {
        List<GridServiceContainer> containersMarkedForShutdown = containersMarkedForShutdownPerProcessingUnit.get(pu);
        if (!containersMarkedForShutdown.contains(container)) {
            containersMarkedForShutdown.add(container);
        }
    }

    public Collection<FutureGridServiceContainer> getFutureContainers(ProcessingUnit pu) {
        return Collections.unmodifiableCollection(
                new ArrayList<FutureGridServiceContainer>(futureContainersPerProcessingUnit.get(pu)));
    }

    public void addFutureContainer(ProcessingUnit pu, FutureGridServiceContainer future) {
        futureContainersPerProcessingUnit.get(pu).add(future);        
    }    
}
