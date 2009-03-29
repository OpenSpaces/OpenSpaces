package org.openspaces.pu.sla.requirement;

/**
 * @author kimchy
 */
public class ZoneRequirement implements Requirement {

    private String zone;

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
