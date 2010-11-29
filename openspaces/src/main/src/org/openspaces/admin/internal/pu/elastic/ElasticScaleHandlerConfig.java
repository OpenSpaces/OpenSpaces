package org.openspaces.admin.internal.pu.elastic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * Elastic scale configuration to be passed to the {@link org.openspaces.grid.esm.ElasticScaleHandler} implementation specified by the
 * <tt>onDemandElasticScaleClassName</tt> constructor parameter. Properties can be passed using the {@link #addProperty(String, String)}.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class ElasticScaleHandlerConfig {
    
    private final String className;
    private final Map<String, String>  properties;

    public ElasticScaleHandlerConfig(String className) {
        this(className,new HashMap<String, String>());
    }

    public ElasticScaleHandlerConfig(String className,Map<String, String> properties) {
        this.className = className;
        this.properties = properties;
    }

    public ElasticScaleHandlerConfig addProperty(String key, String value) {
        if (key.contains(";") || value.contains(";"))
            throw new IllegalArgumentException("properties should not contain the ';' delimeter");
        
        properties.put(key, value);
        return this;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("<class>").append(getClassName()).append("</class>");
        Map<String, String> properties = getProperties();
        if (!properties.isEmpty()) {
            out.append("<props>");
            Set<Entry<String, String>> entrySet = properties.entrySet();
            for (Entry<String, String> entry : entrySet) {
                out.append("<key>").append(entry.getKey()).append("</key>");
                out.append("<val>").append(entry.getValue()).append("</val>");
            }
            out.append("</props>");
        }
        return out.toString();
    }
    
    public static ElasticScaleHandlerConfig valueOf(String config) {
        
        int s,e;
        s = config.indexOf("<class>") + "<class>".length();
        e = config.indexOf("</class>");
        String classname = config.substring(s, e);
        ElasticScaleHandlerConfig elasticConfig = new ElasticScaleHandlerConfig(classname);
        s = config.indexOf("<props>",e) + "<props>".length();
        e = config.indexOf("</props>",e);
        if (e != -1) {
            String properties = config.substring(s, e);
            int s0 = 0, e0 = 0;
            while (e0 != -1) {
                s0 = properties.indexOf("<key>",e0) + "<key>".length();
                e0 = properties.indexOf("</key>",e0);
                if (e0 != -1) {
                    String key = properties.substring(s0, e0);
                    e0 = e0 + "</key>".length();
                    s0 = properties.indexOf("<val>",e0) + "<val>".length();
                    e0 = properties.indexOf("</val>",e0);
                    if (e0 != -1) {
                        String val = properties.substring(s0, e0);
                        e0 = e0 + "</val>".length();
                        elasticConfig.addProperty(key, val);
                    }
                }
            }
        }
        return elasticConfig;
    }
    
}
