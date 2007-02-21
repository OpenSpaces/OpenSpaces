package org.openspaces.pu.container.servicegrid.sla;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 21, 2007
 * Time: 12:54:49 AM
 */
public class Generic extends Requirement {
// ------------------------------ FIELDS ------------------------------

    String name;
    Map attributes;

// --------------------- GETTER / SETTER METHODS ---------------------

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
