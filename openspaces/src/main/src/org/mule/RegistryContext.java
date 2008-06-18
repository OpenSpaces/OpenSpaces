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
        logger.info("Using OpenSpaces RegistryContext");
    }

    public static Registry getRegistry() {
        Registry registry = registryMap.get(Thread.currentThread().getContextClassLoader());
        if (logger.isTraceEnabled()) {
            logger.trace("Returning registry " + registry + " under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
        }
        return registry;
    }

    public static synchronized void setRegistry(Registry registry) {
        if (registry == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Removing registry under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
            }
            registryMap.remove(Thread.currentThread().getContextClassLoader());
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Setting registry " + registry + " under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
            }
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
