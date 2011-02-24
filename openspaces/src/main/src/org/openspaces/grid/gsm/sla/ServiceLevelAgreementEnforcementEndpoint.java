package org.openspaces.grid.gsm.sla;

import org.openspaces.admin.pu.ProcessingUnit;




/**
 * A single on-demand service level agreement.
 * 
 *  
 * @author itaif
 *
 * @param <POLICY> - service level agreement
 */
public interface ServiceLevelAgreementEnforcementEndpoint<POLICY extends ServiceLevelAgreementPolicy> {

    ProcessingUnit getProcessingUnit();
    
    /**
     * 
     * Enforces the specified SLA without blocking the calling thread.
     * To use this method call enforceSla method until it returns true. 
     * Dynamic changes in the sla are reflected by specifying a different sla parameter each call.
     * 
     * @param sla - the sla parameters or null if the sla is to be cleared.
     * @return true if the sla was reached (steady state).
     * @throws ServiceLevelAgreementEnforcementEndpointDestroyedException - this object has already been destroyed
     * 
     * {@code
     *      // Usage example for AdminService
     *      void enforceSlaAndWait(final AdminService service, final AdminServiceLevelAgreement sla) {
     *      final CountDownLatch latch = new CountDownLatch(1);
     *      ScheduledFuture scheduledTask = 
     *          ((InternalAdmin)admin).scheduleWithFixedDelayNonBlockingStateChange(
     *          new Runnable() {
     *
     *              public void run() {
     *                 
     *                   if (service.enforceSla(createSla)) {
     *                       latch.countDown();
     *                   }
     *                   
     *               }
     *               
     *           }, 
     *       
     *           0, 10, TimeUnit.SECONDS);
     *       
     *       try {
     *           latch.await();
     *       }
     *       finally {
     *           scheduledTask.cancel(false);
     *       }
     *    }
     * }
     * 
     */   
     boolean enforceSla(POLICY sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException;
}
