package org.openspaces.admin.gsa.events;


public interface ElasticGridServiceAgentProvisioningFailureEventManager {

    /**
    * Adds an event listener, events will be raised if a machine failed to be provisioned for an Elastic PU.
    */
   public void add(ElasticGridServiceAgentProvisioningFailureEventListener listener);
   
   /**
    * Removes an event listener.
    */
   public void remove(ElasticGridServiceAgentProvisioningFailureEventListener listener);

}
