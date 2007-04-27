package org.openspaces.pu.sla.requirement;

import java.util.Map;

/**
 */
public class SystemRequirement implements Requirement {

    private String name;

    private Map attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Map getAttributes() {
        return attributes;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }
}
