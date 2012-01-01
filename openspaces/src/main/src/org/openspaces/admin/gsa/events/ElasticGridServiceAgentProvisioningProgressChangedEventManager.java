package org.openspaces.admin.gsa.events;

public interface ElasticGridServiceAgentProvisioningProgressChangedEventManager {

    /**
     * Adds the specified event listener and invokes the last progress event
     */
    void add(ElasticGridServiceAgentProvisioningProgressChangedEventListener listener);
    
    /**
     * Adds the specified event listener and if specified invokes the last progress event.
     * @param includeLastProgressEvent - If specified invokes the last progress event.
     */
    void add(ElasticGridServiceAgentProvisioningProgressChangedEventListener listener, boolean includeLastProgressEvent);

    /**
     * Removes the specified event listener.
     */
    void remove(ElasticGridServiceAgentProvisioningProgressChangedEventListener listener);
}
