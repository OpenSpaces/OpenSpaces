package org.openspaces.grid.esm;

import java.io.Serializable;
import java.util.Properties;


public class ElasticScaleConfig implements Serializable {
    
    private final String className;
    private final Properties properties = new Properties();

    public ElasticScaleConfig(String onDemandElasticScaleClassName) {
        this.className = onDemandElasticScaleClassName;
    }
    
    public ElasticScaleConfig addProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    Properties getProperties() {
        return properties;
    }
}
