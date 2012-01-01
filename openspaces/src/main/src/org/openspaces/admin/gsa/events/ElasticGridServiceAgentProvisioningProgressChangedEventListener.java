package org.openspaces.admin.gsa.events;

/**
 * An event that provides state change indication for the process of starting new agents for Elastic PUs 
 * @author itaif
 */
public interface ElasticGridServiceAgentProvisioningProgressChangedEventListener {

    void elasticGridServiceAgentProvisioningProgressChanged(ElasticGridServiceAgentProvisioningProgressChangedEvent event);

}
