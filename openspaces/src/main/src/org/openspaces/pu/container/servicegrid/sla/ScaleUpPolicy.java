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
    int scaleTo;

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

    public int getScaleTo() {
        return scaleTo;
    }

    public void setScaleTo(int scaleTo) {
        this.scaleTo = scaleTo;
    }

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }
}
