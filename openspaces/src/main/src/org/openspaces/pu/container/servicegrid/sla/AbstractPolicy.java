package org.openspaces.pu.container.servicegrid.sla;

/**
 * Simple base class for different policies.
 *
 * @author kimchy
 */
public abstract class AbstractPolicy implements Policy {

    private String monitor;

    private double low;

    private double high;

    /**
     * @see Policy#getHigh()
     */
    public double getHigh() {
        return high;
    }

    /**
     * @see Policy#setHigh(double)
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * @see Policy#getLow()
     */
    public double getLow() {
        return low;
    }

    /**
     * @see Policy#setLow(double)
     */
    public void setLow(double low) {
        this.low = low;
    }

    /**
     * @see Policy#getMonitor()
     */
    public String getMonitor() {
        return monitor;
    }

    /**
     * @see Policy#setMonitor(String)
     */
    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }
}
