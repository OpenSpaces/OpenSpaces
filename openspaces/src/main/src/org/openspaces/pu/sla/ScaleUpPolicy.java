package org.openspaces.pu.sla;

/**
 * Scale up policy will cause a processing unit instance to be created
 * when the policy associated monitor breaches its threashold values.
 *
 * @author kimchy
 */
public class ScaleUpPolicy extends AbstractPolicy {

    private int maxInstances;

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public int getMaxInstances() {
        return maxInstances;
    }

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public void setMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public String toString() {
        return "ScaleUpPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh()
                + "] maxInstances [" + getMaxInstances() + "]";
    }
}
