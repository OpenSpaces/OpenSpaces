package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 13, 2007
 * Time: 1:08:08 AM
 */
public class ScaleUpPolicy implements Policy {
// ------------------------------ FIELDS ------------------------------

    String watch;
    int low;
    int high;
    int scaleUpTo;

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

    public int getScaleUpTo() {
        return scaleUpTo;
    }

    public void setScaleUpTo(int scaleUpTo) {
        this.scaleUpTo = scaleUpTo;
    }

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }
}
