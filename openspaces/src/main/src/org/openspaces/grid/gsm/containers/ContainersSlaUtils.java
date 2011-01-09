package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.esm.ToStringHelper;

public class ContainersSlaUtils {

    static FutureGridServiceContainer startGridServiceContainerAsync(
            final InternalAdmin admin,
            final InternalGridServiceAgent gsa,
            final GridServiceContainerConfig config, 
            final long timeoutDuration, final TimeUnit timeoutUnit) {

        final AtomicReference<Object> ref = new AtomicReference<Object>(null);
        final long startTimestamp = System.currentTimeMillis();
        final long endTimestamp = startTimestamp + timeoutUnit.toMillis(timeoutDuration);
        
        admin.scheduleAdminOperation(new Runnable() {
            public void run() {
                try {
                    ref.set(gsa.internalStartGridService(config));
                    
                } catch (Exception e) {
                    ref.set(e);
                }
            }
        });
        
        FutureGridServiceContainer future = new FutureGridServiceContainer() {

            public boolean isTimedOut() {
                return System.currentTimeMillis() > endTimestamp;
            }


            public ExecutionException getException() {
                Object result = ref.get();
                if (result instanceof Exception) {
                    return new ExecutionException((Exception)result);
                }
                return null;
            }
            
            public GridServiceContainer get() throws ExecutionException, IllegalStateException,
                    TimeoutException {
                
                if (isTimedOut()) {
                    throw new TimeoutException("Starting a new container on machine "+ gsa.getMachine().getHostAddress() + " took more than " + timeoutUnit.toSeconds(timeoutDuration) + " seconds to complete.");
                }
                
                ExecutionException exception = getException();
                if (exception != null) {
                    throw exception;
                }
               
                Object result = ref.get();
                GridServiceContainer container = null;
                if (result != null) {
                    container = getGridServiceContainerInternal((Integer)result);
                }
                
                if (container == null) {
                    throw new IllegalStateException("Async operation is not done yet.");
                }
                
                return container;            
                
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

            public GridServiceContainerConfig getGridServiceContainerConfig() {
                return config;
            }

            public Date getTimestamp() {
                return new Date(startTimestamp);
            }
        };
        
        return future;
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
    
    public static int getMachineShortage(
            ContainersSlaPolicy sla,
            Iterable<GridServiceContainer> containers) {
               
        return getFutureMachineShortage(sla, containers, new ArrayList<FutureGridServiceContainer>());
    }

    public static double getCpuCapacityShortage(
            ContainersSlaPolicy sla,
            Iterable<GridServiceContainer> containers) {
        
        return getFutureCpuCapacityShortage(sla,containers, new ArrayList<FutureGridServiceContainer>());
    }

    public static long getMemoryCapacityShortageInMB(
            ContainersSlaPolicy sla,
            Iterable<GridServiceContainer> containers) {
        
        return getFutureMemoryCapacityShortageInMB(sla, containers, new ArrayList<FutureGridServiceContainer>());
    }
   
    
    private static double getCpu(Machine machine) {
        return machine.getOperatingSystem().getDetails().getAvailableProcessors();
    }
    
    private static String getCommandLineArgumentRemovePrefix(GridServiceContainer container, String prefix) {
        String[] commandLineArguments = container.getVirtualMachine().getDetails().getInputArguments();
        String requiredArg = null;
        for (final String arg : commandLineArguments) {
            if (arg.startsWith(prefix)) {
                requiredArg = arg;
            }
        }
        
        if (requiredArg != null) {
            return requiredArg.substring(prefix.length());
        }
        return null;
    }
    
    private static long getMemoryInMB(GridServiceContainer container) {
        
        String prefix = "-Xmx";
        String xmxArgument = getCommandLineArgumentRemovePrefix(container,prefix);
        if (xmxArgument == null) {
            throw new IllegalStateException("Container " + ToStringHelper.gscToString(container) + " does not have an -Xmx commandline argument.");
        }
        return MemoryUnit.MEGABYTES.convert(xmxArgument);
    }
    
    public static long getFutureMemoryCapacityShortageInMB(
            ContainersSlaPolicy sla, 
            Iterable<GridServiceContainer> containers,
            Iterable<FutureGridServiceContainer> futureContainers) {
        
        long memoryShortageInMB = sla.getMemoryCapacityInMB();
        
        for (GridServiceContainer container : containers) {
            memoryShortageInMB -= getMemoryInMB(container);
        }
        
        for (FutureGridServiceContainer future : futureContainers) {
            memoryShortageInMB -= future.getGridServiceContainerConfig().getMaximumJavaHeapSizeInMB();
        }
        return memoryShortageInMB;
    }

    public static double getFutureCpuCapacityShortage(
            ContainersSlaPolicy sla, 
            Iterable<GridServiceContainer> containers,
            Iterable<FutureGridServiceContainer> futureContainers) {

        double cpuShortage = sla.getCpuCapacity();
        
        for (Machine machine : getFutureMachinesHostingContainers(containers, futureContainers)) {
            cpuShortage -= getCpu(machine);
        }
        
        return cpuShortage;
    }

    private static Set<Machine> getFutureMachinesHostingContainers(Iterable<GridServiceContainer> containers,
            Iterable<FutureGridServiceContainer> futureContainers) {
        final Set<Machine> machines = new HashSet<Machine>();
        for (final GridServiceContainer container : containers) {
            machines.add(container.getMachine());
        }
        for (final FutureGridServiceContainer futureContainer : futureContainers) {
            final GridServiceAgent agent = futureContainer.getGridServiceAgent();
            if (agent.isDiscovered()) {
                machines.add(agent.getMachine());
            }
        }
        return machines;
    }

    public static int getFutureMachineShortage(
            ContainersSlaPolicy sla, 
            Iterable<GridServiceContainer> containers,
            Iterable<FutureGridServiceContainer> futureContainers) {
        return sla.getMinimumNumberOfMachines() - getFutureMachinesHostingContainers(containers, futureContainers).size();
    }

    /**
     * Sorts the specified agents by the number of containers (from the specified containers) on each agent.
     * @param agents
     * @param containers
     * @return the sorted agents list
     */
    public static List<GridServiceAgent> sortAgentsByNumberOfContainers(
            GridServiceAgent[] agents, 
            List<GridServiceContainer> containers) {

        final Map<GridServiceAgent,Integer> numberOfContainersPerAgent = new HashMap<GridServiceAgent,Integer>();
        for (GridServiceAgent agent: agents) {
            numberOfContainersPerAgent.put(agent, 0);
        }
        for (GridServiceContainer container : containers) {
            GridServiceAgent agent = container.getGridServiceAgent();
            int count = numberOfContainersPerAgent.get(agent);
            numberOfContainersPerAgent.put(agent, count+1);
        }
        
        List<GridServiceAgent> sortedAgents = new ArrayList<GridServiceAgent>(Arrays.asList(agents));
        Collections.sort(sortedAgents,new Comparator<GridServiceAgent>() {

            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                return numberOfContainersPerAgent.get(agent1) - numberOfContainersPerAgent.get(agent2);
            }
        });
        
        return sortedAgents;
    }
    
    public static String getContainerZone(ProcessingUnit pu) {
        String[] zones = pu.getRequiredZones();
        if (zones.length == 0) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with no zones defined.");
        }

        if (zones.length > 1) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with " + zones.length + " zones : " + Arrays.toString(zones));
        }

        return zones[0];
    }

    public static ProcessingUnit findProcessingUnitWithSameZone(Set<ProcessingUnit> processingUnits, ProcessingUnit pu) {
        for (final ProcessingUnit endpointPu : processingUnits) {
            if (ContainersSlaUtils.getContainerZone(endpointPu).equals(ContainersSlaUtils.getContainerZone(pu))) {
                return endpointPu;
            }
        }
        return null;
    }

    public static ProcessingUnit findProcessingUnitWithSameName(Set<ProcessingUnit> processingUnits, ProcessingUnit pu) {
        for (final ProcessingUnit endpointPu : processingUnits) {
            if (endpointPu.getName().equals(pu.getName())) {
                return endpointPu;
            }
        }
        return null;
    }

    public static boolean isCapacityMet(ContainersSlaPolicy sla, Iterable<GridServiceContainer> containers) {
        return ContainersSlaUtils.getMemoryCapacityShortageInMB(sla, containers) <= 0
                && ContainersSlaUtils.getCpuCapacityShortage(sla, containers) <= 0
                && ContainersSlaUtils.getMachineShortage(sla, containers) <= 0;
    }

    public static boolean isFutureCapacityMet(ContainersSlaPolicy sla, Iterable<GridServiceContainer> containers,
            Iterable<FutureGridServiceContainer> futureContainers) {
        return ContainersSlaUtils.getFutureMemoryCapacityShortageInMB(sla, containers, futureContainers) <= 0
                && ContainersSlaUtils.getFutureCpuCapacityShortage(sla, containers, futureContainers) <= 0
                && ContainersSlaUtils.getFutureMachineShortage(sla, containers, futureContainers) <= 0;
    }

}
