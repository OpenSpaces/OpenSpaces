package org.openspaces.pu.container.servicegrid.sla;

import java.io.Serializable;

/**
 */
public interface Policy extends Serializable {

    int getHigh();

    int getLow();

    String getMonitor();

    void setHigh(int high);

    void setLow(int low);

    void setMonitor(String watch);
}
