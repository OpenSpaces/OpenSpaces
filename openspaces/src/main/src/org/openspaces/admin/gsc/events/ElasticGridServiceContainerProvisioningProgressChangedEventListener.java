package org.openspaces.admin.gsc.events;

/**
 * An event that provides state change indication for the process of starting new containers for Elastic PUs 
 * @author itaif
 */
public interface ElasticGridServiceContainerProvisioningProgressChangedEventListener {

    void elasticGridServiceContainerProvisioningProgressChanged(ElasticGridServiceContainerProvisioningProgressChangedEvent event);
}
