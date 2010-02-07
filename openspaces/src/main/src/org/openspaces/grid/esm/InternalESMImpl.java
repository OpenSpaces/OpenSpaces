package org.openspaces.grid.esm;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.openspaces.admin.esm.deployment.IsolationLevel;
import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
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

public class InternalESMImpl {
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
    
    public InternalESMImpl() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        
        logger.setLevel(Level.parse(System.getProperty("logger.level", "INFO")));
        logger.config("Initial Delay: " + initialDelay + " ms");
        logger.config("Fixed Delay: " + fixedDelay + " ms");
        if (Boolean.getBoolean("runAtServer"))
            executorService.scheduleWithFixedDelay(new ScheduledTask(), initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
    }

    //-Dorg.jini.rio.qos.address=moran2
 // -Dcom.gs.zones=barfoo -Dcom.gigaspaces.grid.gsc.serviceLimit=10
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
    
    private final class ScheduledTask implements Runnable {

        BrokenDeploymentHandler brokenDeploymentHandler = new BrokenDeploymentHandler();
        IntactSlaHandler intactSlaHandler = new IntactSlaHandler();
        
        public void run() {
            try {
                logger.finest("ScheduledTask is running...");
                ProcessingUnits processingUnits = admin.getProcessingUnits();
                for (ProcessingUnit pu : processingUnits) {

                    logger.fine(puToString(pu));

                    if (!pu.getBeanLevelProperties().getContextProperties().containsKey("elastic"))
                        continue;

                    DeploymentStatus status = pu.getStatus();
                    switch (status) {
                    case BROKEN:
                    case COMPROMISED:
                        brokenDeploymentHandler.handle(pu);
                        break;
                    case INTACT:
                        intactSlaHandler.handle(pu);
                        break;
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Caught exception: " + t, t);
            }
        }
        
        private String puToString(ProcessingUnit pu) {
            StringBuilder sb = new StringBuilder();
            sb.append("Processing Unit Configuration:")
            .append("\n\tName: ").append(pu.getName())
            .append("\n\tInstances: ").append(pu.getNumberOfInstances())
            .append("\n\tBackups: ").append(pu.getNumberOfBackups())
            .append("\n\tInstances per-machine: ").append(pu.getMaxInstancesPerMachine())
            .append("\n\tPlanned instances: ").append(pu.getTotalNumberOfInstances())
            .append("\n\tActual instances: ").append(pu.getProcessingUnitInstances().length)
            .append("\n\tDeployment status: ").append(pu.getStatus())
            ;
            return sb.toString();
        }
    }

    /**
     * Could be broken because of failed deployment, or compromised because of service limit, or
     * max-per-machine constraints
     */
    private final class BrokenDeploymentHandler {

        
        public void handle(ProcessingUnit pu) {
            
            IsolationLevel isolationLevel = IsolationLevel.valueOf(pu.getBeanLevelProperties().getContextProperties().getProperty("isolationLevel"));
            switch(isolationLevel) {
            case DEDICATED:
                DedicatedIsolationDeploymentHandler dedicatedIsolationHandler = new DedicatedIsolationDeploymentHandler(pu);
                dedicatedIsolationHandler.handleBrokenDeployment();
                default:
                    return;
            }
        }
    }
    
    private final class DedicatedIsolationDeploymentHandler {
        
        private final ProcessingUnit pu;
        private final PuCapacityPlanner puCapacityPlanner;
        
        public DedicatedIsolationDeploymentHandler(ProcessingUnit pu) {
            this.pu = pu;
            this.puCapacityPlanner = new PuCapacityPlanner(pu);
            
            logger.finest(
                      "\n\t status: " + pu.getStatus()
                    + "\n\t minNumberOfGSCs=" + puCapacityPlanner.getMinNumberOfGSCs()
                    + "\n\t maxNumberOfGSCs=" + puCapacityPlanner.getMaxNumberOfGSCs()
                    + "\n\t actual number of GSCs: " + admin.getGridServiceContainers().getSize()
                    + "\n\t maxNumberOfGSCsPerMachine=" + puCapacityPlanner.getMaxNumberOfGSCsPerMachine()
                    + "\n\t scalingFactor="+puCapacityPlanner.getScalingFactor()+" (instances per GSC)"
                    + "\n\t number of GSCs in zone: " + puCapacityPlanner.getZoneName() + "=" + numberOfGSCsInZone(puCapacityPlanner.getZoneName()));

        }
        
        public void handleBrokenDeployment() {

            while (numberOfGSCsInZone(puCapacityPlanner.getZoneName()) < puCapacityPlanner.getMinNumberOfGSCs()) {
                if (!startGSC(null)) {
                    logger.fine("[X] Can't start a GSC - Another machine is required.");
                    break; // try when scheduled again
                }
            }
        }
        
        public boolean startGSC(List<Machine> machinesToExclude) {
            
            Zone zone = admin.getZones().getByName(puCapacityPlanner.getZoneName());
            if (zone != null && !(zone.getGridServiceContainers().getSize() < puCapacityPlanner.getMaxNumberOfGSCs())) {
                logger.warning("Could not allocate a new GSC, reached scale limit of " + puCapacityPlanner.getMaxNumberOfGSCs());
                return false; //can't start GSC
            }
            
            //we limit when deployment is broken, and to amend it we rely on the min number of gscs needed
            boolean limitNumberOfGSCsPerMachine = zone == null || zone.getGridServiceContainers().getSize() < puCapacityPlanner.getMinNumberOfGSCs();
            
            List<Machine> machines = new ArrayList<Machine>(Arrays.asList(admin.getMachines().getMachines()));
            if (machinesToExclude != null) {
                machines.removeAll(machinesToExclude);
            }
            for (Machine machine : machines) {

                if (!meetsDedicatedIsolationConstaint(machine, puCapacityPlanner.getZoneName())) {
                    logger.finest("[X] Machine ["+machineToString(machine)+"] doesn't meet dedicated isolation constraint");
                    continue;
                }
                
                //assumes dedicated machine!
                if (limitNumberOfGSCsPerMachine && !(machine.getGridServiceContainers().getSize() < puCapacityPlanner.getMaxNumberOfGSCsPerMachine())) {
                    logger.finest("[X] Machine ["+machineToString(machine)+"] reached the scale limit of [" + puCapacityPlanner.getMaxNumberOfGSCsPerMachine()
                            + "] GSCs per machine");
                    continue;
                } else {
                    logger.finest("[X] Machine ["+machineToString(machine)+"] is within scale limit of [" + puCapacityPlanner.getMaxNumberOfGSCsPerMachine()
                            + "] GSCs per machine, has ["+machine.getGridServiceContainers().getSize()+"] GSCs");
                }
                
                GridServiceAgent agent = machine.getGridServiceAgent();
                if (agent == null)
                    continue; // to next machine

                if (!hasEnoughMemoryForNewGSC(pu.getBeanLevelProperties().getContextProperties(), machine)) {
                    logger.finest("[X] Machine ["+machineToString(machine)+"] doesn't have enough memory to start a new GSC");
                    continue; // can't start GSC here
                }
                
                logger.finest("[V] Scaling up, starting GSC on machine ["+machineToString(machine)+"]");
                GridServiceContainer newGSC = agent.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument(
                        "-Dcom.gs.zones=" + puCapacityPlanner.getZoneName()).vmInputArgument(
                                "-Dcom.gigaspaces.grid.gsc.serviceLimit=" + puCapacityPlanner.getScalingFactor()));
                logger.finest("[V] started GSC ["+gscToString(newGSC)+"] on machine ["+machineToString(newGSC.getMachine())+"]");
                return true; //started GSC
            }
            
            return false; //can't start GSC
        }

        private int numberOfGSCsInZone(String zoneName) {
            Zone zone = admin.getZones().getByName(zoneName);
            return zone == null ? 0 : zone.getGridServiceContainers().getSize();
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

        private boolean hasEnoughMemoryForNewGSC(Properties contextProperties, Machine machine) {

            double totalPhysicalMemorySizeInMB = machine.getOperatingSystem()
            .getDetails()
            .getTotalPhysicalMemorySizeInMB();
            int jvmSizeInMB = MemorySettings.valueOf(contextProperties.getProperty("jvmSize")).toMB();
            int numberOfGSCsScaleLimit = (int) Math.floor(totalPhysicalMemorySizeInMB / jvmSizeInMB);
            int numberOfGSCsOnMachine = machine.getGridServiceContainers().getSize();
            int freePhysicalMemorySizeInMB = (int) Math.floor(machine.getOperatingSystem()
                    .getStatistics()
                    .getFreePhysicalMemorySizeInMB());

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("\nMachine memory:" + "\n\t Total physical memory: "
                        + machine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInMB() + " MB"
                        + "\n\t Free physical memory: "
                        + machine.getOperatingSystem().getStatistics().getFreePhysicalMemorySizeInMB() + " MB"
                        + "\n\t Memory Saftey Buffer: "
                        + memorySafetyBufferInMB + " MB"
                        + "\n\t Total swap space: "
                        + machine.getOperatingSystem().getDetails().getTotalSwapSpaceSizeInMB() + " MB"
                        + "\n\t Free swap space: "
                        + machine.getOperatingSystem().getStatistics().getFreeSwapSpaceSizeInMB() + " MB"
                        + "\n\t Memory Used: "
                        + NUMBER_FORMAT.format(machine.getOperatingSystem().getStatistics().getPhysicalMemoryUsedPerc()) + "%"
                        + "\n\t GSC JVM Size: " + contextProperties.getProperty("jvmSize") + "\n\t GSC amount: "
                        + numberOfGSCsOnMachine + " - Scale Limit: " + numberOfGSCsScaleLimit);
            }

            //Check according to the calculated limit, but also check that the physical memory is enough
            return (machine.getGridServiceContainers().getSize() < numberOfGSCsScaleLimit && jvmSizeInMB < (freePhysicalMemorySizeInMB - memorySafetyBufferInMB));
        }
    }
    
    private final class IntactSlaHandler {
        
        public void handle(ProcessingUnit pu) {
            String slaDescriptors = (String)pu.getBeanLevelProperties().getContextProperties().get("sla");
            if (slaDescriptors == null) {
                return; //no sla defined
            }
            
            //extract sla
            String[] split = slaDescriptors.split("/");
            for (String s : split) {
                int beginIndex = s.indexOf("sla=", 0)+"sla=".length();
                int endIndex = s.indexOf(',',beginIndex);
                String sla = s.substring(beginIndex, endIndex);
                if (sla.equals(MemorySla.class.getSimpleName())) {
                    beginIndex = s.indexOf("threshold=", endIndex)+"threshold=".length();
                    String threshold = s.substring(beginIndex);
                    MemorySlaHandler memorySlaHandler = new MemorySlaHandler(Integer.valueOf(threshold));
                    memorySlaHandler.handle(pu);
                }
            }
        }
    }
    
    private final class CpuSlaHandler {

        private final int threshold;

        public CpuSlaHandler(int threshold) {
            this.threshold = threshold;
        }
        
        public void handle(ProcessingUnit pu) {
            List<Machine> machines = new ArrayList<Machine>();
            for (ProcessingUnitInstance puInstance : pu) {
                Machine machine = puInstance.getMachine();
                if (!machines.contains(machine)) {
                    machines.add(machine);
                }
            }
            
            for (Machine machine : machines) {
                double cpuPerc = machine.getOperatingSystem().getStatistics().getCpuPerc();
                if (cpuPerc > threshold) {
                    logger.warning("Machine [" + machineToString(machine)
                            + "] - CPU [" + NUMBER_FORMAT.format(cpuPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(threshold) + "%]");
                } else {
                    logger.finest("Machine [" + machineToString(machine)
                            + "] - CPU [" + NUMBER_FORMAT.format(cpuPerc) + "%] is within threshold of [" + NUMBER_FORMAT.format(threshold) + "%]");
                }
            }
        }
        
    }
    
    private final class MemorySlaHandler {

        private final int threshold;

        public MemorySlaHandler(int threshold) {
            this.threshold = threshold;
        }

        public void handle(ProcessingUnit pu) {
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
                if (memoryHeapUsedPerc > threshold) {
                    if (gsc.getProcessingUnitInstances().length == 1) {
                        logger.warning("Can't amend GSC [" + gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(threshold) + "%]");
                    } else {
                        logger.warning("GSC [" + gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(threshold) + "%]");
                        gscsWithBreach.add(gsc);
                    }

                } else {
                    logger.finest("GSC [" + gscToString(gsc)
                            + "] - Memory [" + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] is within threshold of [" + NUMBER_FORMAT.format(threshold) + "%]");
                    gscsWithoutBreach.add(gsc);
                }
            }

            //nothing to fix
            if (!gscsWithBreach.isEmpty()) {
                handleGSCsWithBreach(pu, gscsWithBreach);
            } else if (!gscsWithoutBreach.isEmpty()) {
                handleGSCsWithoutBreach(pu, gscsWithoutBreach);
            }

        }

        private void handleGSCsWithBreach(ProcessingUnit pu, List<GridServiceContainer> gscsWithBreach) {
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
                
                logger.finest("Handling GSC ["+gscToString(gsc)+"] which has ["+gsc.getProcessingUnitInstances().length+"] processing unit instances");
                
                //try and relocate a pu instance from a breached gsc. if successful, move to next gsc
                //if not, try the next pu instance. if no instance was moved, try and create a gsc and retry to relocate an instance to it
                for (int retry=0; retry<2; ++retry) {

                    for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                        int estimatedMemoryHeapUsedPercPerInstance = getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                        logger.finest("Finding GSC that can hold [" + puInstanceToString(puInstanceToMaybeRelocate) + "] with an estimate of ["
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
                                logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - same GSC as this instance");
                                continue;
                            }

                            //if gsc reached its scale limit (of processing units) then skip it
                            if (!(gscToRelocateTo.getProcessingUnitInstances().length < puCapacityPlanner.getScalingFactor())) {
                                logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - reached the scale limit");
                                continue; //can't use this GSC to scale
                            }

                            int memoryHeapUsedByThisGsc = (int)Math.ceil(gscToRelocateTo.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc());
                            if (memoryHeapUsedByThisGsc + estimatedMemoryHeapUsedPercPerInstance > threshold) {
                                logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - memory used ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%]");
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
                                        logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - reached max-instances-per-machine limit");
                                        continue; //reached limit
                                    }
                                }                            
                            }


                            logger.finest("Found GSC [" + gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%] used.");
                            logger.finest("Relocating ["+puInstanceToString(puInstanceToMaybeRelocate)+"] to GSC [" + gscToString(gscToRelocateTo) + "]");
                            puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo);
                            return; //found a gsc
                        }
                    }

                    if (retry == 0) {
                        logger.finest("Trying to start a new GSC");
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
                        
                        DedicatedIsolationDeploymentHandler handler = new DedicatedIsolationDeploymentHandler(pu);
                        if (!handler.startGSC(machinesToExclude)) {
                            logger.fine("[X] Scaling out, Can't start a GSC - Another machine is required.");
                            break; //out of retry loop
                        }
                    }
                }
            }
        }

        
        private void handleGSCsWithoutBreach(ProcessingUnit pu, List<GridServiceContainer> gscsWithoutBreach) {
            logger.finest("Handling " + gscsWithoutBreach.size() + " GSCs without Breach");
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
                
                logger.finest("Handling GSC ["+gscToString(gsc)+"] which has ["+gsc.getProcessingUnitInstances().length+"] processing unit instances");
                if (gsc.getProcessingUnitInstances().length > 1) {
                    logger.finest("Skipped GSC ["+gscToString(gsc)+"] has more than 1 processing unit");
                    continue;
                }
                
                
                for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                    int estimatedMemoryHeapUsedPercPerInstance = getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                    logger.finest("Finding GSC that can hold [" + puInstanceToString(puInstanceToMaybeRelocate) + "] with an estimate of ["
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
                            logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - same GSC as this instance");
                            continue;
                        }

                        //if gsc reached its scale limit (of processing units) then skip it
                        if (!(gscToRelocateTo.getProcessingUnitInstances().length < puCapacityPlanner.getScalingFactor())) {
                            logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - reached the scale limit");
                            continue; //can't use this GSC to scale
                        }

                        int memoryHeapUsedByThisGsc = (int)Math.ceil(gscToRelocateTo.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc());
                        if (memoryHeapUsedByThisGsc + estimatedMemoryHeapUsedPercPerInstance > threshold) {
                            logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - memory used ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%]");
                            continue;
                        }
                        
                        if (estimatedMemoryHeapUsedPercPerInstance > memoryHeapUsedByThisGsc) {
                            logger.finest("Skipping GSC ["+gscToString(gsc)+"] - memory will be increased by more than 100% if instance is moved here");
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
                                    logger.finest("Skipping GSC ["+gscToString(gscToRelocateTo)+"] - reached max-instances-per-machine limit");
                                    continue; //reached limit
                                }
                            }                            
                        }


                        logger.finest("Found GSC [" + gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%] used.");
                        logger.finest("Relocating ["+puInstanceToString(puInstanceToMaybeRelocate)+"] to GSC [" + gscToString(gscToRelocateTo) + "]");
                        puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo);
                        if (gsc.getProcessingUnitInstances().length == 0) {
                            logger.finest("Scaling in, trying to terminate empty GSC ["+gscToString(gsc)+"]");
                            gsc.kill();
                        }
                        if (gsc.getMachine().getGridServiceContainers().getSize() == 0) {
                            logger.finest("Scaling down, No need for machine ["+gsc.getMachine()+"]");
                        }
                        return;
                    }
                }
            }
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
        
    private static String machineToString(Machine machine) {
        return machine.getHostName() + "/" + machine.getHostAddress();
    }
    
    private static String gscToString(GridServiceContainer container) {
        return "pid["+container.getVirtualMachine().getDetails().getPid()+"] host["+machineToString(container.getMachine())+"]";
    }
    
    private static String gscListToString(List<GridServiceContainer> list) {
        String s = "[";
        for (int i=0; i<list.size(); ++i) {
            s += gscToString(list.get(i));
            if (i<list.size()-1) {
                s += ",";
            }
        }
        s+="]";
        return s;
    }
    
    private static String puInstanceToString(ProcessingUnitInstance instance) {
        return instance.getInstanceId() + " [" + instance.getBackupId() + "]";
    }
    
    public static void main(String[] args) {
        new InternalESMImpl();
    }
}
