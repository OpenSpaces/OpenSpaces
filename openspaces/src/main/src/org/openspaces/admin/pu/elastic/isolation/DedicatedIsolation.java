package org.openspaces.admin.pu.elastic.isolation;

public class DedicatedIsolation implements ElasticProcessingUnitDeploymentIsolation {

    public String getIsolationType() {
        return "dedicated-isolation";
    }

}
