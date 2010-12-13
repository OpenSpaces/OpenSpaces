package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.core.util.MemoryUnit;

public class ContainersSlaUtils {

    static FutureGridServiceContainer startGridServiceContainerAsync(
            final InternalAdmin admin,
            final InternalGridServiceAgent gsa,
            final GridServiceContainerOptions options, 
            final long timeoutDuration, final TimeUnit timeoutUnit) {

        final AtomicReference<Object> ref = new AtomicReference<Object>(null);
        final long startTimestamp = System.currentTimeMillis();
        final long endTimestamp = startTimestamp + timeoutUnit.toMillis(timeoutDuration);
        
        admin.scheduleAdminOperation(new Runnable() {
            public void run() {
                try {
                    ref.set(new Integer(gsa.internalStartGridService(options)));
                    
                } catch (Exception e) {
                    ref.set(e);
                }
            }
        });
        
        FutureGridServiceContainer future = new FutureGridServiceContainer() {

            public GridServiceContainer getGridServiceContainer() throws ExecutionException, IllegalStateException,
                    TimeoutException {
                
                if (System.currentTimeMillis() > endTimestamp) {
                    throw new TimeoutException("Starting a new container on machine "+ gsa.getMachine().getHostAddress() + " took more than " + timeoutUnit.toSeconds(timeoutDuration) + " seconds to complete.");
                }
                
                Object result = ref.get();
                
                if (result == null) {
                    throw new IllegalStateException("Async operation is not done yet.");
                }
                
                if (result instanceof Exception) {
                    throw new ExecutionException((Exception)result);
                }
                
                GridServiceContainer container = getGridServiceContainerInternal((Integer)result);
                if (container != null) {
                    return container;
                }
                                
                throw new IllegalStateException("Async operation is not done yet.");
            }

            public boolean isDone() {
                Object result = ref.get();
                
                if (System.currentTimeMillis() > endTimestamp) {
                    return true;
                }
                
                if (result == null) {
                    return false;
                }
                
                if (result instanceof Exception) {
                    return true;
                }
                                
                GridServiceContainer container = getGridServiceContainerInternal((Integer)result);
                if (container != null) {
                    return true;
                }
                
                
                
                return false;                
           }
            
            public GridServiceContainer getGridServiceContainerInternal(int agentId) {
                for (GridServiceContainer container : admin.getGridServiceContainers()) {
                    
                    String agentUid = ((InternalGridServiceContainer) container).getAgentUid();
                    if (agentUid != null && agentUid.equals(gsa.getUid())) {
                        if (agentId == container.getAgentId()) {
                            return container;
                        }
                    }
                }
                return null;
            }

            public GridServiceAgent getGridServiceAgent() {
                return gsa;
            }

            public GridServiceContainerOptions getGridServiceContainerOptions() {
                return options;
            }

            public long getTimestamp() {
                return startTimestamp;
            }
            
        };
        
        return future;
    }
    
    /**
     * @param options - grid service container options
     * @return the maximum JVM heap size or 0 if not found.
     */
    static int getMaximumJavaHeapSizeInMB(GridServiceContainerOptions options) {
        int maximumJavaHeapSizeMegabytes = 0;
        String maxMemory = options.getOptions().getMaximumJavaHeapSize();
        if (maxMemory != null) {
            maximumJavaHeapSizeMegabytes = (int) MemoryUnit.toMegaBytes(maxMemory);
        }
        return maximumJavaHeapSizeMegabytes;
     
    }

    /**
     * @param options - grid service container options
     * @return the grid service container zones
     */
    static String[] getZones(GridServiceContainerOptions options) {
        return options.getOptions().getZones().split(",");
    }
    
    
    static List<GridServiceContainer> getContainersByZone(String zone, Admin admin) {
        List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            if (isContainerMatchesZone(container,zone)) {
                containers.add(container);
            }
        }
        return containers;
    }

    static List<GridServiceContainer> getContainersByZoneOnMachine(String zone, Machine machine) {
        List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer container : machine.getGridServiceContainers()) {
            if (isContainerMatchesZone(container,zone)) {
                containers.add(container);
            }
        }
        return containers;
    }
    
    private static boolean isContainerMatchesZone(GridServiceContainer container, String zone) {
        return container.getZones().containsKey(zone);
    }

}
