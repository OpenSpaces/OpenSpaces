package org.openspaces.pu.container.servicegrid.sla;

/**
 */
public class ScaleUpPolicy extends AbstractPolicy {

    private int scaleUpTo;

    public int getScaleUpTo() {
        return scaleUpTo;
    }

    public void setScaleUpTo(int scaleUpTo) {
        this.scaleUpTo = scaleUpTo;
    }

    public String toString() {
        return "ScaleUpPolicy monitor [" + getMonitor() + "] low [" + getLow() + "] high [" + getHigh()
                + "] scaleUpTo [" + getScaleUpTo() + "]";
    }
}
