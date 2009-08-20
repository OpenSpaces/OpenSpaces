package org.openspaces.pu.container.servicegrid.jmxs;

import java.util.Date;

import org.jini.rio.jsb.ServiceBeanAdapterMBean;

/**
 * Secured PU JMX MBean interface restricting access to the default MBean
 * {@link ServiceBeanAdapterMBean}.
 * 
 * @author Moran Avigdor
 */
public interface SecuredPUExtensionMBean {
    /**
     * Get the Date the ServiceBean was started
     */
    Date getStarted();

    /**
     * Get the discovery groups
     */
    String getLookupGroups();
}
