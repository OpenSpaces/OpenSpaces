package org.openspaces.pu.container.servicegrid.sla;

/**
 * Scale up policy will cause a processing unit instance to be created
 * when the policy associated monitor breaches its threashold values.
 *
 * @author kimchy
 */
public class ScaleUpPolicy extends AbstractPolicy {

    private int scaleUpTo;

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public int getScaleUpTo() {
        return scaleUpTo;
    }

    /**
     * The maximum number of processing instances this scale up policy will scale
     * up to. Should be higher than {@link #getHigh() high} value.
     */
    public void setScaleUpTo(int scaleUpTo) {
        this.scaleUpTo = scaleUpTo;
    }

    public String toString() {
        return "ScaleUpPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh()
                + "] scaleUpTo [" + getScaleUpTo() + "]";
    }
}
