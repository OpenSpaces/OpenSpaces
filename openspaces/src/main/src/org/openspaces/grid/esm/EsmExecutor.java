package org.openspaces.grid.esm;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.deployment.DeploymentContext;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.zone.Zone;

import com.gigaspaces.internal.backport.java.util.Arrays;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import com.j_spaces.kernel.TimeUnitProperty;

import edu.emory.mathcs.backport.java.util.Collections;

public class EsmExecutor {
    
    public static void main(String[] args) {
        new EsmExecutor();
    }
    
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    private final static long initialDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.initialDelay", "1m");
    private final static long fixedDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.fixedDelay", "5s");
    private final static int memorySafetyBufferInMB = MemorySettings.valueOf(System.getProperty("org.openspaces.grid.esm.memorySafetyBuffer", "100MB")).toMB();
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    
    private final Admin admin = new AdminFactory().createAdmin();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        final ThreadFactory factory = Executors.defaultThreadFactory();
        public Thread newThread(Runnable r) {
            Thread newThread = factory.newThread(r);
            newThread.setName("ESM-ScheduledTask");
            return newThread;
        }
    });
    
    public EsmExecutor() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        
        logger.setLevel(Level.parse(System.getProperty("logger.level", "INFO")));
        logger.config("Initial Delay: " + initialDelay + " ms");
        logger.config("Fixed Delay: " + fixedDelay + " ms");
        if (Boolean.valueOf(System.getProperty("esm.enabled", "true")))
            executorService.scheduleWithFixedDelay(new ScheduledTask(), initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
    }

    public void deploy(ElasticDataGridDeployment deployment) {
        DeploymentContext context = deployment.getContext();
        final String zoneName = deployment.getDataGridName();
        SpaceDeployment spaceDeployment = new SpaceDeployment(deployment.getDataGridName());
        spaceDeployment.addZone(zoneName);
        int numberOfParitions = calculateNumberOfPartitions(context);
        if (context.isHighlyAvailable()) {
            spaceDeployment.maxInstancesPerMachine(1);
            spaceDeployment.partitioned(numberOfParitions, 1);
        } else {
            spaceDeployment.partitioned(numberOfParitions, 0);
        }
        
        spaceDeployment.setContextProperty("elastic", "true");
        spaceDeployment.setContextProperty("minMemory", context.getMinMemory());
        spaceDeployment.setContextProperty("maxMemory", context.getMaxMemory());
        spaceDeployment.setContextProperty("jvmSize", context.getJvmSize());
        spaceDeployment.setContextProperty("isolationLevel", context.getIsolationLevel().name());
        spaceDeployment.setContextProperty("sla", context.getSlaDescriptors());

        logger.finest("Deploying " + deployment.getDataGridName() 
                + "\n\t Zone: " + zoneName 
                + "\n\t Min Memory: " + context.getMinMemory()
                + "\n\t Max Memory: " + context.getMaxMemory()
                + "\n\t JVM Size: " + context.getJvmSize()
                + "\n\t Isolation Level: " + context.getIsolationLevel().name()
                + "\n\t Highly Available? " + context.isHighlyAvailable()
                + "\n\t Partitions: " + numberOfParitions
                + "\n\t SLA: " + context.getSlaDescriptors());
        
        admin.getGridServiceManagers().waitForAtLeastOne().deploy(spaceDeployment);
    }
    
    private int calculateNumberOfPartitions(DeploymentContext context) {
        int numberOfPartitions = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getJvmSize());
        if (context.isHighlyAvailable()) {
            numberOfPartitions /= 2;
        }
        
        return numberOfPartitions;
    }
    
    private final class Command {
        List<Machine> machinesToExclude;
    }
    
    private final class ScheduledTask implements Runnable {

        public void run() {
            try {
                logger.finest("ScheduledTask is running...");
                ProcessingUnits processingUnits = admin.getProcessingUnits();
                for (ProcessingUnit pu : processingUnits) {

                    logger.fine(ToStringHelper.puToString(pu));

                    if (!pu.getBeanLevelProperties().getContextProperties().containsKey("elastic"))
                        continue;

                    execute(pu);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Caught exception: " + t, t);
            }
        }

        private void execute(ProcessingUnit pu) {
            
            PuCapacityPlanner puCapacityPlanner = new PuCapacityPlanner(pu);
            logger.finest(ToStringHelper.puCapacityPlannerToString(puCapacityPlanner));
            
            if (puCapacityPlanner.getNumberOfGSCsInZone() < puCapacityPlanner.getMinNumberOfGSCs()) {
                if (startGSC(puCapacityPlanner, null)) {
                    return;
                }
            }
            
            if (!puCapacityPlanner.hasEnoughMachines()) {
                Command command = excludeMachines(puCapacityPlanner);  
                if (command == null)
                    return;
                    
                if (startGSC(puCapacityPlanner, command)) {
                    return;
                }
            }
            
            
            Command command = handle(puCapacityPlanner);
            if (command == null) {
                return;
            }
            
            if (puCapacityPlanner.getNumberOfGSCsInZone() < puCapacityPlanner.getMaxNumberOfGSCs()) {
                if (startGSC(puCapacityPlanner, command)) {
                    return;
                }
            }
            
            //reject!
        }
        
        private Command excludeMachines(PuCapacityPlanner puCapacityPlanner) {
            Zone zone = admin.getZones().getByName(puCapacityPlanner.getZoneName());
            if (zone == null) return null;
            
            List<Machine> machines = new ArrayList<Machine>();
            GridServiceContainers gscsInZone = zone.getGridServiceContainers();
            for (GridServiceContainer gsc : gscsInZone) {
                Machine machine = gsc.getMachine();
                if (!machines.contains(machine)) {
                    machines.add(machine);
                }
            }
            
            if (machines.size() == 0) {
                return null;
            }
            Command command = new Command();
            command.machinesToExclude = machines;
            return command;
        }

        private boolean startGSC(PuCapacityPlanner puCapacityPlanner, Command command) {
            
            Zone zone = admin.getZones().getByName(puCapacityPlanner.getZoneName());
            if (zone != null && !(zone.getGridServiceContainers().getSize() < puCapacityPlanner.getMaxNumberOfGSCs())) {
                logger.warning("Could not allocate a new GSC, reached scale limit of " + puCapacityPlanner.getMaxNumberOfGSCs());
                return false; //can't start GSC
            }
            
            //we limit when deployment is broken, and to amend it we rely on the min number of gscs needed
            boolean limitNumberOfGSCsPerMachine = zone == null || zone.getGridServiceContainers().getSize() < puCapacityPlanner.getMinNumberOfGSCs();
            
            List<Machine> machines = new ArrayList<Machine>(Arrays.asList(admin.getMachines().getMachines()));
            if (command != null && command.machinesToExclude != null) {
                machines.removeAll(command.machinesToExclude);
            }
            for (Machine machine : machines) {

                if (!meetsDedicatedIsolationConstaint(machine, puCapacityPlanner.getZoneName())) {
                    logger.finest("[X] Machine ["+ToStringHelper.machineToString(machine)+"] doesn't meet dedicated isolation constraint");
                    continue;
                }
                
                if (limitNumberOfGSCsPerMachine) {
                    //assumes dedicated machine!
                    if (machine.getGridServiceContainers().getSize() < puCapacityPlanner.getMaxNumberOfGSCsPerMachine()) {
                        logger.finest("[X] Machine ["+ToStringHelper.machineToString(machine)+"] is within scale limit of [" + puCapacityPlanner.getMaxNumberOfGSCsPerMachine()
                                + "] GSCs per machine, has ["+machine.getGridServiceContainers().getSize()+"] GSCs");
                    } else {
                        logger.finest("[X] Machine ["+ToStringHelper.machineToString(machine)+"] reached the scale limit of [" + puCapacityPlanner.getMaxNumberOfGSCsPerMachine()
                                + "] GSCs per machine");
                        continue;
                    }
                }
                
                GridServiceAgent agent = machine.getGridServiceAgent();
                if (agent == null)
                    continue; // to next machine

                if (machineHasAnEmptyGSC(machine)) {
                    logger.finest("Machine ["+ToStringHelper.machineToString(machine)+"] has an empty GSC, not starting a new one");
                    continue; //wait for instance to instantiate on it before starting a new one
                }
                
                if (!hasEnoughMemoryForNewGSC(puCapacityPlanner, machine)) {
                    logger.finest("[X] Machine ["+ToStringHelper.machineToString(machine)+"] doesn't have enough memory to start a new GSC");
                    continue; // can't start GSC here
                }
                
                logger.finest("[V] Scaling up, starting GSC on machine ["+ToStringHelper.machineToString(machine)+"]");
                GridServiceContainer newGSC = agent.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument(
                        "-Dcom.gs.zones=" + puCapacityPlanner.getZoneName()).vmInputArgument(
                                "-Dcom.gigaspaces.grid.gsc.serviceLimit=" + puCapacityPlanner.getScalingFactor()));
                logger.finest("[V] started GSC ["+ToStringHelper.gscToString(newGSC)+"] on machine ["+ToStringHelper.machineToString(newGSC.getMachine())+"]");
                return true; //started GSC
            }
            
            logger.finest("Can't start a GSC, needs another machine");
            return false; //can't start GSC
        }

        private boolean machineHasAnEmptyGSC(Machine machine) {
            for (GridServiceContainer gscOnMachine : machine.getGridServiceContainers()) {
                if (gscOnMachine.getProcessingUnitInstances().length == 0) {
                    return true;
                }
            }
            return false;
        }
        
        
        // requires this machine to contain only GSCs matching the zone name provided
        private boolean meetsDedicatedIsolationConstaint(Machine machine, String zoneName) {
            for (GridServiceContainer gsc : machine.getGridServiceContainers()) {
                Map<String, Zone> gscZones = gsc.getZones();
                if (gscZones.isEmpty() || !gscZones.containsKey(zoneName)) {
                    return false; // GSC either has no zone or is of another zone
                }
            }

            return true;
        }
        
        private boolean hasEnoughMemoryForNewGSC(PuCapacityPlanner puCapacityPlanner, Machine machine) {

            double totalPhysicalMemorySizeInMB = machine.getOperatingSystem()
            .getDetails()
            .getTotalPhysicalMemorySizeInMB();
            int jvmSizeInMB = MemorySettings.valueOf(puCapacityPlanner.getJvmSize()).toMB();
            int numberOfGSCsScaleLimit = (int) Math.floor(totalPhysicalMemorySizeInMB / jvmSizeInMB);
            int numberOfGSCsOnMachine = machine.getGridServiceContainers().getSize();
            int freePhysicalMemorySizeInMB = (int) Math.floor(machine.getOperatingSystem()
                    .getStatistics()
                    .getFreePhysicalMemorySizeInMB());

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("\nMachine ["+ToStringHelper.machineToString(machine)+"] memory:" + "\n\t Total physical memory: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInMB()) + " MB"
                        + "\n\t Free physical memory: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getStatistics().getFreePhysicalMemorySizeInMB()) + " MB"
                        + "\n\t Memory Saftey Buffer: "
                        + memorySafetyBufferInMB + " MB"
                        + "\n\t Total swap space: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getDetails().getTotalSwapSpaceSizeInMB()) + " MB"
                        + "\n\t Free swap space: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getStatistics().getFreeSwapSpaceSizeInMB()) + " MB"
                        + "\n\t Memory Used: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getStatistics().getPhysicalMemoryUsedPerc()) + "%"
                        + "\n\t GSC JVM Size: " + puCapacityPlanner.getJvmSize() + "\n\t GSC amount: "
                        + numberOfGSCsOnMachine + " - Scale Limit: " + numberOfGSCsScaleLimit);
            }

            //Check according to the calculated limit, but also check that the physical memory is enough
            return (machine.getGridServiceContainers().getSize() < numberOfGSCsScaleLimit && jvmSizeInMB < (freePhysicalMemorySizeInMB - memorySafetyBufferInMB));
        }

        /** returns false if needs to scale up/out */
        private Command handle(PuCapacityPlanner puCapacityPlanner) {
            
            SlaExtractor slaExtractor = new SlaExtractor(puCapacityPlanner.getProcessingUnit());
            MemorySla memorySla = slaExtractor.getMemorySla();
            if (memorySla == null) {
                return null;
            }
            
            return handleMemorySla(puCapacityPlanner.getProcessingUnit(), memorySla);
        }

        private Command handleMemorySla(ProcessingUnit pu, MemorySla memorySla) {
            List<GridServiceContainer> gscs = new ArrayList<GridServiceContainer>();
            for (ProcessingUnitInstance puInstance : pu) {
                GridServiceContainer gsc = puInstance.getGridServiceContainer();
                if (!gscs.contains(gsc)) {
                    gscs.add(gsc);
                }
            }

            List<GridServiceContainer> gscsWithoutBreach = new ArrayList<GridServiceContainer>();
            List<GridServiceContainer> gscsWithBreach = new ArrayList<GridServiceContainer>();
            for (GridServiceContainer gsc : gscs) {
                double memoryHeapUsedPerc = gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                if (memoryHeapUsedPerc > memorySla.getThreshold()) {
                    if (gsc.getProcessingUnitInstances().length == 1) {
                        logger.warning("Can't amend GSC [" + ToStringHelper.gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                    } else {
                        logger.warning("GSC [" + ToStringHelper.gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                        gscsWithBreach.add(gsc);
                    }

                } else {
                    logger.finest("GSC [" + ToStringHelper.gscToString(gsc)
                            + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] is within threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                    gscsWithoutBreach.add(gsc);
                }
            }

            if (!gscsWithBreach.isEmpty()) {
                return handleGSCsWithBreach(pu, memorySla, gscsWithBreach);
            } else if (!gscsWithoutBreach.isEmpty()) {
                return handleGSCsWithoutBreach(pu, memorySla, gscsWithoutBreach);
            }

            return null;
        }

        private Command handleGSCsWithBreach(ProcessingUnit pu, MemorySla memorySla, List<GridServiceContainer> gscsWithBreach) {
            logger.finest("Handling " + gscsWithBreach.size()+" GSCs with Breach");
            //sort from high to low
            Collections.sort(gscsWithBreach, new Comparator<GridServiceContainer>() {
                public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                    double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    return (int)(memoryHeapUsedPerc2 - memoryHeapUsedPerc1);
                }
            });

            PuCapacityPlanner puCapacityPlanner = new PuCapacityPlanner(pu);
            Zone zone = admin.getZones().getByName(puCapacityPlanner.getZoneName());

            for (GridServiceContainer gsc : gscsWithBreach) {
                
                logger.finest("Handling GSC ["+ToStringHelper.gscToString(gsc)+"] which has ["+gsc.getProcessingUnitInstances().length+"] processing unit instances");
                
                //try and relocate a pu instance from a breached gsc. if successful, move to next gsc
                //if not, try the next pu instance. if no instance was moved, try and create a gsc and retry to relocate an instance to it
//                for (int retry=0; retry<2; ++retry) {

                    for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                        int estimatedMemoryHeapUsedPercPerInstance = getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                        logger.finest("Finding GSC that can hold [" + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] with an estimate of ["
                                + NUMBER_FORMAT.format(estimatedMemoryHeapUsedPercPerInstance) + "%]");


                        List<GridServiceContainer> gscsInZone = Arrays.asList(zone.getGridServiceContainers().getContainers());
                        //sort from low to high
                        Collections.sort(gscsInZone, new Comparator<GridServiceContainer>() {
                            public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                                double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                                double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                                return (int)(memoryHeapUsedPerc1 - memoryHeapUsedPerc2);
                            }
                        });

                        for (GridServiceContainer gscToRelocateTo : gscsInZone) {

                            //if gsc is the same gsc as the pu instance we are handling - skip it
                            if (gscToRelocateTo.equals(puInstanceToMaybeRelocate.getGridServiceContainer())) {
                                //logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - same GSC as this instance");
                                continue;
                            }

                            //if gsc reached its scale limit (of processing units) then skip it
                            if (!(gscToRelocateTo.getProcessingUnitInstances().length < puCapacityPlanner.getScalingFactor())) {
                                logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - reached the scale limit");
                                continue; //can't use this GSC to scale
                            }

                            int memoryHeapUsedByThisGsc = (int)Math.ceil(gscToRelocateTo.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc());
                            if (memoryHeapUsedByThisGsc + estimatedMemoryHeapUsedPercPerInstance > memorySla.getThreshold()) {
                                logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - memory used ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%]");
                                continue;
                            }

                            // if GSC on same machine, no need to check for co-location constraints
                            // since these were already checked when instance was instantiated
                            if (!gscToRelocateTo.getMachine().equals(puInstanceToMaybeRelocate.getMachine())) {

                                //TODO add max-instances-per-vm and max-instances-per-zone if necessary

                                //verify max instances per machine
                                if (pu.getMaxInstancesPerMachine() > 0) {
                                    int instancesOnMachine = 0;
                                    for (ProcessingUnitInstance instance : puInstanceToMaybeRelocate.getPartition().getInstances()) {
                                        if (instance.getMachine().equals(gscToRelocateTo.getMachine())) {
                                            ++instancesOnMachine;
                                        }
                                    }
                                    if (instancesOnMachine >= pu.getMaxInstancesPerMachine()) {
                                        logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - reached max-instances-per-machine limit");
                                        continue; //reached limit
                                    }
                                }                            
                            }


                            logger.finest("Found GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%] used.");
                            logger.finest("Relocating ["+ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate)+"] to GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "]");
                            puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo);
                            return null; //found a gsc
                        }
                    }

//                    if (retry == 0) {
                        logger.finest("Needs to scale up/out to obey memory SLA");
                        //exclusion list indicating machines we don't want a GSC to be started on. e.g. reached max-instances-per-machine constraint
                        List<Machine> machinesToExclude = null;
                        if (pu.getMaxInstancesPerMachine() > 0) {
                            machinesToExclude = new ArrayList<Machine>();
                            for (ProcessingUnitInstance puInstance : gsc.getProcessingUnitInstances()) {
                                for (ProcessingUnitInstance instance : puInstance.getPartition().getInstances()) {
                                    if (!puInstance.equals(instance)) {
                                        Machine machineToExclude = instance.getMachine();
                                        if (!machinesToExclude.contains(machinesToExclude)) {
                                            machinesToExclude.add(machineToExclude);
                                        }
                                    }
                                }
                            }
                        }
                        
                        Command command = new Command();
                        command.machinesToExclude = machinesToExclude;
                        return command;
//                        if (!handler.startGSC(machinesToExclude)) {
//                            logger.fine("[X] Scaling out, Can't start a GSC - Another machine is required.");
//                            break; //out of retry loop
//                        }
 //                   }
//                }
            }
            
            return null;
        }

        
        private Command handleGSCsWithoutBreach(ProcessingUnit pu, MemorySla memorySla, List<GridServiceContainer> gscsWithoutBreach) {
            
            if (!pu.getStatus().equals(DeploymentStatus.INTACT)) {
                return null;
            }
            
            //logger.finest("Handling " + gscsWithoutBreach.size() + " GSCs without Breach");
          //sort from low to high
            Collections.sort(gscsWithoutBreach, new Comparator<GridServiceContainer>() {
                public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                    double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    return (int)(memoryHeapUsedPerc1 - memoryHeapUsedPerc2);
                }
            });

            PuCapacityPlanner puCapacityPlanner = new PuCapacityPlanner(pu);
            Zone zone = admin.getZones().getByName(puCapacityPlanner.getZoneName());

            for (GridServiceContainer gsc : gscsWithoutBreach) {
                
                if (gsc.getProcessingUnitInstances().length > 1) {
                    //logger.finest("Skipped GSC ["+ToStringHelper.gscToString(gsc)+"] has more than 1 processing unit");
                    continue;
                }
                
                
                logger.finest("Handling GSC ["+ToStringHelper.gscToString(gsc)+"] which has ["+gsc.getProcessingUnitInstances().length+"] processing unit instances");
                for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                    int estimatedMemoryHeapUsedInMB = (int)Math.ceil(gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedInMB());
                    int estimatedMemoryHeapUsedPerc = (int)Math.ceil(gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()); //getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                    logger.finest("Finding GSC that can hold [" + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] with ["
                            + NUMBER_FORMAT.format(estimatedMemoryHeapUsedPerc) + "%]");


                    List<GridServiceContainer> gscsInZone = Arrays.asList(zone.getGridServiceContainers().getContainers());
                    //sort from low to high
                    Collections.sort(gscsInZone, new Comparator<GridServiceContainer>() {
                        public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                            double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                            double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                            return (int)(memoryHeapUsedPerc1 - memoryHeapUsedPerc2);
                        }
                    });

                    for (GridServiceContainer gscToRelocateTo : gscsInZone) {

                        //if gsc is the same gsc as the pu instance we are handling - skip it
                        if (gscToRelocateTo.equals(puInstanceToMaybeRelocate.getGridServiceContainer())) {
                            //logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - same GSC as this instance");
                            continue;
                        }

                        //if gsc reached its scale limit (of processing units) then skip it
                        if (!(gscToRelocateTo.getProcessingUnitInstances().length < puCapacityPlanner.getScalingFactor())) {
                            logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - reached the scale limit");
                            continue; //can't use this GSC to scale
                        }

                        int memoryHeapUsedPercByThisGsc = (int)Math.ceil(gscToRelocateTo.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc());
                        int memoryRequiredInPerc = memoryHeapUsedPercByThisGsc + estimatedMemoryHeapUsedPerc;
                        if (memoryRequiredInPerc > memorySla.getThreshold()) {
                            logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - memory used ["+NUMBER_FORMAT.format(memoryHeapUsedPercByThisGsc)+"%]"
                                    + " - required ["+ memoryRequiredInPerc+"%] exceeds threshold of ["+memorySla.getThreshold()+"%]");
                            continue;
                        }
                        
                        int memoryHeapUsedInMB = (int)Math.ceil(gscToRelocateTo.getVirtualMachine().getStatistics().getMemoryHeapUsedInMB());
                        int memoryRequiredInMB = memoryHeapUsedInMB+estimatedMemoryHeapUsedInMB+memorySafetyBufferInMB;
                        int memorySlaThresholdInMB = (int)Math.ceil((100.0-memorySla.getThreshold())*gscToRelocateTo.getVirtualMachine().getDetails().getMemoryHeapMaxInMB()/100);
                        if (memoryRequiredInMB > memorySlaThresholdInMB) {
                            logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - required ["+NUMBER_FORMAT.format(memoryRequiredInMB)+" MB] exceeds threshold of ["+memorySlaThresholdInMB+" MB]");
                            continue;
                        }

                        // if GSC on same machine, no need to check for co-location constraints
                        // since these were already checked when instance was instantiated
                        if (!gscToRelocateTo.getMachine().equals(puInstanceToMaybeRelocate.getMachine())) {

                            //TODO add max-instances-per-vm and max-instances-per-zone if necessary

                            //verify max instances per machine
                            if (pu.getMaxInstancesPerMachine() > 0) {
                                int instancesOnMachine = 0;
                                for (ProcessingUnitInstance instance : puInstanceToMaybeRelocate.getPartition().getInstances()) {
                                    if (instance.getMachine().equals(gscToRelocateTo.getMachine())) {
                                        ++instancesOnMachine;
                                    }
                                }
                                if (instancesOnMachine >= pu.getMaxInstancesPerMachine()) {
                                    logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - reached max-instances-per-machine limit");
                                    continue; //reached limit
                                }
                            }                            
                        }


                        logger.finest("Found GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedPercByThisGsc)+"%] used.");
                        logger.finest("Relocating ["+ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate)+"] to GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "]");
                        puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo);
                        if (gsc.getProcessingUnitInstances().length == 0) {
                            logger.finest("Scaling in, trying to terminate empty GSC ["+ToStringHelper.gscToString(gsc)+"]");
                            gsc.kill();
                        }
                        if (gsc.getMachine().getGridServiceContainers().getSize() == 0) {
                            logger.finest("Scaling down, No need for machine ["+ToStringHelper.machineToString(gsc.getMachine())+"]");
                        }
                        return null;
                    }
                }
            }
            
            return null;
        }
        
        
        private int getEstimatedMemoryHeapUsedPercPerInstance(GridServiceContainer gsc, ProcessingUnitInstance puInstance) {
            
            int totalPuNumOfObjects = 0;
            int thisPuNumOfObjects = 0;
            for (ProcessingUnitInstance aPuInstance : gsc.getProcessingUnitInstances()) {

                try {
                    IJSpace space = aPuInstance.getSpaceInstance().getGigaSpace().getSpace();
                    SpaceRuntimeInfo runtimeInfo = ((IRemoteJSpaceAdmin)space.getAdmin()).getRuntimeInfo();
                    Integer numOfEntries = runtimeInfo.m_NumOFEntries.isEmpty() ? 0 : runtimeInfo.m_NumOFEntries.get(0);
                    Integer numOfTemplates = runtimeInfo.m_NumOFTemplates.isEmpty() ? 0 : runtimeInfo.m_NumOFTemplates.get(0);
                    int nObjects = numOfEntries + numOfTemplates;
                    totalPuNumOfObjects += nObjects;
                    if (aPuInstance.equals(puInstance)) {
                        thisPuNumOfObjects = nObjects;
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            
            double memoryHeapUsed = gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
            
            int estimatedMemoryHeapUsedPerc = 0;
            if (thisPuNumOfObjects == 0) {
                if (totalPuNumOfObjects == 0) {
                    estimatedMemoryHeapUsedPerc = (int)Math.ceil(memoryHeapUsed/gsc.getProcessingUnitInstances().length);
                } else {
                    estimatedMemoryHeapUsedPerc = (int)Math.ceil(memoryHeapUsed/totalPuNumOfObjects);
                }
            } else {
                estimatedMemoryHeapUsedPerc = (int)Math.ceil((thisPuNumOfObjects * memoryHeapUsed)/totalPuNumOfObjects);
            }
            return estimatedMemoryHeapUsedPerc;
        }
    }
}
