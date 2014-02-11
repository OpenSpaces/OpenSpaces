package org.openspaces.pu.sla;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.space.SecurityConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.grid.zone.ZoneHelper;


/**
 * {@link PrimaryZoneController} is a task that runs periodically and checks if
 * primary-zone definition is violated: 
 * 1. backups in primary zone
 * 2. primaries in backup zone if it happens instances are restarted and balance is restored.
 */
public class PrimaryZoneController
        implements InitializingBean, DisposableBean, ClusterInfoAware
{
    private static Logger            logger             = Logger.getLogger(PrimaryZoneController.class.getName());

    private String                   primaryZone;
    private String                   puName;

    private Admin                    admin              = null;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?>       handle;
    private int                      delayBetweenChecks = 60;

    private ClusterInfo              clusterInfo;
    private SecurityConfig           securityConfig;


    public PrimaryZoneController()
    {
    }

    public void afterPropertiesSet() throws Exception
    {
        
        if (clusterInfo == null)
        {
            throw new IllegalStateException(PrimaryZoneController.class.getSimpleName()
                    + " doesn't run on non-clustered pu.");
        }

        puName = clusterInfo.getName();
       
        AdminFactory a = new AdminFactory();
        if(securityConfig != null)
        {
            a.credentialsProvider(securityConfig.getCredentialsProvider() );
        }
            
        admin = a.create();
        
        GridServiceManager gsm = admin.getGridServiceManagers()
                .waitForAtLeastOne(30, TimeUnit.SECONDS);
        if (gsm == null)
        {
            throw new IllegalStateException("Can't find GSM - Abort deploy");
        }

        RebalancingTask task = new RebalancingTask();
        scheduler = Executors.newScheduledThreadPool(1);
        if (logger.isLoggable(Level.FINE))
            logger.fine("Primary zone [" + primaryZone + " ] balancer for ["
                    + puName + "] started.");

        handle = scheduler.scheduleWithFixedDelay(task,
                                                  10,
                                                  delayBetweenChecks,
                                                  TimeUnit.SECONDS);

    }

    private void rebalancePrimaryZoneIfNecessary()
    {
        // get all PU instances
        ProcessingUnit pu = admin.getProcessingUnits()
                .waitFor(puName, 30, TimeUnit.SECONDS);
        if (pu == null)
        {
            logger.warning("PU [" + puName + "] is offline");
            return;
        }

        // check that pu is intact - if not no rebalancing is done
        if (pu.getStatus() != DeploymentStatus.INTACT)
        {
            logger.fine("Waiting for pu [" + puName + "] to be deployed.");
            return;
        }

        ProcessingUnitPartition[] partitions = pu.getPartitions();

        Arrays.sort(partitions, new PUSorter());

        for (int i = 0; i < partitions.length; i++)
        {
            ProcessingUnitInstance primary = partitions[i].getPrimary();
            if (primary == null)
            {
                logger.fine("Primary instace is not available for "
                        + partitions[i].getProcessingUnit().getName());
                return;
            }
            Set<String> currentPrimaryZones = getZones(primary);

            ProcessingUnitInstance backup = partitions[i].getBackup();
            if (backup == null)
            {
                logger.fine("Backup instance is not available for "
                        + partitions[i].getProcessingUnit().getName());
                return;
            }

            Set<String> primaryZones = ZoneHelper.parseZones(primaryZone);
            Set<String> currentBackupZones = getZones(backup);

            // if backup is in primary zone, and primary is not,
            // then the current primary just needs to be restarted
            // the current backup will become primary and balance will be
            // restored.
            if (!containsAny(currentPrimaryZones, primaryZones)
                    && containsAny(currentBackupZones, primaryZones))
            {
                logger.warning("Primary instance of PU "
                        + primary.getProcessingUnitInstanceName()
                        + " located on machine ["
                        + primary.getMachine().getHostName() + ":"
                        + primary.getMachine().getHostAddress()
                        + "] is not running on primary zone [" + primaryZone
                        + "]. It will be restarted.");

                primary.restartAndWait(60, TimeUnit.SECONDS);
            }
        }
    }

    private boolean containsAny(Set<String> containerZones,
            Set<String> primaryZones)
    {
        for (String primaryZone : primaryZones)
        {
            if (containerZones.contains(primaryZone))
                return true;
        }
        return false;
    }

    private Set<String> getZones(ProcessingUnitInstance puInstance)
    {
        if (puInstance.getGridServiceContainer() == null)
            throw new IllegalStateException("GSC not available for pu instance "
                    + puInstance.getProcessingUnitInstanceName());
        Set<String> currentPrimaryZones = puInstance.getGridServiceContainer()
                .getExactZones()
                .getZones();
        return currentPrimaryZones;
    }

    class PUSorter
            implements Comparator<ProcessingUnitPartition>
    {
        public int compare(ProcessingUnitPartition o1,
                ProcessingUnitPartition o2)
        {
            int o1Count = o1.getPartitionId();
            int o2Count = o2.getPartitionId();
            return o1Count - o2Count;
        }
    }

    public String getPrimaryZone()
    {
        return primaryZone;
    }

    public void setPrimaryZone(String primaryZone)
    {
        this.primaryZone = primaryZone;
    }

   
    @Override
    public void destroy()
    {
        if (admin != null)
            admin.close();

        if (scheduler != null)
        {
            handle.cancel(true);
            scheduler.shutdown();
        }
    }

    public int getDelayBetweenChecks()
    {
        return delayBetweenChecks;
    }

    public void setDelayBetweenChecks(int delayBetweenChecks)
    {
        this.delayBetweenChecks = delayBetweenChecks;
    }

    class RebalancingTask
            implements Runnable
    {
        public void run()
        {
            // Periodically check the cluster status and rebalance if necessary
            try
            {
                // make sure this task is only active on a single instance
                if (clusterInfo.getInstanceId() == 1L
                        && clusterInfo.getBackupId() == null)
                {
                    logger.finest("Checking primary-zone [" + primaryZone
                            + "] sla.");
                    rebalancePrimaryZoneIfNecessary();
                }
            }
            catch (IllegalStateException ise)
            {
                logger.warning(ise.getMessage());
            }
            catch (Throwable e)
            {
                logger.log(Level.SEVERE, "Failed to rebalance primary zone ["
                        + primaryZone + "] for pu [" + puName + "]", e);
            }
        }
    }

    @Override
    public void setClusterInfo(ClusterInfo clusterInfo)
    {
        this.clusterInfo = clusterInfo;

    }

    public SecurityConfig getSecurityConfig()
    {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig)
    {
        this.securityConfig = securityConfig;
    }

}
