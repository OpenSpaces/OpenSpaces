package org.openspaces.pu.container.servicegrid.sla;

import java.io.Serializable;

/**
 * A policy controls the runtime action that should be taken when the
 * monitor value associated with this policy (using {@link #setMonitor(String)})
 * breaches the policy threasholds.
 *
 * <p>The monitor is referenced by name and can use one of the built in monitors
 * that comes with the grid container (<code>CPU</code> and <code>Memory</code>)
 * or one of the custom monitors defined within the {@link SLA}.
 *
 * @author kimchy
 * @see org.openspaces.pu.container.servicegrid.sla.SLA
 */
public interface Policy extends Serializable {

    /**
     * The monitor name this policy will use in order to get check if its
     * value has breached the policy threasholds ({@link #setHigh(double)} and
     * {@link #setLow(double)}).
     */
    String getMonitor();

    /**
     * The monitor name this policy will use in order to get check if its
     * value has breached the policy threasholds ({@link #setHigh(double)} and
     * {@link #setLow(double)}).
     */
    void setMonitor(String watch);

    /**
     * The high threshold value of the policy.
     */
    double getHigh();

    /**
     * The high threshold value of the policy.
     */
    void setHigh(double high);

    /**
     * The low threshold value of the policy.
     */
    double getLow();

    /**
     * The low threshold value of the policy.
     */
    void setLow(double low);
}
