package org.openspaces.admin.gsc.events;

public interface ElasticGridServiceContainerProvisioningProgressChangedEventManager {

    /**
     * Adds the specified event listener and invokes the last progress event
     */
    void add(ElasticGridServiceContainerProvisioningProgressChangedEventListener listener);
    
    /**
     * Adds the specified event listener and if specified invokes the last progress event.
     * @param includeLastProgressEvent - If specified invokes the last progress event.
     */
    void add(ElasticGridServiceContainerProvisioningProgressChangedEventListener listener, boolean includeLastProgressEvent);

    /**
     * Removes the specified event listener.
     */
    void remove(ElasticGridServiceContainerProvisioningProgressChangedEventListener listener);
}
