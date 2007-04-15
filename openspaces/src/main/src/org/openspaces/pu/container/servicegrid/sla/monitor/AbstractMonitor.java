package org.openspaces.pu.container.servicegrid.sla.monitor;

/**
 * @author kimchy
 */
public abstract class AbstractMonitor implements Monitor {

    private String name;

    private long period = 5000;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

}
