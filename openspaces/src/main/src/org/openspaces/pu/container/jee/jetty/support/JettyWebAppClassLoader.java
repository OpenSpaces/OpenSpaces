package org.openspaces.pu.container.jee.jetty.support;

import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.jini.rio.boot.LoggableClassLoader;

import java.io.IOException;

/**
 * @author kimchy
 */
public class JettyWebAppClassLoader extends WebAppClassLoader implements LoggableClassLoader {

    private final String loggableName;

    public JettyWebAppClassLoader(ClassLoader parent, WebAppContext context, String loggableName) throws IOException {
        super(parent, context);
        this.loggableName = loggableName;
    }

    public String getLogName() {
        return loggableName; 
    }
}
