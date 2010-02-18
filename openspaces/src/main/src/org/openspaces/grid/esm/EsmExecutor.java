package org.openspaces.grid.esm;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.zone.Zone;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.internal.backport.java.util.Arrays;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import com.j_spaces.kernel.TimeUnitProperty;

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
        
        String loggerProp = System.getProperty("logger.level");
        if (loggerProp != null)
            logger.setLevel(Level.parse(loggerProp));
        
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

        public ScheduledTask() {
        }
        
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
            
            return handleMemorySla(puCapacityPlanner, memorySla);
        }

        private Command handleMemorySla(PuCapacityPlanner puCapacityPlanner, MemorySla memorySla) {
            List<GridServiceContainer> gscs = new ArrayList<GridServiceContainer>();
            for (ProcessingUnitInstance puInstance : puCapacityPlanner.getProcessingUnit()) {
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
                return handleGSCsWithBreach(puCapacityPlanner.getProcessingUnit(), memorySla, gscsWithBreach);
            } else if (!gscsWithoutBreach.isEmpty()) {
                Command c = handleGSCsWithoutBreach(puCapacityPlanner.getProcessingUnit(), memorySla, gscsWithoutBreach);
                if (c == null) {
                    rebalance(puCapacityPlanner);
                }
                return c;
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

                            //precaution - if we are about to relocate make sure there is a backup in a partition-sync2backup topology.
                            if (puInstanceToMaybeRelocate.getProcessingUnit().getNumberOfBackups() > 0) {
                                ProcessingUnitInstance backup = puInstanceToMaybeRelocate.getPartition().getBackup();
                                if (backup == null) {
                                    logger.finest("---> no backup found for instance : " +ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate));
                                    return null;
                                }
                            }
                            
                            logger.finest("Found GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%] used.");
                            logger.finest("Relocating ["+ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate)+"] to GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "]");
                            puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo);
                            return null; //found a gsc
                        }
                    }

//                    if (retry == 0) {
//                        logger.finest("Needs to scale up/out to obey memory SLA");
//                        //exclusion list indicating machines we don't want a GSC to be started on. e.g. reached max-instances-per-machine constraint
//                        List<Machine> machinesToExclude = null;
//                        if (pu.getMaxInstancesPerMachine() > 0) {
//                            machinesToExclude = new ArrayList<Machine>();
//                            for (ProcessingUnitInstance puInstance : gsc.getProcessingUnitInstances()) {
//                                for (ProcessingUnitInstance instance : puInstance.getPartition().getInstances()) {
//                                    if (!puInstance.equals(instance)) {
//                                        Machine machineToExclude = instance.getMachine();
//                                        if (!machinesToExclude.contains(machinesToExclude)) {
//                                            machinesToExclude.add(machineToExclude);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        
//                        Command command = new Command();
//                        command.machinesToExclude = machinesToExclude;
//                        return command;
//                        if (!handler.startGSC(machinesToExclude)) {
//                            logger.fine("[X] Scaling out, Can't start a GSC - Another machine is required.");
//                            break; //out of retry loop
//                        }
 //                   }
//                }
            }
            
            logger.finest("Needs to scale up/out to obey memory SLA");
            Command command = new Command();
            return command;            
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

    /*
     * 1. calculate optimal number of primaries per machine 
     * 
     * 2. sort machines by number of primaries (large -> small) 
     * 
     * 3. stop condition: number of primaries on first machine - optimal number > 1
     * to not run into loops 
     * 
     * 4. find primary processing unit that it's backup is on a machine with
     * the least number of primaries and restart it
     */
    public void rebalance(PuCapacityPlanner puCapacityPlanner) {
        
        if (!puCapacityPlanner.getProcessingUnit().getStatus().equals(DeploymentStatus.INTACT)) {
            return;
        }
        
        List<Machine> machinesSortedByNumOfPrimaries = getMachinesSortedByNumPrimaries(puCapacityPlanner);
        Machine firstMachine = machinesSortedByNumOfPrimaries.get(0);

        int optimalNumberOfPrimariesPerMachine = (int)Math.ceil(1.0*puCapacityPlanner.getProcessingUnit().getNumberOfInstances() / puCapacityPlanner.getNumberOfMachinesInZone());
        int numPrimariesOnMachine = getNumPrimariesOnMachine(puCapacityPlanner, firstMachine);
        
        logger.finest("Rebalancer - Expects " + optimalNumberOfPrimariesPerMachine + " primaries per machine");
        if (numPrimariesOnMachine > optimalNumberOfPrimariesPerMachine) {
            logger.finest("Rebalancer - Machine [" + ToStringHelper.machineToString(firstMachine) + " has: " + numPrimariesOnMachine + " - rebalancing...");
            findPrimaryProcessingUnitToRestart(puCapacityPlanner, machinesSortedByNumOfPrimaries);
//            GridServiceContainer firstGsc = getFirstGscsSortedByNumPrimaries(puCapacityPlanner, firstMachine);
//            restartPrimaryInstance(firstGsc);
        } else {
            logger.finest("Rebalancer - Machines are balanced");
        }
    }
    
    private List<Machine> getMachinesSortedByNumPrimaries(final PuCapacityPlanner puCapacityPlanner) {
        String zoneName = puCapacityPlanner.getZoneName();
        Zone zone = admin.getZones().getByName(zoneName);
        List<GridServiceContainer> gscList = Arrays.asList(zone.getGridServiceContainers().getContainers());
        List<Machine> machinesInZone = new ArrayList<Machine>();
        for (GridServiceContainer gsc : gscList) {
            if (!machinesInZone.contains(gsc.getMachine()))
                machinesInZone.add(gsc.getMachine());
        }
        
        Collections.sort(machinesInZone, new Comparator<Machine>() {
            public int compare(Machine m1, Machine m2) {
                return getNumPrimariesOnMachine(puCapacityPlanner, m2) - getNumPrimariesOnMachine(puCapacityPlanner, m1);
            }
            
        });
        
        return machinesInZone;
    }
    
    //find primary processing unit that it's backup is on a machine with the least number of primaries and restart it
    private void findPrimaryProcessingUnitToRestart(PuCapacityPlanner puCapacityPlanner, List<Machine> machinesSortedByNumOfPrimaries) {

        Machine lastMachine = machinesSortedByNumOfPrimaries.get(machinesSortedByNumOfPrimaries.size() -1);
        
        for (Machine machine : machinesSortedByNumOfPrimaries) {
            List<GridServiceContainer> gscsSortedByNumPrimaries = getGscsSortedByNumPrimaries(puCapacityPlanner, machine);
            
            for (GridServiceContainer gsc : gscsSortedByNumPrimaries) {
                for (ProcessingUnitInstance puInstance : gsc.getProcessingUnitInstances()) {
                    if (isPrimary(puInstance)) {
                        ProcessingUnitInstance backup = puInstance.getPartition().getBackup();
                        if (backup == null) {
                            logger.finest("---> no backup found for instance : " +ToStringHelper.puInstanceToString(puInstance));
                            return; //something is wrong, wait for next reschedule
                        }
                        
                        GridServiceContainer backupGsc = backup.getGridServiceContainer();
                        if (backupGsc.getMachine().equals(lastMachine)) {
                            restartPrimaryInstance(puInstance);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private List<GridServiceContainer> getGscsSortedByNumPrimaries(PuCapacityPlanner puCapacityPlanner, Machine machine) {
        String zoneName = puCapacityPlanner.getZoneName();
        Zone zone = admin.getZones().getByName(zoneName);
        
        //extract only gscs within this machine which belong to this zone
        List<GridServiceContainer> gscList = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer gsc : zone.getGridServiceContainers().getContainers()) {
            if (gsc.getMachine().equals(machine)) {
                gscList.add(gsc);
            }
        }
        
        Collections.sort(gscList, new Comparator<GridServiceContainer>() {
            public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                int puDiff = getNumPrimaries(gsc2) - getNumPrimaries(gsc1);
                return puDiff;
            }
        });
        
        return gscList;
    }

    private GridServiceContainer getFirstGscsSortedByNumPrimaries(PuCapacityPlanner puCapacityPlanner, Machine machine) {
        String zoneName = puCapacityPlanner.getZoneName();
        Zone zone = admin.getZones().getByName(zoneName);
        
        //extract only gscs within this machine which belong to this zone
        List<GridServiceContainer> gscList = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer gsc : zone.getGridServiceContainers().getContainers()) {
            if (gsc.getMachine().equals(machine)) {
                gscList.add(gsc);
            }
        }
        
        Collections.sort(gscList, new Comparator<GridServiceContainer>() {
            public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                int puDiff = getNumPrimaries(gsc2) - getNumPrimaries(gsc1);
                return puDiff;
            }
        });
        
        return gscList.get(0);
    }
    
    private List<GridServiceContainer> getGscsSortedByNumPrimaries(PuCapacityPlanner puCapacityPlanner) {
        String zoneName = puCapacityPlanner.getZoneName();
        Zone zone = admin.getZones().getByName(zoneName);
        
        List<GridServiceContainer> gscList = Arrays.asList(zone.getGridServiceContainers().getContainers());
        Collections.sort(gscList, new Comparator<GridServiceContainer>() {
            public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                int puDiff = getNumPrimaries(gsc2) - getNumPrimaries(gsc1);
                return puDiff;
                
//                if (puDiff != 0) {
//                    return puDiff;
//                }
//                puDiff = getNumBackups(gsc2) - getNumBackups(gsc1);
//                if (puDiff != 0) {
//                    return puDiff;
//                }
//                return gsc2.getProcessingUnitInstances().length - gsc1.getProcessingUnitInstances().length;
            }
        });
        return gscList;
    }
    
    private int getMinNumOfInstancesOnMachine(PuCapacityPlanner puCapacityPlanner, List<Machine> machines) {
        String zoneName = puCapacityPlanner.getZoneName();
        int minNumOfInstances = puCapacityPlanner.getProcessingUnit().getTotalNumberOfInstances();
        for (Machine machine : machines) {
            int numOfInstancesOnMachine = 0;
            for (ProcessingUnitInstance instance : machine.getProcessingUnitInstances()) {
                if (!instance.getZones().containsKey(zoneName)) {
                    continue;
                }
                numOfInstancesOnMachine++;
            }
            minNumOfInstances = Math.min(minNumOfInstances, numOfInstancesOnMachine);
        }
        return minNumOfInstances;
    }
    
    private int getNumPrimariesOnMachine(PuCapacityPlanner puCapacityPlanner, Machine machine) {
        String zoneName = puCapacityPlanner.getZoneName();
        Zone zone = admin.getZones().getByName(zoneName);
        int numPrimaries = 0;
        ProcessingUnitInstance[] instances = zone.getProcessingUnitInstances();
        for (ProcessingUnitInstance instance : instances) {
            if (!instance.getMachine().equals(machine)) {
                continue;
            }
            if (isPrimary(instance)) {
                numPrimaries++;
            }
        }
        return numPrimaries;
    }

    
    private int getNumPrimaries(GridServiceContainer gsc) {
        int numPrimaries = 0;
        ProcessingUnitInstance[] instances = gsc.getProcessingUnitInstances();
        for (ProcessingUnitInstance instance : instances) {
            if (isPrimary(instance)) {
                numPrimaries++;
            }
        }
        return numPrimaries;
    }

    private int getNumBackups(GridServiceContainer gsc2) {
        int numBackups = 0;
        ProcessingUnitInstance[] instances = gsc2.getProcessingUnitInstances();
        for (ProcessingUnitInstance instance : instances) {
            if (isBackup(instance)) {
                numBackups++;
            }
        }
        return numBackups;
    }

    private boolean isPrimary(ProcessingUnitInstance instance) {
        SpaceInstance spaceInstance = instance.getSpaceInstance();
        return (spaceInstance != null && spaceInstance.getMode().equals(SpaceMode.PRIMARY));
    }

    private boolean isBackup(ProcessingUnitInstance instance) {
        SpaceInstance spaceInstance = instance.getSpaceInstance();
        return (spaceInstance != null && spaceInstance.getMode().equals(SpaceMode.BACKUP));
    }
    
    private void restartPrimaryInstance(ProcessingUnitInstance instance) {
        //Perquisite precaution - can happen if state changed
        if (!instance.getProcessingUnit().getStatus().equals(DeploymentStatus.INTACT)) {
            return;
        }
        
        logger.finest("Restarting instance " + ToStringHelper.puInstanceToString(instance) + " at GSC " + ToStringHelper.gscToString(instance.getGridServiceContainer()));
        ProcessingUnitInstance restartedInstance = instance.restartAndWait();
        boolean isBackup = restartedInstance.waitForSpaceInstance().waitForMode(SpaceMode.BACKUP, 10, TimeUnit.SECONDS);
        if (!isBackup) {
            logger.finest("Waited 10 seconds, instance " + ToStringHelper.puInstanceToString(instance) + " still not registered as backup");
            return;
        }
        logger.finest("Done restarting instance " + ToStringHelper.puInstanceToString(instance));
    }
    
    private void restartPrimaryInstance(GridServiceContainer gsc) {
        //Perquisite precaution - can happen if state changed 
        if (gsc == null)
            return;
        
        ProcessingUnitInstance[] instances = gsc.getProcessingUnitInstances();
        ProcessingUnitInstance instanceToRestart = null;
        int maxNumBackupsOnGscOfBackup = 0;
        for (final ProcessingUnitInstance instance : instances) {
            if (isPrimary(instance)) {
                ProcessingUnitInstance backup = instance.getPartition().getBackup();
                if (backup == null) {
                    logger.finest("---> no backup found for instance : " +ToStringHelper.puInstanceToString(instance));
                    return;
                }
                GridServiceContainer backupGsc = backup.getGridServiceContainer();
                int numBackupsOnGscOfBackup = getNumBackups(backupGsc);
                if (numBackupsOnGscOfBackup >= maxNumBackupsOnGscOfBackup) {
                    instanceToRestart = instance;
                    maxNumBackupsOnGscOfBackup = numBackupsOnGscOfBackup; 
                }
            }
        }
        
        //Perquisite precaution - can happen if state changed
        if (instanceToRestart == null)
            return;
        
        //Perquisite precaution - can happen if state changed
        if (!instanceToRestart.getProcessingUnit().getStatus().equals(DeploymentStatus.INTACT)) {
            return;
        }
        
        logger.finest("Restarting instance " + ToStringHelper.puInstanceToString(instanceToRestart) + " at GSC " + ToStringHelper.gscToString(gsc));
        ProcessingUnitInstance restartedInstance = instanceToRestart.restartAndWait();
        boolean isBackup = restartedInstance.waitForSpaceInstance().waitForMode(SpaceMode.BACKUP, 10, TimeUnit.SECONDS);
        if (!isBackup) {
            logger.finest("Waited 10 seconds, instance " + ToStringHelper.puInstanceToString(instanceToRestart) + " still not registered as backup");
            return;
        }
        logger.finest("Done restarting instance " + ToStringHelper.puInstanceToString(instanceToRestart));
    }
}
