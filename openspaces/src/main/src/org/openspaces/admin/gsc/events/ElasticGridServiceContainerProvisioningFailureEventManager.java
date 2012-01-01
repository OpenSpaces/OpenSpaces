package org.openspaces.admin.gsc.events;


public interface ElasticGridServiceContainerProvisioningFailureEventManager {

    /**
     * Adds an event listener, events will be raised if a gsc failed to be provisioned.
     */
    public void add(ElasticGridServiceContainerProvisioningFailureEventListener listener);
    
    /**
     * Removes an event listener.
     */
    public void remove(ElasticGridServiceContainerProvisioningFailureEventListener listener);
}
