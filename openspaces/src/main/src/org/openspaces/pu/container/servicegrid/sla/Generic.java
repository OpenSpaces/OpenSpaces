package org.openspaces.pu.container.servicegrid.sla;

import java.util.Map;

/**
 */
public class Generic implements Requirement {

    private String name;

    private Map attributes;

    public Map getAttributes() {
        return attributes;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
