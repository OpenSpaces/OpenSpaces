package org.openspaces.grid.esm;

import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.DEPLOYMENT_ISOLATION;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.ELASTIC;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.ELASTIC_SCALE_CONFIG;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.HIGHLY_AVAILABLE;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.INITIAL_JAVA_HEAP_SIZE;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.MAXIMUM_JAVA_HEAP_SIZE;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.MAX_MEMORY;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.MIN_MEMORY;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.SLA;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.TENANT;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.VM_ARGUMENTS;
import static org.openspaces.grid.esm.ElasticDeploymentContextProperties.ZONE;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import org.openspaces.admin.esm.deployment.InternalElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.zone.Zone;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import com.j_spaces.kernel.ClassLoaderHelper;
import com.j_spaces.kernel.TimeUnitProperty;

public class EsmExecutor {
    
    public static void main(String[] args) {
        new EsmExecutor();
    }

    /*
     * FINEST   - step by step actions and decisions
     * FINER    - 
     * FINE     - 
     * INFO     - actions user should know about
     * WARNING  -
     * SEVERE   -
     */
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    private final static long initialDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.initialDelay", "5s");
    private final static long fixedDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.fixedDelay", "5s");
    private final static int memorySafetyBufferInMB = MemorySettings.valueOf(System.getProperty("org.openspaces.grid.esm.memorySafetyBuffer", "100m")).toMB();
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    //maps pu-name to elastic-scale impl
    private final Map<String, ElasticScaleHandler> elasticScaleMap = new HashMap<String, ElasticScaleHandler>();
    
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

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Initial Delay: " + initialDelay + " ms");
            logger.config("Fixed Delay: " + fixedDelay + " ms");
        }
        
        if (Boolean.valueOf(System.getProperty("esm.enabled", "true"))) {
            executorService.scheduleWithFixedDelay(new ScheduledTask(), initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
            admin.getProcessingUnits().addLifecycleListener(new UndeployedProcessingUnitLifecycleEventListener());
        }
    }
    
    public String[] getManagedProcessingUnits() {
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ArrayList<String> processingUnitNames = new ArrayList<String>(processingUnits.getSize());
        for (ProcessingUnit pu : processingUnits) {
            if (!PuCapacityPlanner.isElastic(pu))
                continue;
            processingUnitNames.add(pu.getName());
        }
        return processingUnitNames.toArray(new String[processingUnitNames.size()]);
    }

    public void deploy(ElasticDataGridDeployment deployment) {
        DeploymentContext deploymentContext = ((InternalElasticDataGridDeployment)deployment).getDeploymentContext();
        String tenantPrefix = deploymentContext.getTenant().length() == 0 ? "" : deploymentContext.getTenant() + "-"; 
        final String zoneName = tenantPrefix+deployment.getDataGridName();
        SpaceDeployment spaceDeployment = new SpaceDeployment(deployment.getDataGridName());
        spaceDeployment.addZone(zoneName);
        
        int numberOfParitions = calculateNumberOfPartitions(deploymentContext);
        if (deploymentContext.hasPartitionsSet()) {
            if (deploymentContext.getPartitions() < numberOfParitions) {
                throw new IllegalArgumentException("Number of set partitions [" + deploymentContext.getPartitions()
                        + "] must be greater than [" + numberOfParitions
                        + "] to meet the minimum requested capacity of [" + deploymentContext.getMinMemory() + "]");
            }
            numberOfParitions = deploymentContext.getPartitions();
        } else {
            numberOfParitions = calculateNumberOfPartitions(deploymentContext);
        }
        
        if (deploymentContext.isHighlyAvailable()) {
            spaceDeployment.maxInstancesPerMachine(1);
            spaceDeployment.partitioned(numberOfParitions, 1);
        } else {
            spaceDeployment.partitioned(numberOfParitions, 0);
        }
        
        String initialJavaHeapSize = deploymentContext.getInitialJavaHeapSize();
        String maximumJavaHeapSize = deploymentContext.getMaximumJavaHeapSize();
        if (MemorySettings.valueOf(initialJavaHeapSize).isGreaterThan(MemorySettings.valueOf(maximumJavaHeapSize))) {
            deploymentContext.setInitialJavaHeapSize(maximumJavaHeapSize);
            initialJavaHeapSize = maximumJavaHeapSize;
        }
        
        spaceDeployment.setContextProperty(ELASTIC, "true");
        spaceDeployment.setContextProperty(HIGHLY_AVAILABLE, String.valueOf(deploymentContext.isHighlyAvailable()));
        spaceDeployment.setContextProperty(MIN_MEMORY, deploymentContext.getMinMemory());
        spaceDeployment.setContextProperty(MAX_MEMORY, deploymentContext.getMaxMemory());
        spaceDeployment.setContextProperty(INITIAL_JAVA_HEAP_SIZE, deploymentContext.getInitialJavaHeapSize());
        spaceDeployment.setContextProperty(MAXIMUM_JAVA_HEAP_SIZE, deploymentContext.getMaximumJavaHeapSize());
        spaceDeployment.setContextProperty(DEPLOYMENT_ISOLATION, deploymentContext.getDeploymentIsolationLevel().name());
        spaceDeployment.setContextProperty(SLA, deploymentContext.getSlaDescriptors());
        spaceDeployment.setContextProperty(ZONE, zoneName);
        spaceDeployment.setContextProperty(TENANT, deploymentContext.getTenant());

        if (deployment.getElasticScaleHandlerConfig() != null) {
            spaceDeployment.setContextProperty(ELASTIC_SCALE_CONFIG, ElasticScaleHandlerConfigSerializer.toString(deployment.getElasticScaleHandlerConfig()));
        }
        
        if (!deployment.getContextProperties().isEmpty()) {
            Set<Entry<Object,Object>> entrySet = deployment.getContextProperties().entrySet();
            for (Entry<Object,Object> entry : entrySet) {
                spaceDeployment.setContextProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
        
        if (deploymentContext.getVmInputArguments()!= null) {
           spaceDeployment.setContextProperty(VM_ARGUMENTS, deploymentContext.getVmInputArguments()); 
        }
        
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Request to deploy " + deployment.getDataGridName() + " " + numberOfParitions
                    + (deploymentContext.isHighlyAvailable() ? ",1" : ",0") + " capacity: "
                    + deploymentContext.getMinMemory() + " - " + deploymentContext.getMaxMemory()
                    + " - " + deploymentContext.getDeploymentIsolationLevel());
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deploying " + deployment.getDataGridName() 
                    + "\n\t Zone: " + zoneName 
                    + "\n\t Min Capacity: " + deploymentContext.getMinMemory()
                    + "\n\t Max Capacity: " + deploymentContext.getMaxMemory()
                    + "\n\t Initial Java Heap Size: " + initialJavaHeapSize
                    + "\n\t Maximum Java Heap Size: " + maximumJavaHeapSize
                    + "\n\t Deployment Isolation: " + deploymentContext.getDeploymentIsolationLevel().name()
                    + "\n\t Highly Available? " + deploymentContext.isHighlyAvailable()
                    + "\n\t Partitions: " + numberOfParitions
                    + "\n\t SLA: " + deploymentContext.getSlaDescriptors());
        }
        
        admin.getGridServiceManagers().waitForAtLeastOne().deploy(spaceDeployment);
    }
    
    private int calculateNumberOfPartitions(DeploymentContext context) {
        
        int numberOfPartitions = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getMaximumJavaHeapSize());
        if (context.isHighlyAvailable()) {
            numberOfPartitions /= 2;
        }
        
        return Math.max(1, numberOfPartitions);
    }
    
    /**
     * Life cycle listener of processing units which are un-deployed, and their resources should be scaled down.
     */
    private final class UndeployedProcessingUnitLifecycleEventListener implements
            ProcessingUnitLifecycleEventListener {

        public void processingUnitAdded(ProcessingUnit processingUnit) {
        }

        public void processingUnitRemoved(ProcessingUnit processingUnit) {
            if (!PuCapacityPlanner.isElastic(processingUnit))
                return;
            
            try {
                executorService.schedule(new UndeployHandler(new PuCapacityPlanner(processingUnit, getOnDemandElasticScale(processingUnit))), fixedDelay, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to invoke undeploy handler for " +processingUnit.getName(), e);
            }
        }

        public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        }

        public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
        }

        public void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event) {
        }
    }


    private final class ScheduledTask implements Runnable {

        public ScheduledTask() {
        }
        
        public void run() {
            try {
                if (Level.OFF.equals(logger.getLevel())) {
                    return; //turn off cruise control
                }

                ProcessingUnits processingUnits = admin.getProcessingUnits();
                for (ProcessingUnit pu : processingUnits) {

                    if (!PuCapacityPlanner.isElastic(pu)) {
                        logger.finest(ToStringHelper.puToString(pu));
                        continue;
                    }
                    
                    PuCapacityPlanner puCapacityPlanner = new PuCapacityPlanner(pu, getOnDemandElasticScale(pu));
                    logger.finest(ToStringHelper.puCapacityPlannerToString(puCapacityPlanner));
                    
                    Workflow workflow = new Workflow(puCapacityPlanner);
                    while (workflow.hasNext()) {
                        Runnable nextInWorkflow = workflow.removeFirst();
                        nextInWorkflow.run();
                    }
                    
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Caught exception: " + t, t);
            }
        }
    }
    
    private ElasticScaleHandler getOnDemandElasticScale(ProcessingUnit pu) throws Exception {
        ElasticScaleHandler onDemandElasticScale = elasticScaleMap.get(pu.getName());
        if (onDemandElasticScale != null) {
            return onDemandElasticScale;
        }
        
        String elasticScaleConfigStr = pu.getBeanLevelProperties().getContextProperties().getProperty(ELASTIC_SCALE_CONFIG);
        if (elasticScaleConfigStr == null) {
            return new NullElasticScaleHandler();
        }

        ElasticScaleHandlerConfig elasticScaleConfig = ElasticScaleHandlerConfigSerializer.fromString(elasticScaleConfigStr);
        Class<? extends ElasticScaleHandler> clazz = ClassLoaderHelper.loadClass(elasticScaleConfig.getClassName()).asSubclass(ElasticScaleHandler.class);
        ElasticScaleHandler newInstance = clazz.newInstance();
        newInstance.init(elasticScaleConfig);

        elasticScaleMap.put(pu.getName(), newInstance);
        return newInstance;
    }

    
    private class Workflow {
        private boolean isBroken = false;
        final List<Runnable> runnables = new ArrayList<Runnable>();
        
        /** constructs an empty workflow */
        public Workflow() {
        }
        
        public Workflow(PuCapacityPlanner puCapacityPlanner) {
            runnables.add(new UnderCapacityHandler(puCapacityPlanner, this));
            runnables.add(new MemorySlaHandler(puCapacityPlanner, this));
            runnables.add(new RebalancerHandler(puCapacityPlanner, this));
            runnables.add(new GscCollectorHandler(puCapacityPlanner, this));
            runnables.add(new CompromisedDeploymentHandler(puCapacityPlanner, this));
        }
        
        public void add(Runnable runner) {
            runnables.add(runner);
        }
        
        public boolean hasNext() {
            return !runnables.isEmpty();
        }
        
        public Runnable removeFirst() {
            return runnables.remove(0);
        }
        
        public void breakWorkflow() {
            isBroken = true;
            runnables.clear();
        }
        
        public boolean isBroken() {
            return isBroken;
        }
    }
    
    private class UnderCapacityHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;

        public UnderCapacityHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
        }

        public void run() {
            logger.finest(UnderCapacityHandler.class.getSimpleName() + " invoked");
            
            if (puCapacityPlanner.isProcessingUnitIntact()) {
                return; //no need for a new GSC if deployment is intact (not to run into an infinite loop of start/terminate empty gsc)
            }
            
            if (underMinCapcity()) {
                logger.info("Starting a minimum of ["+puCapacityPlanner.getMinNumberOfGSCs()+"] GSCs");
                workflow.breakWorkflow();
                workflow.add(new StartGscHandler(puCapacityPlanner, workflow));
            }
        }
        
        private boolean underMinCapcity() {
            return puCapacityPlanner.getNumberOfGSCsInZone() < puCapacityPlanner.getMinNumberOfGSCs();
        }
    }
    
    public class StartGscHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;

        public StartGscHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
        }

        public void run() {
            
            if (reachedMaxCapacity() && puCapacityPlanner.hasEnoughMachines()) {
                logger.warning("Reached Capacity Limit");
                return;
            }
            
            final ZoneCorrelator zoneCorrelator = new ZoneCorrelator(admin, puCapacityPlanner.getContextProperties().getZoneName());
            
            //machines filtered by zones, filtered by isolation level
            List<Machine> machines = puCapacityPlanner.getDeploymentIsolationFilter().filter(zoneCorrelator.getMachines());

            //sort by least number of GSCs in zone
            Collections.sort(machines, new Comparator<Machine>() {
                public int compare(Machine m1, Machine m2) {
                    
                    List<GridServiceContainer> gscsM1 = zoneCorrelator.getGridServiceContainersByMachine(m1);
                    List<GridServiceContainer> gscsM2 = zoneCorrelator.getGridServiceContainersByMachine(m2);
                    
                    return gscsM1.size() - gscsM2.size();
                }
            });
            
            boolean needsNewMachine = true;
            for (Machine machine : machines) {

                if (!puCapacityPlanner.getElasticScale().accept(machine)) {
                    logger.finest("Machine ["+ToStringHelper.machineToString(machine)+"] was filtered out by [" + puCapacityPlanner.getElasticScale().getClass().getName()+"]");
                    continue;
                }
                
                if (!machineHasAnAgent(machine)) {
                    logger.finest("Can't start a GSC on machine ["+ToStringHelper.machineToString(machine)+"] - doesn't have an agent.");
                    continue;
                }
                
                if (machineHasAnEmptyGSC(zoneCorrelator, machine)) {
                    logger.finest("Won't start a GSC on machine ["+ToStringHelper.machineToString(machine)+"] - already has an empty GSC.");
                    continue;
                }
                
                if (!hasEnoughMemoryForNewGSC(machine)) {
                    logger.finest("Can't start a GSC on machine ["+ToStringHelper.machineToString(machine)+"] - doesn't have enough memory to start a new GSC");
                    continue; // can't start GSC here
                }
                
                startGscOnMachine(machine);
                needsNewMachine = false;
                break;
            }
            
            if (needsNewMachine) {
                if (puCapacityPlanner.getNumberOfMachinesWithEmptyGSCsInZone() >= 2) {
                    logger.fine("Won't start a new machine - already has two machines with an empty GSC in zone");
                    return;
                }
                
                logger.fine("Can't start a GSC on the available machines - needs a new machine.");
                
                ElasticScaleHandlerContext elasticScaleCommand = new ElasticScaleHandlerContext();
                elasticScaleCommand.setMachines(machines);
                puCapacityPlanner.getElasticScale().scaleOut(elasticScaleCommand);
            }
        }

        /*
         * If INTACT, base decision on the number of GSCs in zone and the max number of GSCs planned.
         * Otherwise, don't count the number of empty GSCs since we may run short of 
         */
        private boolean reachedMaxCapacity() {
            if (puCapacityPlanner.isProcessingUnitIntact()) {
                int numberOfGSCsInZone = puCapacityPlanner.getNumberOfGSCsInZone();
                logger.finest("GSCs in zone: [" + numberOfGSCsInZone + "], max allowed [" + puCapacityPlanner.getMaxNumberOfGSCs()+"]");
                return numberOfGSCsInZone >= puCapacityPlanner.getMaxNumberOfGSCs();
            } 
            int numberOfNonEmptyGSCsInZone = puCapacityPlanner.getNumberOfNonEmptyGSCsInZone();
            logger.finest("(non-empty) GSCs in zone: [" + numberOfNonEmptyGSCsInZone + "], max allowed [" + puCapacityPlanner.getMaxNumberOfGSCs()+"]");
            return numberOfNonEmptyGSCsInZone >= puCapacityPlanner.getMaxNumberOfGSCs();
        }
        
        private boolean aboveMinCapcity() {
            return puCapacityPlanner.getNumberOfGSCsInZone() >= puCapacityPlanner.getMinNumberOfGSCs();
        }
        
        // due to timing delays, a machine might have an empty GSC - don't start a GSC on it
        private boolean machineHasAnEmptyGSC(ZoneCorrelator zoneCorrelator, Machine machine) {
            List<GridServiceContainer> list = zoneCorrelator.getGridServiceContainersByMachine(machine);
            for (GridServiceContainer gscOnMachine : list) {
                if (gscOnMachine.getProcessingUnitInstances().length == 0) {
                    return true;
                }
            }
            return false;
        }
        
        //if machine does not have an agent, it can't be used to start a new GSC
        private boolean machineHasAnAgent(Machine machine) {
            GridServiceAgent agent = machine.getGridServiceAgent();
            return (agent != null);
        }
        
        // machine has enough memory to start a new GSC
        private boolean hasEnoughMemoryForNewGSC(Machine machine) {

            double totalPhysicalMemorySizeInMB = machine.getOperatingSystem()
                .getDetails()
                .getTotalPhysicalMemorySizeInMB();
            int jvmSizeInMB = MemorySettings.valueOf(puCapacityPlanner.getContextProperties().getMaximumJavaHeapSize()).toMB();
            int numberOfGSCsScaleLimit = (int) Math.floor(totalPhysicalMemorySizeInMB / jvmSizeInMB);
            
            final OperatingSystemStatistics operatingSystemStatistics = machine.getOperatingSystem().getStatistics();

            // get total free system memory + cached (getActualFreePhysicalMemorySizeInMB returns -1
            // when not using Sigar)
            int totalFreePhysicalMemorySizeInMB = (int) Math.floor(
                    (operatingSystemStatistics.getActualFreePhysicalMemorySizeInBytes() > -1 ? 
                            operatingSystemStatistics.getActualFreePhysicalMemorySizeInMB() : 
                                operatingSystemStatistics.getFreePhysicalMemorySizeInMB()));

            // Check according to the calculated limit, but also check that the physical memory is enough
            boolean withinScaleLimit = machine.getGridServiceContainers().getSize() < numberOfGSCsScaleLimit;
            boolean withingMemoryLimit = jvmSizeInMB < (totalFreePhysicalMemorySizeInMB - memorySafetyBufferInMB);
            boolean hasEnoughMemoryForNewGSC = withinScaleLimit && withingMemoryLimit;
            
            if (!hasEnoughMemoryForNewGSC) {
                if (logger.isLoggable(Level.FINEST)) {
                    if (!withingMemoryLimit) {
                        logger.finest("Machine ["+ToStringHelper.machineToString(machine)+"] has reached a memory limit of ["+NUMBER_FORMAT.format(totalFreePhysicalMemorySizeInMB)+"] MB");
                    } else if (!withinScaleLimit) {
                        logger.finest("Machine ["+ToStringHelper.machineToString(machine)+"] has reached a scale limit of ["+numberOfGSCsScaleLimit+"] GSCs");
                    }
                }
            }
            return hasEnoughMemoryForNewGSC;
        }
        
        private void startGscOnMachine(Machine machine) {
            logger.info("Scaling up - Starting GSC on machine ["+ToStringHelper.machineToString(machine)+"]");
            
            GridServiceContainerOptions vmInputArgument = new GridServiceContainerOptions()
                .vmInputArgument("-Xms" + puCapacityPlanner.getContextProperties().getInitialJavaHeapSize())
                .vmInputArgument("-Xmx" + puCapacityPlanner.getContextProperties().getMaximumJavaHeapSize())
                .vmInputArgument("-Dcom.gs.zones=" + puCapacityPlanner.getContextProperties().getZoneName())
                .vmInputArgument("-Dcom.gigaspaces.grid.gsc.serviceLimit=" + puCapacityPlanner.getScalingFactor());
            
            if (puCapacityPlanner.getContextProperties().getVmArguments() != null) {
                String[] vmArguments = puCapacityPlanner.getContextProperties().getVmArguments().split(",");
                for (String vmArgument : vmArguments) {
                    vmInputArgument.vmInputArgument(vmArgument);
                }
            }
            
            GridServiceContainer newGSC = machine.getGridServiceAgent().startGridServiceAndWait(
                    vmInputArgument
                        , 60, TimeUnit.SECONDS);
            
            if (newGSC == null) {
                logger.warning("Failed Scaling up - Failed to start GSC on machine ["+ToStringHelper.machineToString(machine)+"]");
            } else {
                workflow.breakWorkflow();
                logger.info("Successfully started GSC ["+ToStringHelper.gscToString(newGSC)+"] on machine ["+ToStringHelper.machineToString(newGSC.getMachine())+"]");
            }
        }
    }
    
    private class MemorySlaHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;
        private final MemorySla memorySla;

        public MemorySlaHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
            SlaExtractor slaExtractor = new SlaExtractor(puCapacityPlanner.getProcessingUnit());
            memorySla = slaExtractor.getMemorySla();
        }
        
        public boolean hasMemorySla() {
            return memorySla != null;
        }
        
        public void run() {
            if (!hasMemorySla())
                return;

            logger.finest(MemorySlaHandler.class.getSimpleName() + " invoked");
            
            handleBreachAboveThreshold();
            
            if (!workflow.isBroken())
                handleBreachBelowThreshold();
        }
        
        public void handleBreachAboveThreshold() {
            /*
             * Go over GSCs, and gather GSCs above threshold
             */
            List<GridServiceContainer> gscs = new ArrayList<GridServiceContainer>();
            for (ProcessingUnitInstance puInstance : puCapacityPlanner.getProcessingUnit()) {
                GridServiceContainer gsc = puInstance.getGridServiceContainer();
                if (!gscs.contains(gsc)) {
                    gscs.add(gsc);
                }
            }

            List<GridServiceContainer> gscsWithBreach = new ArrayList<GridServiceContainer>();
            for (GridServiceContainer gsc : gscs) {
                double memoryHeapUsedPerc = gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                double avgMemoryHeapUsedPerc = getAvgMemoryHeapUsedPerc(gsc); //
                if (avgMemoryHeapUsedPerc > memorySla.getThreshold()) {
                    if (gsc.getProcessingUnitInstances().length == 1) {
                        logger.warning("Can't amend GSC [" + ToStringHelper.gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(avgMemoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                    } else {
                        logger.warning("GSC [" + ToStringHelper.gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(avgMemoryHeapUsedPerc) + "%] breached threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                        gscsWithBreach.add(gsc);
                    }
                } else if (memoryHeapUsedPerc > memorySla.getThreshold()) {
                    logger.warning("GSC [" + ToStringHelper.gscToString(gsc) + "] - Memory ["
                            + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] is fluctuating above threshold of ["
                            + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                }
                
            }
            
            //nothing to do
            if (gscsWithBreach.isEmpty()) {
                logger.finest("None of the GSCs breached above the threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) +"]" );
                return;
            }
            
            //sort from high to low
            Collections.sort(gscsWithBreach, new Comparator<GridServiceContainer>() {
                public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                    double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    return (int)(memoryHeapUsedPerc2 - memoryHeapUsedPerc1);
                }
            });

            ProcessingUnit pu = puCapacityPlanner.getProcessingUnit();
            ZoneCorrelator zoneCorrelator = new ZoneCorrelator(admin, puCapacityPlanner.getContextProperties().getZoneName());

            for (GridServiceContainer gsc : gscsWithBreach) {

                double memoryHeapUsedPerc = gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                logger.finest("Trying to handle GSC [" + ToStringHelper.gscToString(gsc) + "] - Memory ["
                        + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] with [" + gsc.getProcessingUnitInstances().length
                        + "] processing units");
                
                for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                    int estimatedMemoryHeapUsedPercPerInstance = getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                    logger.finer("Finding GSC that can hold [" + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] with an estimate of ["
                            + NUMBER_FORMAT.format(estimatedMemoryHeapUsedPercPerInstance) + "%]");


                    List<GridServiceContainer> gscsInZone = zoneCorrelator.getGridServiceContainers();
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
                                logger.finest("No backup found for instance : " +ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate));
                                return;
                            }
                        }

                        logger.finer("Found GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedByThisGsc)+"%] used.");
                        logger.info("Memory above threshold - Relocating ["
                                + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] from GSC ["
                                + ToStringHelper.gscToString(gsc) + "] to GSC ["
                                + ToStringHelper.gscToString(gscToRelocateTo) + "]");
                        puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo, 60, TimeUnit.SECONDS);
                        workflow.breakWorkflow();
                        return; //found a gsc
                    }
                }
            }
            
            logger.finest("Needs to scale up/out to obey memory SLA");
            workflow.breakWorkflow();
            workflow.add(new StartGscHandler(puCapacityPlanner, workflow));
        }
        
        private double getAvgMemoryHeapUsedPerc(GridServiceContainer gsc) {
            List<VirtualMachineStatistics> timeline = gsc.getVirtualMachine().getStatistics().getTimeline();
            
            double avgMemoryHeapUsedPerc = 0.0;
            for (int i=0; i<memorySla.getSubsetSize() && i<timeline.size(); ++i) {
                VirtualMachineStatistics statistics = timeline.get(i);
                double memoryHeapUsedPerc = statistics.getMemoryHeapUsedPerc();
                avgMemoryHeapUsedPerc += memoryHeapUsedPerc;
            }
            
            avgMemoryHeapUsedPerc /= memorySla.getSubsetSize();
            return avgMemoryHeapUsedPerc;
        }

        public void handleBreachBelowThreshold() {
           
            //handle only if deployment is intact
            if (!puCapacityPlanner.isProcessingUnitIntact()) {
                return;
            }
            
            /*
             * Go over GSCs, and gather GSCs below threshold
             */
            List<GridServiceContainer> gscs = new ArrayList<GridServiceContainer>();
            for (ProcessingUnitInstance puInstance : puCapacityPlanner.getProcessingUnit()) {
                GridServiceContainer gsc = puInstance.getGridServiceContainer();
                if (!gscs.contains(gsc)) {
                    gscs.add(gsc);
                }
            }

            List<GridServiceContainer> gscsWithoutBreach = new ArrayList<GridServiceContainer>();
            for (GridServiceContainer gsc : gscs) {
                if (gsc.getProcessingUnitInstances().length == 1) {
                    double memoryHeapUsedPerc = gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    double avgMemoryHeapUsedPerc = getAvgMemoryHeapUsedPerc(gsc);
                    if (avgMemoryHeapUsedPerc < memorySla.getThreshold()) {
                        logger.finest("GSC [" + ToStringHelper.gscToString(gsc)
                                + "] - Memory [" + NUMBER_FORMAT.format(avgMemoryHeapUsedPerc) + "%] is below threshold of [" + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                        gscsWithoutBreach.add(gsc);
                    }else if (memoryHeapUsedPerc < memorySla.getThreshold()) {
                        logger.finest("GSC [" + ToStringHelper.gscToString(gsc) + "] - Memory ["
                                + NUMBER_FORMAT.format(memoryHeapUsedPerc) + "%] is fluctuating below threshold of ["
                                + NUMBER_FORMAT.format(memorySla.getThreshold()) + "%]");
                    }
                }
            }
            
            //nothing to do
            if (gscsWithoutBreach.isEmpty()) {
                return;
            }
            
            //sort from low to high
            //first by number of processing units per machine, then by memory in each GSC
            Collections.sort(gscsWithoutBreach, new Comparator<GridServiceContainer>() {
                public int compare(GridServiceContainer gsc1, GridServiceContainer gsc2) {
                    
                    int nProcessingUnits1 = gsc1.getMachine().getProcessingUnitInstances().length;
                    int nProcessingUnits2 = gsc2.getMachine().getProcessingUnitInstances().length;

                    if (nProcessingUnits1 != nProcessingUnits2) {
                        return (nProcessingUnits1 - nProcessingUnits2);
                    }
                    
                    double memoryHeapUsedPerc1 = gsc1.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();
                    double memoryHeapUsedPerc2 = gsc2.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc();

                    return (int)(memoryHeapUsedPerc1 - memoryHeapUsedPerc2);
                }
            });

            ProcessingUnit pu = puCapacityPlanner.getProcessingUnit();
            ZoneCorrelator zoneCorrelator = new ZoneCorrelator(admin, puCapacityPlanner.getContextProperties().getZoneName());

            for (GridServiceContainer gsc : gscsWithoutBreach) {
                
                if (gsc.getProcessingUnitInstances().length > 1) {
                    continue;
                }
                
                for (ProcessingUnitInstance puInstanceToMaybeRelocate : gsc.getProcessingUnitInstances()) {
                    int estimatedMemoryHeapUsedInMB = (int)Math.ceil(gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedInMB());
                    int estimatedMemoryHeapUsedPerc = (int)Math.ceil(gsc.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()); //getEstimatedMemoryHeapUsedPercPerInstance(gsc, puInstanceToMaybeRelocate);
                    logger.finer("Finding GSC that can hold [" + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] with ["
                            + NUMBER_FORMAT.format(estimatedMemoryHeapUsedPerc) + "%]");

                    List<GridServiceContainer> gscsInZone = zoneCorrelator.getGridServiceContainers();
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
                        
                        //if target machine has less processing units than origin then skip it - we want to scale down not up.
                        if (gscToRelocateTo.getMachine().getProcessingUnitInstances().length < gsc.getMachine().getProcessingUnitInstances().length) {
                            logger.finest("Skipping GSC ["+ToStringHelper.gscToString(gscToRelocateTo)+"] - machine has less processing units than origin");
                            continue; //can't use this GSC to scale
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
                        int memorySlaThresholdInMB = (int)Math.floor(memorySla.getThreshold()*gscToRelocateTo.getVirtualMachine().getDetails().getMemoryHeapMaxInMB()/100);
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


                        logger.finer("Found GSC [" + ToStringHelper.gscToString(gscToRelocateTo) + "] which has only ["+NUMBER_FORMAT.format(memoryHeapUsedPercByThisGsc)+"%] used.");
                        logger.info("Memory below threshold - Relocating ["
                                + ToStringHelper.puInstanceToString(puInstanceToMaybeRelocate) + "] from GSC ["
                                + ToStringHelper.gscToString(gsc) + "] to GSC ["
                                + ToStringHelper.gscToString(gscToRelocateTo) + "]");
                        puInstanceToMaybeRelocate.relocateAndWait(gscToRelocateTo, 60, TimeUnit.SECONDS);
                        workflow.breakWorkflow();
                        return;
                    }
                }
            }
            
            return;
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
    
    private class RebalancerHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;

        public RebalancerHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
        }

        public void run() {
            if (!puCapacityPlanner.isProcessingUnitIntact()) {
                return;
            }
            
            logger.finest(RebalancerHandler.class.getSimpleName() + " invoked");
            
            List<Machine> machinesSortedByNumOfPrimaries = getMachinesSortedByNumPrimaries(puCapacityPlanner);
            Machine firstMachine = machinesSortedByNumOfPrimaries.get(0);

            int optimalNumberOfPrimariesPerMachine = (int)Math.ceil(1.0*puCapacityPlanner.getProcessingUnit().getNumberOfInstances() / puCapacityPlanner.getNumberOfMachinesInZone());
            int numPrimariesOnMachine = getNumPrimariesOnMachine(puCapacityPlanner, firstMachine);
            
            if (numPrimariesOnMachine > optimalNumberOfPrimariesPerMachine) {
                logger.finer("Rebalancer - Expects [" + optimalNumberOfPrimariesPerMachine
                        + "] primaries per machine; Machine [" + ToStringHelper.machineToString(firstMachine) + " has: "
                        + numPrimariesOnMachine + " - rebalancing...");
                findPrimaryProcessingUnitToRestart(puCapacityPlanner, machinesSortedByNumOfPrimaries);
            } else {
                logger.finest("Rebalancer - Machines are balanced; ["+optimalNumberOfPrimariesPerMachine+"] per machine");
            }
        }
        
        private List<Machine> getMachinesSortedByNumPrimaries(final PuCapacityPlanner puCapacityPlanner) {
            String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
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
        
        private int getNumPrimariesOnMachine(PuCapacityPlanner puCapacityPlanner, Machine machine) {
            String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
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
        
        private boolean isPrimary(ProcessingUnitInstance instance) {
            SpaceInstance spaceInstance = instance.getSpaceInstance();
            return (spaceInstance != null && spaceInstance.getMode().equals(SpaceMode.PRIMARY));
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
                                logger.finest("Skipping - no backup found yet for instance : " + ToStringHelper.puInstanceToString(puInstance));
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
            String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
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
        
        private void restartPrimaryInstance(ProcessingUnitInstance instance) {
            //Perquisite precaution - can happen if state changed
            if (!PuCapacityPlanner.isProcessingUnitIntact(instance.getProcessingUnit())) {
                return;
            }
            
            logger.info("Rebalancing - Restarting instance " + ToStringHelper.puInstanceToString(instance) + " at GSC " + ToStringHelper.gscToString(instance.getGridServiceContainer()));
            ProcessingUnitInstance restartedInstance = instance.restartAndWait(60, TimeUnit.SECONDS);
            workflow.breakWorkflow();
            if (restartedInstance == null) {
                logger.finest("Waited 60 seconds, for restarted instance " + ToStringHelper.puInstanceToString(instance) + " - still not restarted");
                return;
            }
            
            boolean isBackup = restartedInstance.waitForSpaceInstance().waitForMode(SpaceMode.BACKUP, 10, TimeUnit.SECONDS);
            if (!isBackup) {
                logger.finest("Waited 10 seconds, instance " + ToStringHelper.puInstanceToString(instance) + " still not registered as backup");
                return;
            }
            logger.finest("Done restarting instance " + ToStringHelper.puInstanceToString(instance));
        }
    }
    
    private class GscCollectorHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;

        public GscCollectorHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
        }

        public void run() {
            
            //Perquisite precaution
            if (!puCapacityPlanner.isProcessingUnitIntact()) {
                return;
            }
            
            logger.finest(GscCollectorHandler.class.getSimpleName() + " invoked");
            
            String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
            Zone zone = admin.getZones().getByName(zoneName);
            if (zone == null) return;
            
            for (GridServiceContainer gsc : zone.getGridServiceContainers()) {
                if (gsc.getProcessingUnitInstances().length == 0) {
                    logger.info("Scaling down - Terminate empty GSC ["+ToStringHelper.gscToString(gsc)+"]");
                    gsc.kill();
                    workflow.breakWorkflow();
                }
                
                if (gsc.getMachine().getGridServiceContainers().getSize() == 0) {
                    logger.info("Scaling in - No need for machine ["+ToStringHelper.machineToString(gsc.getMachine())+"]");
                    puCapacityPlanner.getElasticScale().scaleIn(gsc.getMachine());
                }
            }
        }
    }
    
    private class UndeployHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;

        public UndeployHandler(PuCapacityPlanner puCapacityPlanner) {
            this.puCapacityPlanner = puCapacityPlanner;
        }

        public void run() {
            
            //Perquisite precaution
//TODO due to bug in status, postpone this check            
//            if (!puCapacityPlanner.getProcessingUnit().getStatus().equals(DeploymentStatus.UNDEPLOYED)) {
//                return;
//            }
            
            logger.finest(UndeployHandler.class.getSimpleName() + " invoked");
            
            elasticScaleMap.remove(puCapacityPlanner.getProcessingUnit().getName());
            
            String zoneName = puCapacityPlanner.getContextProperties().getZoneName();
            Zone zone = admin.getZones().getByName(zoneName);
            if (zone == null) return;
            
            for (GridServiceContainer gsc : zone.getGridServiceContainers()) {
                int gscsOnMachine = gsc.getMachine().getGridServiceContainers().getSize();
                
                if (gsc.getProcessingUnitInstances().length == 0) {
                    logger.info("Scaling down - Terminate empty GSC ["+ToStringHelper.gscToString(gsc)+"]");
                    gsc.kill();
                    gscsOnMachine--;
                }
                
                if (gscsOnMachine == 0) {
                    logger.info("Scaling in - No need for machine ["+ToStringHelper.machineToString(gsc.getMachine())+"]");
                    puCapacityPlanner.getElasticScale().scaleIn(gsc.getMachine());
                }
            }
        }
    }
    
    private final class CompromisedDeploymentHandler implements Runnable {
        private final PuCapacityPlanner puCapacityPlanner;
        private final Workflow workflow;

        public CompromisedDeploymentHandler(PuCapacityPlanner puCapacityPlanner, Workflow workflow) {
            this.puCapacityPlanner = puCapacityPlanner;
            this.workflow = workflow;
        }
        
        public void run() {
            if (deploymentStatusCompromised()) {
                logger.finest(CompromisedDeploymentHandler.class.getSimpleName() + " invoked");
                workflow.breakWorkflow();
                workflow.add(new StartGscHandler(puCapacityPlanner, workflow));
            }
        }
        
        private boolean deploymentStatusCompromised() {
            DeploymentStatus status = puCapacityPlanner.getProcessingUnit().getStatus();
            return DeploymentStatus.BROKEN.equals(status) || DeploymentStatus.COMPROMISED.equals(status);
        }
    }
}
