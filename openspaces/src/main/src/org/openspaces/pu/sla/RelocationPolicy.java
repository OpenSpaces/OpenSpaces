package org.openspaces.pu.sla;

/**
 * Relocation policy will cause a processing unit instance to relocate when the policy
 * associated monitor breaches its threashold values. Relocation means that
 * the processing unit will be removed from its current grid container and
 * moved to a new one (that meets its requirements).
 *
 * @author kimchy
 */
public class RelocationPolicy extends AbstractPolicy {

    public String toString() {
        return "RelocationPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh() + "]";
    }
}
