package org.openspaces.pu.container.servicegrid.sla;

/**
 */
public abstract class AbstractPolicy implements Policy {

    private String monitor;

    private double low;

    private double high;

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }
}
