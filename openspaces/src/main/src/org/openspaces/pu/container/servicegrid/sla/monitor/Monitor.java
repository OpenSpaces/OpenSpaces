package org.openspaces.pu.container.servicegrid.sla.monitor;

import java.io.Serializable;

/**
 * @author kimchy
 */
public interface Monitor extends Serializable {

    String getName();

    long getPeriod();

    double getValue();

    int getHistorySize();
}
