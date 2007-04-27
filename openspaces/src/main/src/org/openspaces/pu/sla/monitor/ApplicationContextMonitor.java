package org.openspaces.pu.sla.monitor;

import org.springframework.context.ApplicationContext;

/**
 * @author kimchy
 */
public interface ApplicationContextMonitor extends Monitor {

    void setApplicationContext(ApplicationContext applicationContext);
}
