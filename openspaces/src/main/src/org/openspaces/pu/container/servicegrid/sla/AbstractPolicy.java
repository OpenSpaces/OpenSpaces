package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 20, 2007
 * Time: 7:21:29 PM
 */
public abstract class AbstractPolicy implements Policy {
// ------------------------------ FIELDS ------------------------------

    String monitor;
    int low;
    int high;

// --------------------- GETTER / SETTER METHODS ---------------------

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
