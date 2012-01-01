package org.openspaces.admin.machine.events;

public interface ElasticMachineProvisioningProgressChangedEventManager {
    
    /**
     * Adds the specified event listener.
     * @param includeLastProgressEvent if true invokes the last progress changed event
     */
    void add(ElasticMachineProvisioningProgressChangedEventListener listener, boolean includeLastProgressEvent);
    
    /**
     * Adds the specified event listener and invokes with the last progress changed event.
     */
    void add(ElasticMachineProvisioningProgressChangedEventListener listener);
    
    /**
     * removes the specified event listener
     */
    void remove(ElasticMachineProvisioningProgressChangedEventListener listener);
}
