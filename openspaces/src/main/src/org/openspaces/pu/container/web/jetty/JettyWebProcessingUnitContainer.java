package org.openspaces.pu.container.web.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * @author kimchy
 */
public class JettyWebProcessingUnitContainer implements ApplicationContextProcessingUnitContainer {

    private static final Log logger = LogFactory.getLog(JettyWebProcessingUnitContainer.class);

    private ApplicationContext applicationContext;

    private WebAppContext webAppContext;

    private HandlerContainer container;

    private Server server;

    /**
     */
    public JettyWebProcessingUnitContainer(ApplicationContext applicationContext, WebAppContext webAppContext,
                                           HandlerContainer container, Server server) {
        this.applicationContext = applicationContext;
        this.webAppContext = webAppContext;
        this.container = container;
        this.server = server;
    }

    /**
     * Returns the spring application context this processing unit container wraps.
     */
    public ApplicationContext getApplicationContext() {
        ApplicationContext webContext = ContextLoader.getCurrentWebApplicationContext();
        if (webContext != null) {
            return webContext;
        }
        return applicationContext;
    }

    /**
     * Closes the processing unit container by destroying the Spring application context.
     */
    public void close() throws CannotCloseContainerException {

        if (!webAppContext.isRunning())
            return;

        try {
            webAppContext.stop();
        } catch (Exception e) {
            logger.warn("Faield to stop web context");
        }

        if (container != null) {
            server.removeHandler(webAppContext);
        }

        if (applicationContext instanceof DisposableBean) {
            try {
                ((DisposableBean) applicationContext).destroy();
            } catch (Exception e) {
                throw new CannotCloseContainerException("Failed to close container with application context ["
                        + applicationContext + "]", e);
            }
        }
    }

}
