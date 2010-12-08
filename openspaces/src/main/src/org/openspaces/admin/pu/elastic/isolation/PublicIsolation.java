package org.openspaces.admin.pu.elastic.isolation;

public class PublicIsolation implements ElasticProcessingUnitDeploymentIsolation {

    public String getIsolationType() {
        return "public-isolation";
    }

}
