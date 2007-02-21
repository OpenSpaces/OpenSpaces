package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 13, 2007
 * Time: 1:07:49 AM
 */
public interface Policy {
// -------------------------- OTHER METHODS --------------------------

    int getHigh();

    int getLow();

    String getMonitor();

    void setHigh(int high);

    void setLow(int low);

    void setMonitor(String watch);
}
