package org.openspaces.grid.esm;

import java.util.Properties;

public class ElasticDeploymentContextProperties {
    
    static final String INITIAL_JAVA_HEAP_SIZE = "initialJavaHeapSize";
    static final String MAXIMUM_JAVA_HEAP_SIZE = "maximumJavaHeapSize";
    static final String MIN_MEMORY = "minMemory";
    static final String MAX_MEMORY = "maxMemory";
    static final String DEPLOYMENT_ISOLATION = "deploymentIsolation";
    static final String VM_ARGUMENTS = "vmArguments";
    static final String ELASTIC = "elastic";
    static final String SLA = "sla";
    static final String ELASTIC_SCALE_CONFIG = "elasticScaleConfig";
    static final String ZONE = "zone";
    static final String TENANT = "tenant";

    private final Properties properties;

    public ElasticDeploymentContextProperties(Properties properties) {
        this.properties = properties;
    }
    
    public String getInitialJavaHeapSize() {
        return properties.getProperty(INITIAL_JAVA_HEAP_SIZE);
    }
    
    public String getMaximumJavaHeapSize() {
        return properties.getProperty(MAXIMUM_JAVA_HEAP_SIZE);
    }
    
    public String getMinMemoryCapacity() {
        return properties.getProperty(MIN_MEMORY);
    }
    
    public String getMaxMemoryCapacity() {
        return properties.getProperty(MAX_MEMORY);
    }
    
    public String getDeploymentIsolationLevel() {
        return properties.getProperty(DEPLOYMENT_ISOLATION);
    }
    
    public String getVmArguments() {
        return properties.getProperty(VM_ARGUMENTS);
    }
    
    public String getSla() {
        return properties.getProperty(SLA);
    }
    
    public boolean isElastic() {
        return properties.containsKey(ELASTIC);
    }
    
    public String getElasticScaleConfig() {
        return properties.getProperty(ELASTIC_SCALE_CONFIG);
    }
    
    public String getZoneName() {
        return properties.getProperty(ZONE);
    }
    
    public String getTenant() {
        return properties.getProperty(TENANT);
    }
}
