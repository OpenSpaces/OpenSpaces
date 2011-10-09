package org.openspaces.grid.gsm.rebalancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

class RebalancingSlaEnforcementState {
    
    // futures used to keep track of ongoing service grid PU instance deployments
    private final Map<ProcessingUnit, List<FutureStatefulProcessingUnitInstance>> futureStatefulDeploymentPerProcessingUnit;
    private final Map<ProcessingUnit, List<FutureStatelessProcessingUnitInstance>> futureStatelessDeploymentPerProcessingUnit;
    private final List<FutureStatefulProcessingUnitInstance> failedStatefulDeployments;
    private final List<FutureStatelessProcessingUnitInstance> failedStatelessDeployments;
    private final List<ProcessingUnitInstance> removedStatelessProcessingUnitInstances;

    // tracing used for component testing expected result validation
    private boolean tracingEnabled = false;
    private final List<FutureStatefulProcessingUnitInstance> doneFutureStatefulDeployments;
    private final List<FutureStatelessProcessingUnitInstance> doneFutureStatelessDeployments;

    
    public RebalancingSlaEnforcementState() {
        
        futureStatefulDeploymentPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureStatefulProcessingUnitInstance>>();
        futureStatelessDeploymentPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureStatelessProcessingUnitInstance>>();
        failedStatefulDeployments = new ArrayList<FutureStatefulProcessingUnitInstance>();
        failedStatelessDeployments = new ArrayList<FutureStatelessProcessingUnitInstance>();
        removedStatelessProcessingUnitInstances = new ArrayList<ProcessingUnitInstance>();
        
        doneFutureStatefulDeployments = new ArrayList<FutureStatefulProcessingUnitInstance>();
        doneFutureStatelessDeployments = new ArrayList<FutureStatelessProcessingUnitInstance>();

    }

    public void initProcessingUnit(ProcessingUnit pu) {
        futureStatefulDeploymentPerProcessingUnit.put(pu, new ArrayList<FutureStatefulProcessingUnitInstance>());
        futureStatelessDeploymentPerProcessingUnit.put(pu, new ArrayList<FutureStatelessProcessingUnitInstance>());
    }

    public void destroyProcessingUnit(ProcessingUnit pu) {
        futureStatefulDeploymentPerProcessingUnit.remove(pu);
        futureStatelessDeploymentPerProcessingUnit.remove(pu);
    }
    
    public List<FutureStatefulProcessingUnitInstance> getAllFutureStatefulProcessingUnitInstances() {
        final List<FutureStatefulProcessingUnitInstance> futures = new ArrayList<FutureStatefulProcessingUnitInstance>();

        for (final ProcessingUnit pu : this.futureStatefulDeploymentPerProcessingUnit.keySet()) {
            futures.addAll(this.futureStatefulDeploymentPerProcessingUnit.get(pu));
        }

        futures.addAll(this.failedStatefulDeployments);
        
        return futures;
    }
    
    public List<FutureStatelessProcessingUnitInstance> getAllFutureStatelessProcessingUnitInstances() {
        final List<FutureStatelessProcessingUnitInstance> futures = new ArrayList<FutureStatelessProcessingUnitInstance>();

        for (final ProcessingUnit pu : this.futureStatefulDeploymentPerProcessingUnit.keySet()) {
            futures.addAll(this.futureStatelessDeploymentPerProcessingUnit.get(pu));
        }
        futures.addAll(this.failedStatelessDeployments);
        
        return futures;
    }

    public boolean isDestroyedProcessingUnit(ProcessingUnit pu) {
        return
            futureStatefulDeploymentPerProcessingUnit.get(pu) == null || 
            futureStatelessDeploymentPerProcessingUnit.get(pu) == null;
    }

    public int getNumberOfFutureDeployments(ProcessingUnit pu) {
        return 
            futureStatefulDeploymentPerProcessingUnit.get(pu).size() + 
            futureStatelessDeploymentPerProcessingUnit.get(pu).size();
    }

    public void addFutureStatelessDeployments(Iterable<FutureStatelessProcessingUnitInstance> futureInstances) {
        for (FutureStatelessProcessingUnitInstance futureInstance : futureInstances) {
            addFutureStatelessDeployment(futureInstance);
        }
    }

    private void addFutureStatelessDeployment(FutureStatelessProcessingUnitInstance futureInstance) {
        final ProcessingUnit pu = futureInstance.getProcessingUnit();
        futureStatelessDeploymentPerProcessingUnit.get(pu).add(futureInstance);
    }
    
    public void addFutureStatefulDeployments(Iterable<FutureStatefulProcessingUnitInstance> futureInstances) {
        for (FutureStatefulProcessingUnitInstance futureInstance : futureInstances) {
            addFutureStatefulDeployment(futureInstance);
        }
    }
    
    public void addFutureStatefulDeployment(FutureStatefulProcessingUnitInstance futureInstance) {
        ProcessingUnit pu = futureInstance.getProcessingUnit();
        futureStatefulDeploymentPerProcessingUnit.get(pu).add(futureInstance);
    }

    public List<FutureStatefulProcessingUnitInstance> removeDoneFutureStatefulDeployments(ProcessingUnit pu) {
        List<FutureStatefulProcessingUnitInstance> removed = new ArrayList<FutureStatefulProcessingUnitInstance>();
        List<FutureStatefulProcessingUnitInstance> list = futureStatefulDeploymentPerProcessingUnit.get(pu);
        
        if (list == null) {
            throw new IllegalStateException("endpoint for pu " + pu.getName() + " has already been destroyed.");
        }
        
        final Iterator<FutureStatefulProcessingUnitInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureStatefulProcessingUnitInstance future = iterator.next();
            if (future.getProcessingUnit().equals(pu) &&
                future.isDone()) {

                if (tracingEnabled) {
                    doneFutureStatefulDeployments.add(future);
                }
                
                iterator.remove();
                removed.add(future);
            }
        }
        return removed;
    }

    public List<FutureStatelessProcessingUnitInstance> removeDoneFutureStatelessDeployments(ProcessingUnit pu) {
        List<FutureStatelessProcessingUnitInstance> removed = new ArrayList<FutureStatelessProcessingUnitInstance>();
        List<FutureStatelessProcessingUnitInstance> list = futureStatelessDeploymentPerProcessingUnit.get(pu);
        
        if (list == null) {
            throw new IllegalStateException("endpoint for pu " + pu.getName() + " has already been destroyed.");
        }
        
        final Iterator<FutureStatelessProcessingUnitInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureStatelessProcessingUnitInstance future = iterator.next();
            if (future.getProcessingUnit().equals(pu) &&
                future.isDone()) {

                if (tracingEnabled) {
                    doneFutureStatelessDeployments.add(future);
                }
                
                iterator.remove();
                removed.add(future);
            }
        }
        return removed;
    }
    
    public void addFailedStatefulDeployment(FutureStatefulProcessingUnitInstance future) {
        failedStatefulDeployments.add(future);        
    }

    public void addFailedStatelessDeployment(FutureStatelessProcessingUnitInstance future) {
        failedStatelessDeployments.add(future);
    }

    public Iterable<FutureStatelessProcessingUnitInstance> getFailedStatelessDeployments(ProcessingUnit pu) {
        
        List<FutureStatelessProcessingUnitInstance> failedDeployments = new ArrayList<FutureStatelessProcessingUnitInstance>();
        
        for (FutureStatelessProcessingUnitInstance future : failedStatelessDeployments) {
            if (future.getProcessingUnit().equals(pu)) {
                failedDeployments.add(future);
            }
        }
        
        return failedDeployments;
    }
    
    public Iterable<FutureStatefulProcessingUnitInstance> getFailedStatefulDeployments(ProcessingUnit pu) {
        
        List<FutureStatefulProcessingUnitInstance> failedDeployments = new ArrayList<FutureStatefulProcessingUnitInstance>();
        
        for (FutureStatefulProcessingUnitInstance future : failedStatefulDeployments) {
            if (future.getProcessingUnit().equals(pu)) {
                failedDeployments.add(future);
            }
        }
        
        return failedDeployments;
    }

    public void removeFailedFutureStatelessDeployment(FutureStatelessProcessingUnitInstance future) {
        failedStatelessDeployments.remove(future);
    }
    
    public void removeFailedFutureStatefulDeployment(FutureStatefulProcessingUnitInstance future) {
        failedStatefulDeployments.remove(future);
    }

    public void enableTracing() {
        tracingEnabled = true;
    }

    public List<FutureStatefulProcessingUnitInstance> getDoneFutureStatefulDeployments() {
        return doneFutureStatefulDeployments;
    }

    public Iterable<ProcessingUnitInstance> getRemovedStatelessProcessingUnitInstances(ProcessingUnit pu) {
        
        List<ProcessingUnitInstance> removedInstances = new ArrayList<ProcessingUnitInstance>();
        
        for (ProcessingUnitInstance instance : removedStatelessProcessingUnitInstances) {
            if (instance.getProcessingUnit().equals(pu)) {
                removedInstances.add(instance);
            }
        }
        
        return removedInstances;
    }

    public void removeRemovedStatelessProcessingUnitInstance(ProcessingUnitInstance instance) {
        removedStatelessProcessingUnitInstances.remove(instance);
    }

    public void addRemovedStatelessProcessingUnitInstance(ProcessingUnitInstance instance) {
        removedStatelessProcessingUnitInstances.add(instance);
    }

    public boolean isStatelessProcessingUnitInstanceBeingRemoved(ProcessingUnitInstance instance) {
        return removedStatelessProcessingUnitInstances.contains(instance);
    }
    
}
