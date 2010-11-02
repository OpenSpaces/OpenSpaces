package org.openspaces.admin.pu.elastic;

import java.util.Properties;

public class ElasticDeploymentContextProperties {
    
    public static final String INITIAL_JAVA_HEAP_SIZE = "initialJavaHeapSize";
    public static final String MAXIMUM_JAVA_HEAP_SIZE = "maximumJavaHeapSize";
    public static final String MIN_MEMORY = "minMemory";
    public static final String MAX_MEMORY = "maxMemory";
    public static final String DEPLOYMENT_ISOLATION = "deploymentIsolation";
    public static final String VM_ARGUMENTS = "vmArguments";
    public static final String ELASTIC = "elastic";
    public static final String SLA = "sla";
    public static final String ELASTIC_SCALE_CONFIG = "elasticScaleConfig";
    public static final String ZONE = "zone";
    public static final String TENANT = "tenant";

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
