package org.openspaces.admin.pu.elastic.isolation;

public class SharedTenantIsolation implements ElasticProcessingUnitDeploymentIsolation {

    private final String tenant;

    public SharedTenantIsolation(String tenant) {
        this.tenant = tenant;
    }
    
    public String getTenant() {
        return this.tenant;
    }
    
    public String getIsolationType() {
        return "shared-tenant-isolation";
    }


}
