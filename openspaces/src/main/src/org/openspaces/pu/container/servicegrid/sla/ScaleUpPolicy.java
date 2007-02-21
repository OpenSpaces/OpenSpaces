package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 13, 2007
 * Time: 1:08:08 AM
 */
public class ScaleUpPolicy extends AbstractPolicy {
// ------------------------------ FIELDS ------------------------------

    int scaleUpTo;

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getScaleUpTo() {
        return scaleUpTo;
    }

    public void setScaleUpTo(int scaleUpTo) {
        this.scaleUpTo = scaleUpTo;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public String toString() {
        return "ScaleUpPolicy{" +
                "\n\twatch='" + monitor + '\'' +
                "\n\tlow=" + low +
                "\n\thigh=" + high +
                "\n\tscaleUpTo=" + scaleUpTo +
                "\n}";
    }
}
