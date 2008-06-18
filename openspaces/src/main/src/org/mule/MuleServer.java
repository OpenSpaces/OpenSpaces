package org.mule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Overrides mule built in <code>MuleServer</code> in order to make its static <code>MuleContext</code>
 * class loader aware.
 *
 * @author kimchy
 */
public class MuleServer {

    private static Log logger = LogFactory.getLog(MuleServer.class);

    private static final ConcurrentHashMap<ClassLoader, MuleContext> muleContextMap = new ConcurrentHashMap<ClassLoader, MuleContext>();

    static {
        logger.info("Using OpenSpaces MuleServer");
    }

    public static MuleContext getMuleContext() {
        MuleContext muleContext = muleContextMap.get(Thread.currentThread().getContextClassLoader());
        if (logger.isTraceEnabled()) {
            logger.trace("Returning context " + muleContext + " under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
        }
        return muleContext;
    }

    public static void setMuleContext(MuleContext muleContext) {
        if (muleContext == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Removing context under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
            }
            muleContextMap.remove(Thread.currentThread().getContextClassLoader());
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Setting context " + muleContext + " under class loader [" + Thread.currentThread().getContextClassLoader() + "]");
            }
            muleContextMap.put(Thread.currentThread().getContextClassLoader(), muleContext);
        }
    }

}
