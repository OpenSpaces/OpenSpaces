package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.grid.gsm.PUsDetails;
/**
 * The purpose of this class is to make sure that admin API
 * view of the PUs and instances is based on the GSM and not the LUS.
 * The reason being is that the LUS may have just been restarted,
 * while the GSM has a consistent replicated view of the deployment.
 * 
 * @author itaif
 *
 */
public class ESMImplInitializer {
    
    private static final long DISCOVERY_POLLING_PERIOD_SECONDS = 20;
    
    private static final Logger logger = Logger.getLogger(ESMImplInitializer.class.getName());
    
    private final InternalAdmin admin;

    private final Runnable esmInitializer;
    
    public ESMImplInitializer (Admin admin, Runnable esmInitializer) {
       this.admin = (InternalAdmin) admin;
       this.esmInitializer = esmInitializer;
       schedule(0, TimeUnit.SECONDS);
    }
    
    private void schedule(long delay, TimeUnit timeunit) {
        
        if (delay > 0) {
            logger.info("Waiting " + timeunit.toSeconds(delay) + " seconds for all grid components to register with lookup service");
        }
        
        ((InternalAdmin)admin).scheduleOneTimeWithDelayNonBlockingStateChange( new Runnable() {

            @Override
            public void run() {
              ((InternalAdmin)admin).scheduleAdminOperation(new Runnable() {
                
                @Override
                public void run() {
                    if (isLookupDiscoverySyncedWithGsm()) {
                        esmInitializer.run();
                    }
                    else {
                        //retry
                        schedule(DISCOVERY_POLLING_PERIOD_SECONDS, TimeUnit.SECONDS);
                    }
                    
                }
            });
            }
          }, 
        delay,timeunit);
    }
    

    /**
     * Makes sure that data arriving from Lookup Service into Admin API cache
     * conforms to the data reported from the GSM.
     */
    private boolean isLookupDiscoverySyncedWithGsm() {
        
        admin.getGridServiceManagers().waitForAtLeastOne();
        
        //for each gsm
        for (final GridServiceManager gsm : admin.getGridServiceManagers()) {
            PUsDetails pusDetails;
            try {
                pusDetails = ((InternalGridServiceManager) gsm).getGSM().getPUsDetails();
            } catch (final RemoteException e) {
                logger.log(Level.WARNING, "Failed to get PU details from GSM",e);
                return false;
            }
            
            // for each pu
            for (final PUDetails details : pusDetails.getDetails()) {
                
                String puName = details.getName();
                
                // check pu
                ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
                if (pu == null) {
                    logger.log(Level.INFO, "Waiting for PU " + puName + " to be discovered");
                    return false;
                }
                
                // check pu instances
                final ProcessingUnitInstance[] instances = pu.getInstances();
                final int numberOfInstancesAccordingToGsm = details.getNumberOfInstances() * ( details.getNumberOfBackups() + 1);
                if (instances.length != numberOfInstancesAccordingToGsm) {
                    logger.log(Level.INFO, "Waiting for PU " + puName + " instances to be discovered. Discovered " + instances.length + " expected " + numberOfInstancesAccordingToGsm);
                    return false;
                }
                
                // check containers
                for (ProcessingUnitInstance instance : instances) {
                    if (instance.getGridServiceContainer() == null) {
                        logger.log(Level.INFO, "Waiting for container hosting PU instance " + instance.getProcessingUnitInstanceName() + " to be discovered.");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
