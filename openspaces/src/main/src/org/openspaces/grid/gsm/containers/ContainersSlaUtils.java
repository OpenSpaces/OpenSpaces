/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.vm.VirtualMachineAware;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;

public class ContainersSlaUtils {

    static FutureGridServiceContainer startGridServiceContainerAsync(
            final InternalAdmin admin,
            final InternalGridServiceAgent gsa,
            final GridServiceContainerConfig config,
            final Log logger,
            final long duration, final TimeUnit timeUnit) {

        final AtomicReference<Object> ref = new AtomicReference<Object>(null);
        final long startTimestamp = System.currentTimeMillis();
        final long end = startTimestamp + timeUnit.toMillis(duration);
        
        admin.scheduleAdminOperation(new Runnable() {
            public void run() {
                try {
                    final OperatingSystemStatistics operatingSystemStatistics = 
                        gsa.getMachine().getOperatingSystem().getStatistics();

                    // get total free system memory + cached (without sigar returns -1)
                    long freeBytes = operatingSystemStatistics.getActualFreePhysicalMemorySizeInBytes();
                    if (freeBytes <= 0) {
                        // fallback - no sigar. Provides a pessimistic number since does not take into
                        // account OS cache that can be allocated.
                        freeBytes = operatingSystemStatistics.getFreePhysicalMemorySizeInBytes();
                        if (freeBytes <= 0) {
                            // machine is probably going down.
                            ref.set(new AdminException("Cannot determine machine " + machineToString(gsa.getMachine()) + " free memory."));
                        }
                    }

                    final long freeInMB = MemoryUnit.MEGABYTES.convert(freeBytes, MemoryUnit.BYTES);
                    if (freeInMB < config.getMaximumJavaHeapSizeInMB()) {
                        ref.set(new AdminException("Machine " + machineToString(gsa.getMachine()) + " free memory " + freeInMB +"MB is not enough to start a container with " + config.getMaximumJavaHeapSizeInMB() + "MB. Free machine memory or increase machine provisioning reservedMemoryPerMachine property."));
                    }
                    else {
                        ref.set(gsa.internalStartGridService(config));
                    }
                } catch (AdminException e) {
                    ref.set(e);
                } catch (Throwable e) {
                    logger.error("Unexpected Exception " + e.getMessage(),e);
                    ref.set(e);
                }
            }
        });
        
        FutureGridServiceContainer future = new FutureGridServiceContainer() {

            public boolean isTimedOut() {
                return System.currentTimeMillis() > end;
            }

            public ExecutionException getException() {
                Object result = ref.get();
                if (result != null && result instanceof Throwable) {
                    return new ExecutionException((Throwable)result);
                }
                return null;
            }
            
            public GridServiceContainer get() throws ExecutionException, IllegalStateException,
                    TimeoutException {
                
                Object result = ref.get();
                
                if (getException() != null) {
                    throw getException();
                }
                
                GridServiceContainer container = null;
                if (result != null) {
                    int agentId = (Integer)result;
                    container = getGridServiceContainerInternal(agentId);
                    //container could still be null if not discovered
                }
                
                if (container == null) {
                    if (isTimedOut()) {
                        throw new TimeoutException(
                                "Starting a new container took more than "
                                        + timeUnit.toSeconds(duration)
                                        + " seconds to complete.");
                    }
                
                    throw new IllegalStateException(
                            "Async operation is not done yet.");
                }
                
                return container;            
                
            }

            public boolean isDone() {
                Object result = ref.get();
                
                if (System.currentTimeMillis() > end) {
                    return true;
                }
                
                if (result == null) {
                    return false;
                }
                
                if (result instanceof Throwable) {
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

            @Override
            public int getAgentId() throws ExecutionException, TimeoutException  {
                ExecutionException exception = getException();
                if (exception != null) {
                    throw exception;
                }
                
                if (isTimedOut() && ref.get() == null) {
                    throw new TimeoutException("Starting a new container on machine "+ gsa.getMachine().getHostAddress() + " took more than " + timeUnit.toSeconds(duration) + " seconds to complete.");
                }
               
                if (ref.get() == null) {
                    throw new IllegalStateException("Async operation is not done yet.");
                }
                
                return (Integer)ref.get();
            }
            
            public boolean isStarted() {
                return ref.get() != null;
            }
        };
        
        return future;
    }
    
    public static Collection<GridServiceContainer> getContainersByZone(Admin admin, String zone) {
        List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            if (isContainerMatchesZone(container,zone) && container.getGridServiceAgent() != null) {
                containers.add(container);
            }
        }
        return containers;
    }

    public static List<GridServiceContainer> getContainersByZoneOnAgentUid(Admin admin, String zone, String agentUid) {
        List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            if (isContainerMatchesZone(container,zone) && 
                container.getGridServiceAgent() != null && 
                container.getGridServiceAgent().getUid().equals(agentUid)) {
                
                containers.add(container);
            }
        }
        return containers;
    }
    
    public static boolean isContainerMatchesZone(GridServiceContainer container, String zone) {
        return container.getZones().containsKey(zone);
    }
    
    
    public static long getMemoryInMB(GridServiceContainer container) {
        
        String prefix = "-Xmx";
        String xmxArgument = getCommandLineArgumentRemovePrefix(container,prefix);
        if (xmxArgument == null) {
            throw new IllegalStateException("Container " + gscToString(container) + " does not have an -Xmx commandline argument. If it was started manually please close it.");
        }
        return MemoryUnit.MEGABYTES.convert(xmxArgument);
    }
    
    public static String getCommandLineArgumentRemovePrefix(VirtualMachineAware container, String prefix) {
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

    public static String machineToString(Machine machine) {
        return MachinesSlaUtils.machineToString(machine);
    }
    
    public static String gscToString(GridComponent container) {
        String[] zones = container.getZones().keySet().toArray(new String[container.getZones().keySet().size()]);
        return "pid["+container.getVirtualMachine().getDetails().getPid()+"] host["+machineToString(container.getMachine())+"] zones [" + Arrays.toString(zones) +"]";
    }

    public static String gscsToString(GridServiceContainer[] containers) {
        String[] containersToString = new String[containers.length];
        for (int i = 0 ; i < containersToString.length ; i++) {
            containersToString[i] = gscToString(containers[i]);
        }
        return Arrays.toString(containersToString);
    }

    public static String gscsToString(List<GridServiceContainer> containers) {
        return gscsToString(containers.toArray(new GridServiceContainer[containers.size()]));
    }
}
