package org.openspaces.pu.container.servicegrid.sla;

import java.util.Map;

/**
 */
public class Generic extends Requirement {

    String name;

    Map attributes;

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
