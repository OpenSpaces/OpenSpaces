package org.openspaces.admin.internal.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.pu.DefaultProcessingUnits;
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

public class DefaultApplication implements InternalApplication {

    private final InternalProcessingUnits processingUnits;
    private final String name;
    private final InternalAdmin admin;
    
    public DefaultApplication(InternalAdmin admin, String name) {
        if (name == null) {
            throw new IllegalArgumentException("Application name cannot be null");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Application name cannot be empty");
        }
        this.processingUnits = new DefaultProcessingUnits(admin);
        this.name = name;
        this.admin = admin;
    }

    @Override
    public ProcessingUnits getProcessingUnits() {
        return processingUnits;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultApplication that = (DefaultApplication) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public void undeployAndWait() {
        undeployAndWait(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
        
    }

    @Override
    public boolean undeployAndWait(long timeout, TimeUnit timeUnit) {
        
        long end = System.currentTimeMillis()  + timeUnit.toMillis(timeout);
        boolean timedOut = false;
        
        final Map<String, List<String>> puReverseDependencies = getReverseDependencies();
        
        while (true) {
            List<ProcessingUnit> pusToUndeploy = new ArrayList<ProcessingUnit>();
            
            for (ProcessingUnit pu : this.getProcessingUnits()) {
                
                boolean postponeUndeployment = false;
                                
                for (String dependantPuName : puReverseDependencies.get(pu.getName())) {
                    ProcessingUnit dependantPu = admin.getProcessingUnits().getProcessingUnit(dependantPuName);
                    if (dependantPu != null && 
                        dependantPu.getApplication() != null &&
                        dependantPu.getApplication().getName().equals(getName())) {
                        //there are other that depend on pu, we need to undeploy them first
                        postponeUndeployment = true;
                    }
                }
                
                if (!postponeUndeployment) {
                    pusToUndeploy.add(pu);
                }
            }
            
            if (this.getProcessingUnits().getSize() == 0) {
                //success
                break;
            }
            
            if (pusToUndeploy.size() == 0) {
                throw new AdminException("Application undeployment does not support cyclic dependencies");
            }
            
            long remaining = end - System.currentTimeMillis();
            if (remaining <= 0) {
                timedOut = true;
                break;
            }
            
            InternalGridServiceManagers gsms = (InternalGridServiceManagers)admin.getGridServiceManagers();
            if (!gsms.undeployProcessingUnitsAndWait(pusToUndeploy.toArray(new ProcessingUnit[pusToUndeploy.size()]),remaining,TimeUnit.MILLISECONDS)) {
                timedOut = true;
                break;
            }
        }
        
        return !timedOut;
    }

    /**
     * @return a map from a pu name to a list of pus that depend on it.
     */
    private Map<String, List<String>> getReverseDependencies() {
        ProcessingUnit[] processingUnits = getProcessingUnits().getProcessingUnits();
        
        //initialize map entry for each pu
        final Map<String,List<String>> puReverseDependencies = new HashMap<String,List<String>>();
        for (final ProcessingUnit pu : processingUnits) {
            if (!puReverseDependencies.containsKey(pu.getName())) {
                puReverseDependencies.put(pu.getName(), new LinkedList<String>());
            }
        }
        
        //populate required pus
        for (final ProcessingUnit pu : processingUnits) {
            
            final String dependantPuName = pu.getName();
            
            for (final String requiredPuName : pu.getDependencies().getDependenciesRequiredProcessingUnitNames()) {
                final List<String> dependantPUs = puReverseDependencies.get(requiredPuName);
                if (!dependantPUs.contains(dependantPuName)) {
                    dependantPUs.add(dependantPuName);
                }
            }
        }
        return puReverseDependencies;
    }
    
    @Override
    public ProcessingUnit deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment, long timeout, TimeUnit timeUnit) {
        return ((InternalGridServiceManagers)admin.getGridServiceManagers()).deploy(this, puDeployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment) {
        return deployProcessingUnit(puDeployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

}
