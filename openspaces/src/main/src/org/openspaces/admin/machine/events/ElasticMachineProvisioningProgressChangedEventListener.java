package org.openspaces.admin.machine.events;


/**
 * An event that provides state change indication for the process of starting new virtual machines for Elastic PUs 
 * @author itaif
 */
public interface ElasticMachineProvisioningProgressChangedEventListener {
    void elasticMachineProvisioningProgressChanged(ElasticMachineProvisioningProgressChangedEvent event);
}
