package org.openspaces.grid.esm;

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
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.zone.Zone;

import com.j_spaces.kernel.TimeUnitProperty;

public class InternalESMImpl {
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    private final static long initialDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.initialDelay", "1m");
    private final static long fixedDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.fixedDelay", "5s");
    private final static int memorySafetyBufferInMB = MemorySettings.valueOf(System.getProperty("org.openspaces.grid.esm.memorySafetyBuffer", "100MB")).toMB();
    
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
        spaceDeployment.setContextProperty("scalingFactor", String.valueOf(calculateScalingFactor(context)));

        logger.finest("Deploying " + deployment.getDataGridName() 
                + "\n\t Zone: " + zoneName 
                + "\n\t Min Memory: " + context.getMinMemory()
                + "\n\t Max Memory: " + context.getMaxMemory()
                + "\n\t JVM Size: " + context.getJvmSize()
                + "\n\t Isolation Level: " + context.getIsolationLevel().name()
                + "\n\t Highly Available? " + context.isHighlyAvailable()
                + "\n\t Partitions: " + numberOfParitions);
        
        admin.getGridServiceManagers().waitForAtLeastOne().deploy(spaceDeployment);
    }
    
    private int calculateNumberOfPartitions(DeploymentContext context) {
        int numberOfPartitions = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getJvmSize());
        if (context.isHighlyAvailable()) {
            numberOfPartitions /= 2;
        }
        
        return numberOfPartitions;
    }
    
    /** scaling factor is used as the service limit of each GSC */
    private int calculateScalingFactor(DeploymentContext context) {
        int maxGSCs = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getJvmSize());
        int minGSCs = MemorySettings.valueOf(context.getMinMemory()).floorDividedBy(context.getJvmSize());
        double scalingFactor = Math.ceil(1.0*maxGSCs/minGSCs);
        return (int)scalingFactor;
    }
    
    private final class ScheduledTask implements Runnable {

        BrokenDeploymentHandler brokenDeploymentHandler = new BrokenDeploymentHandler();
        
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

        DedicatedIsolationBrokenDeploymentHandler dedicatedIsolationHandler = new DedicatedIsolationBrokenDeploymentHandler();
        
        public void handle(ProcessingUnit pu) {
            
            IsolationLevel isolationLevel = IsolationLevel.valueOf(pu.getBeanLevelProperties().getContextProperties().getProperty("isolationLevel"));
            switch(isolationLevel) {
            case DEDICATED:
                dedicatedIsolationHandler.handle(pu);
                default:
                    return;
            }
        }
    }
    
    private final class DedicatedIsolationBrokenDeploymentHandler {

        public void handle(ProcessingUnit pu) {
            Properties contextProperties = pu.getBeanLevelProperties().getContextProperties();
            int scalingFactor = Integer.valueOf(contextProperties.getProperty("scalingFactor"));

            int minNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("minMemory")).floorDividedBy(
                    contextProperties.getProperty("jvmSize"));

            assert pu.getTotalNumberOfInstances() <= scalingFactor * minNumberOfGSCs;

            int maxNumberOfGSCsPerMachine = minNumberOfGSCs;
            if (pu.getNumberOfBackups() > 0) {
                maxNumberOfGSCsPerMachine = (int) Math.ceil(0.5 * minNumberOfGSCs);
            }

            String zoneName = pu.getRequiredZones()[0];

            logger.finest("scalingFactor=" + scalingFactor + ", minNumberOfGSCs=" + minNumberOfGSCs
                    + ", maxNumberOfGSCsPerMachine=" + maxNumberOfGSCsPerMachine + ", numberOfGSCsInZone: [" + zoneName
                    + "]=" + numberOfGSCsInZone(zoneName));

            while (numberOfGSCsInZone(zoneName) < minNumberOfGSCs) {
                boolean startedGSC = false;
                for (Machine machine : admin.getMachines()) {
                    if (!(machine.getGridServiceContainers().getSize() < maxNumberOfGSCsPerMachine)) {
                        logger.finest("[X] Machine reached the scale limit of [" + maxNumberOfGSCsPerMachine
                                + "] GSCs per machine");
                        continue;
                    }
                    if (!meetsDedicatedIsolationConstaint(machine, zoneName)) {
                        logger.finest("[X] Machine doesn't meet dedicated isolation constraint");
                        continue;
                    }

                    GridServiceAgent agent = machine.getGridServiceAgent();
                    if (agent == null)
                        continue; // to next machine

                    if (!hasEnoughMemoryForNewGSC(contextProperties, machine)) {
                        logger.finest("[X] Machine doesn't have enough memory to start a new GSC");
                        continue; // can't start GSC here
                    }

                    logger.finest("[V] starting GSC");
                    agent.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument(
                            "-Dcom.gs.zones=" + zoneName).vmInputArgument(
                                    "-Dcom.gigaspaces.grid.gsc.serviceLimit=" + scalingFactor));
                    startedGSC = true;
                    break; // to check for min-gsc requirement
                }

                if (!startedGSC) {
                    logger.fine("[X] Can't start a GSC on this machine - Another machine is required.");
                    break; // try when scheduled again
                }
            }
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
                        + machine.getOperatingSystem().getStatistics().getPhysicalMemoryUsedPerc() + "%"
                        + "\n\t GSC JVM Size: " + contextProperties.getProperty("jvmSize") + "\n\t GSC amount: "
                        + numberOfGSCsOnMachine + " - Scale Limit: " + numberOfGSCsScaleLimit);
            }

            //Check according to the calculated limit, but also check that the physical memory is enough
            return (machine.getGridServiceContainers().getSize() < numberOfGSCsScaleLimit && jvmSizeInMB < (freePhysicalMemorySizeInMB - memorySafetyBufferInMB));
        }

    }
    
    public static void main(String[] args) {
        new InternalESMImpl();
    }
}
