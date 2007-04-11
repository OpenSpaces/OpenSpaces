package org.openspaces.pu.container.servicegrid.sla;

/**
 */
public abstract class AbstractPolicy implements Policy {

    private String monitor;

    private int low;

    private int high;

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }
}
