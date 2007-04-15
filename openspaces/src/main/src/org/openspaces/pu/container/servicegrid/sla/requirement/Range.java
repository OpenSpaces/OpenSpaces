package org.openspaces.pu.container.servicegrid.sla.requirement;

/**
 */
public class Range implements Requirement {

    private String watch;

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

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }
}
