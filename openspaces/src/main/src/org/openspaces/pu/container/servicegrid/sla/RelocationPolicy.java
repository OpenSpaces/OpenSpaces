package org.openspaces.pu.container.servicegrid.sla;

/**
 */
public class RelocationPolicy extends AbstractPolicy {

    public String toString() {
        return "RelocationPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh() + "]";
    }
}
