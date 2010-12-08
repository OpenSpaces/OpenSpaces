package org.openspaces.admin.pu.elastic.isolation;

public class ElasticProcessingUnitDeploymentIsolationUtils {

    public static PublicIsolation publicIsolation() {return new PublicIsolation();}
    public static SharedTenantIsolation sharedTenantIsolation(String tenant) {return new SharedTenantIsolation(tenant);}
    public static DedicatedIsolation dedicatedIsolation() { return new DedicatedIsolation(); }
}
