package org.openspaces.pu.sla.requirement;

/**
 * @author kimchy
 */
public class ZoneRequirement implements Requirement {

    private static final long serialVersionUID = 8258292384533829725L;

    private String zone;

    public ZoneRequirement() {
    }

    public ZoneRequirement(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
