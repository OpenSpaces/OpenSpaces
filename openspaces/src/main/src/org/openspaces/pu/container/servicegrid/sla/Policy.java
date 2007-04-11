package org.openspaces.pu.container.servicegrid.sla;

/**
 */
public interface Policy {

    int getHigh();

    int getLow();

    String getMonitor();

    void setHigh(int high);

    void setLow(int low);

    void setMonitor(String watch);
}
