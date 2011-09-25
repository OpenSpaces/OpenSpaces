package org.openspaces.admin.pu.elastic.topology;


/*
* The advantage of a state-less topology is that it does not have any inherit 
* scale limits. The disadvantage is that space data , notifications and tasks are
* serialized and sent over the network.
*/
public interface ElasticStatelessDeploymentTopology extends ElasticDeploymentTopology, EagerScaleTopology, ManualCapacityScaleTopology {

}