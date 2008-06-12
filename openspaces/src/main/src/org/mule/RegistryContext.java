package org.mule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.registry.Registry;
import org.mule.registry.TransientRegistry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Overrides mule built in <code>RegistryContext</code> in order to make its static <code>Registry</code>
 * class loader aware.
 *
 * @author kimchy
 */
public class RegistryContext {

    private static Log logger = LogFactory.getLog(RegistryContext.class);

    private static final ConcurrentHashMap<ClassLoader, Registry> registryMap = new ConcurrentHashMap<ClassLoader, Registry>();

    static {
        logger.debug("Using OpenSpaces RegistryContext");
    }

    public static Registry getRegistry() {
        return registryMap.get(Thread.currentThread().getContextClassLoader());
    }

    public static synchronized void setRegistry(Registry registry) {
        if (registry == null) {
            registryMap.remove(Thread.currentThread().getContextClassLoader());
        } else {
            registryMap.put(Thread.currentThread().getContextClassLoader(), registry);
        }
    }

    public static MuleConfiguration getConfiguration() {
        return MuleServer.getMuleContext().getConfiguration();
    }

    public static Registry getOrCreateRegistry() {
        Registry registry = getRegistry();
        if (registry == null || registry.isDisposed()) {
            registry = new TransientRegistry();
        }
        return registry;
    }

}
