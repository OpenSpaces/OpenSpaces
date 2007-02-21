package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 20, 2007
 * Time: 11:50:07 PM
 */
public class RangeRequirement extends Requirement {
// ------------------------------ FIELDS ------------------------------

    String watch;
    double low;
    double high;

// --------------------- GETTER / SETTER METHODS ---------------------

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
