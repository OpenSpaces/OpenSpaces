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
package org.openspaces.grid.esm;

import com.j_spaces.kernel.SystemProperties;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.InternalAdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.pu.ProcessingUnit;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.grid.gsm.PUsDetails;
import com.gigaspaces.internal.utils.concurrent.GSThreadFactory;
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
    
    public interface AdminCreatedEventListener {
        void adminCreated(Admin admin);
    }
    
    private static final long DISCOVERY_POLLING_PERIOD_SECONDS = Integer.getInteger(SystemProperties.ESM_DISCOVERY_POLLING_INTERVAL, 20);
    
    private static final Logger logger = Logger.getLogger(ESMImplInitializer.class.getName());
    
    private InternalAdmin admin;

    private final AdminCreatedEventListener esmInitializer;

    private ExecutorService executor;
    
    public ESMImplInitializer(ESMImplInitializer.AdminCreatedEventListener adminCreatedEventListener) {
        this.esmInitializer = adminCreatedEventListener;
        final ClassLoader correctClassLoader = Thread.currentThread().getContextClassLoader();
        final boolean useDaemonThreads = true;
        executor = Executors.newSingleThreadExecutor(
                new GSThreadFactory("GS-EsmImplInitializer",useDaemonThreads) {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = super.newThread(r);
                        thread.setContextClassLoader(correctClassLoader);
                        return thread;
                     }});
        boolean restartAdmin = false;
        schedule(restartAdmin, 0, TimeUnit.SECONDS);
    }
    
    private void schedule(boolean restartAdmin, long delay, TimeUnit timeunit) {
        
        if (restartAdmin) {
            closeAdminIfNotNull();
        }
        
        createAdminIfNull();
        
        
        if (delay > 0) {
            logger.info("Waiting " + timeunit.toSeconds(delay) + " seconds for all grid components to register with lookup service");
        }
        
        ((InternalAdmin)admin).scheduleOneTimeWithDelayNonBlockingStateChange( new Runnable() {

            @Override
            public void run() {
              
                if (!isManagementDiscovered(admin)) {
                    // retry, give admin more time to discover management machines
                    boolean restartAdmin = false;
                    schedule(restartAdmin, DISCOVERY_POLLING_PERIOD_SECONDS, TimeUnit.SECONDS);
                    return;
                }
                
                //accessing the data from a single threaded admin is done on this NonBlockingStateChange event thread
                final Map<String,Integer> numberOfInstancesPerProcessingUnit = new HashMap<String,Integer>();
                for (ProcessingUnit pu : admin.getProcessingUnits()) {
                    numberOfInstancesPerProcessingUnit.put(pu.getName(), pu.getInstances().length);
                }
                
                final Set<GSM> gridServiceManagers = new HashSet<GSM>();
                for (GridServiceManager gsm : admin.getGridServiceManagers()) {
                    gridServiceManagers.add(((InternalGridServiceManager)gsm).getGSM());
                }
              
              //performing blocking network action is done on a separate thread
              executor.submit(new Runnable() {
                 
                @Override
                public void run() {
                    try { 
                        if (isLookupDiscoverySyncedWithGsm(gridServiceManagers, numberOfInstancesPerProcessingUnit)) {
                            esmInitializer.adminCreated(admin);
                            return;
                        }
                    }
                    catch(Throwable t) {
                        logger.log(Level.SEVERE, "Unexpected error while initializing ESM", t);
                    }
                    
                    //retry, restart admin since something is wrong
                    boolean restartAdmin = true;
                    schedule(restartAdmin, DISCOVERY_POLLING_PERIOD_SECONDS, TimeUnit.SECONDS);   
                }
            });
            }
          }, 
        delay,timeunit);
    }

    private void closeAdminIfNotNull() {
        if (admin != null) {
            admin.close();
            admin = null;
        }
    }

    private void createAdminIfNull() {
        if (admin == null) {
            admin = (InternalAdmin) (new InternalAdminFactory().singleThreadedEventListeners().createAdmin());
        }
    }

    /**
     * Makes sure that data arriving from Lookup Service into Admin API cache
     * conforms to the data reported from the GSM.
     */
    private static boolean isLookupDiscoverySyncedWithGsm(Set<GSM> gridServiceManagers, Map<String,Integer> numberOfInstancesPerProcessingUnit) {

        Set<String> managedPus = new HashSet<String>();
        
        //for each gsm
        for (final GSM gsm : gridServiceManagers) {
            PUsDetails pusDetails;
            try {
                pusDetails = gsm.getPUsDetails();
            } catch (final RemoteException e) {
                logger.log(Level.WARNING, "Failed to get PU details from GSM",e);
                return false;
            }
           
            // for each pu
            for (final PUDetails details : pusDetails.getDetails()) {
                
                String puName = details.getName();
                if (details.isManaging()) {
                    boolean added = managedPus.add(puName);
                    if (!added) {
                        logger.log(Level.WARNING,puName + " seems to have more than one managing GSMs. Waiting for a single managing GSM.");
                        return false;
                    }
                
                    // check pu instances
                    final int numberOfInstancesAccordingToGsm = details.getActualNumberOfInstances();
                    final Integer discoveredNumberOfInstances = numberOfInstancesPerProcessingUnit.get(puName);
                    if (discoveredNumberOfInstances == null) {
                    	logger.log(Level.INFO, "Waiting for PU " + puName + " to be discovered.");
                    	return false;
                    }
                    if (discoveredNumberOfInstances != numberOfInstancesAccordingToGsm) {
                        logger.log(Level.INFO, "Waiting for PU " + puName + " instances to be discovered. Discovered " + discoveredNumberOfInstances + " expected " + numberOfInstancesAccordingToGsm);
                        return false;
                    }
                }
            }
        }
        
        Set<String> unmanagedPus = new HashSet<String>(numberOfInstancesPerProcessingUnit.keySet());
        unmanagedPus.removeAll(managedPus);
        if (!unmanagedPus.isEmpty()) {
            logger.log(Level.INFO, "Waiting for a managing GSM for PUs:" + unmanagedPus);
            return false;
        }
     
        return true;
    }

    /**
     * We want to discover one ESM and at least one GSM We want to discover as much agents as we
     * can, to avoid false alerts such as "need more machines failures"
     * 
     * @param admin
     * @return
     */
    private static boolean isManagementDiscovered(InternalAdmin admin) {
           
        LookupService[] lookupServices = admin.getLookupServices().getLookupServices();
        if (lookupServices.length == 0) {
            logger.log(Level.INFO, "Waiting to discover at least one lookup service.");
            return false;
        }

        for (final LookupService lus : lookupServices) {
            if (lus.isDiscovered() && lus.getAgentId() != -1 && lus.getGridServiceAgent() == null) {
                logger.log(Level.INFO, "Waiting to discover GSA that started lookup service " + lus.getUid());
                return false;
            }
        }

        GridServiceManager[] gsms = admin.getGridServiceManagers().getManagers();
        if (gsms.length == 0) {
            logger.log(Level.INFO, "Waiting to discover at least one GSM");
            return false;
        }

        for (GridServiceManager gsm : gsms) {
            if (gsm.isDiscovered() && gsm.getAgentId() != -1 && gsm.getGridServiceAgent() == null) {
                logger.log(Level.INFO, "Waiting to discover GSA that started GSM " + gsm.getUid());
                return false;
            }
        }
        return true;
    }
    
}
