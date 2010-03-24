package org.openspaces.grid.esm;

import java.io.Serializable;
import java.util.Properties;


/**
 * Elastic scale configuration to be passed to the {@link ElasticScaleHandler} implementation specified by the
 * <tt>onDemandElasticScaleClassName</tt> constructor parameter. Properties can be passed using the {@link #addProperty(String, String)}.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class ElasticScaleHandlerConfig implements Serializable {
    
    private final String className;
    private final Properties properties = new Properties();

    public ElasticScaleHandlerConfig(String className) {
        this.className = className;
    }
    
    public ElasticScaleHandlerConfig addProperty(String key, String value) {
        if (key.contains(";") || value.contains(";"))
            throw new IllegalArgumentException("properties should not contain the ';' delimeter");
        
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
