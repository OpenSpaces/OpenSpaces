package org.openspaces.dsl;

import java.io.Serializable;
import java.util.Map;


public class PluginDescriptor  implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String clazz;
    private Map<String, Object> config;
    private String name;
    
    public String getClazz() {
        return clazz;
    }
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
    public Map<String, Object> getConfig() {
        return config;
    }
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
}
