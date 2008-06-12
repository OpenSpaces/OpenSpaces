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
        logger.debug("Using OpenSpaces MuleServer");
    }

    public static MuleContext getMuleContext() {
        return muleContextMap.get(Thread.currentThread().getContextClassLoader());
    }

    public static void setMuleContext(MuleContext muleContext) {
        if (muleContext == null) {
            muleContextMap.remove(Thread.currentThread().getContextClassLoader());
        } else {
            muleContextMap.put(Thread.currentThread().getContextClassLoader(), muleContext);
        }
    }

}
