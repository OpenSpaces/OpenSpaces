package org.openspaces.pu.container.jee.jetty;

import com.j_spaces.core.IJSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.util.LazyList;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.jee.sessions.jetty.GigaSessionIdManager;
import org.openspaces.jee.sessions.jetty.GigaSessionManager;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import java.lang.reflect.Field;

/**
 * An jetty specific {@link javax.servlet.ServletContextListener} that is automatically loaded by the
 * {@link org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener}.
 * <p/>
 * <p>Support specific GigaSpace based session storge when using the <code>jetty.sessions.spaceUrl</code> parameter
 * within the (web) processing unit properties. It is handled here since we want to setup the session support under
 * the web application class loader and not under the class loader that starts up jetty.
 *
 * @author kimchy
 */
public class JettyWebApplicationContextListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(JettyWebApplicationContextListener.class);

    /**
     * A deploy property that controls if Jetty will store the session on the Space. Just by specifying the
     * url it will automatically enable it.
     */
    public static final String JETTY_SESSIONS_URL = "jetty.sessions.spaceUrl";

    /**
     * How often the scavenger thread will run in order to check for expired sessions. Set in
     * <b>seconds</b> and defaults to <code>60 * 5</code> seconds (5 minutes).
     */
    public static final String JETTY_SESSIONS_SCAVENGE_PERIOD = "jetty.sessions.scavengePeriod";

    /**
     * How often an actual update of a <b>non dirty</b> session will be performed to the Space. Set in
     * <b>seconds</b> and defaults to <code>60</code> seconds.
     */
    public static final String JETTY_SESSIONS_SAVE_PERIOD = "jetty.sessions.savePeriod";

    /**
     * The lease of the {@link org.openspaces.jee.sessions.jetty.SessionData} that is written to the Space. Set
     * in <b>seconds</b> and defaults to FOREVER.
     */
    public static final String JETTY_SESSIONS_LEASE = "jetty.sessions.lease";

    /**
     * Controls, using a deployment property, the timeout value of sessions. Set in <b>minutes</b>.
     */
    public static final String JETTY_SESSIONS_TIMEOUT = "jetty.sessions.timeout";

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();

        BeanLevelProperties beanLevelProperties = (BeanLevelProperties) servletContext.getAttribute(JeeProcessingUnitContainerProvider.BEAN_LEVEL_PROPERTIES_CONTEXT);
        ClusterInfo clusterInfo = (ClusterInfo) servletContext.getAttribute(JeeProcessingUnitContainerProvider.CLUSTER_INFO_CONTEXT);
        if (beanLevelProperties != null) {

            // automatically enable GigaSpaces Session Manager when passing the relevant property
            String sessionsSpaceUrl = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_URL);
            if (sessionsSpaceUrl != null) {
                logger.info("Jetty GigaSpace Session support using space url [" + sessionsSpaceUrl + "]");
                // a hack to get the jetty context
                Context jettyContext = (Context) ((ContextHandler.SContext) servletContext).getContextHandler();
                SessionHandler sessionHandler = jettyContext.getSessionHandler();
                try {
                    sessionHandler.stop();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to stop session handler to inject our own session manager", e);
                }

                GigaSessionManager gigaSessionManager = new GigaSessionManager();
                gigaSessionManager.setSpaceUrl(sessionsSpaceUrl);
                gigaSessionManager.setBeanLevelProperties(beanLevelProperties);
                gigaSessionManager.setClusterInfo(clusterInfo);

                if (sessionsSpaceUrl.startsWith("bean://")) {
                    ApplicationContext applicationContext = (ApplicationContext) servletContext.getAttribute(JeeProcessingUnitContainerProvider.APPLICATION_CONTEXT_CONTEXT);
                    if (applicationContext == null) {
                        throw new IllegalStateException("Failed to find servlet context bound application context");
                    }
                    IJSpace space;
                    Object bean = applicationContext.getBean(sessionsSpaceUrl.substring("bean://".length()));
                    if (bean instanceof GigaSpace) {
                        space = ((GigaSpace) bean).getSpace();
                    } else if (bean instanceof IJSpace) {
                        space = (IJSpace) bean;
                    } else {
                        throw new IllegalArgumentException("Bean [" + bean + "] is not of either GigaSpace type or IJSpace type");
                    }
                    gigaSessionManager.setSpace(space);
                }

                String scavangePeriod = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_SCAVENGE_PERIOD);
                if (scavangePeriod != null) {
                    gigaSessionManager.setScavengePeriod(Integer.parseInt(scavangePeriod));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting scavenge period to [" + scavangePeriod + "] seconds");
                    }
                }
                String savePeriod = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_SAVE_PERIOD);
                if (savePeriod != null) {
                    gigaSessionManager.setSavePeriod(Integer.parseInt(savePeriod));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting save period to [" + savePeriod + "] seconds");
                    }
                }
                String lease = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_LEASE);
                if (lease != null) {
                    gigaSessionManager.setLease(Long.parseLong(lease));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting lease to [" + lease + "] milliseconds");
                    }
                }

                // copy over session settings
                gigaSessionManager.setSessionCookie(sessionHandler.getSessionManager().getSessionCookie());
                gigaSessionManager.setSessionDomain(sessionHandler.getSessionManager().getSessionDomain());
                gigaSessionManager.setSessionPath(sessionHandler.getSessionManager().getSessionPath());
                gigaSessionManager.setSessionURL(sessionHandler.getSessionManager().getSessionURL());
                gigaSessionManager.setUsingCookies(sessionHandler.getSessionManager().isUsingCookies());
                gigaSessionManager.setMaxCookieAge(sessionHandler.getSessionManager().getMaxCookieAge());
                gigaSessionManager.setSecureCookies(sessionHandler.getSessionManager().isUsingCookies());
                gigaSessionManager.setMaxInactiveInterval(sessionHandler.getSessionManager().getMaxInactiveInterval());
                gigaSessionManager.setHttpOnly(sessionHandler.getSessionManager().getHttpOnly());

                String sessionTimeout = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_TIMEOUT);
                if (sessionTimeout != null) {
                    gigaSessionManager.setMaxInactiveInterval(Integer.parseInt(sessionTimeout) * 60);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting session timeout to [" + sessionTimeout + "] seconds");
                    }
                }

                GigaSessionIdManager sessionIdManager = new GigaSessionIdManager(jettyContext.getServer());
                sessionIdManager.setWorkerName(clusterInfo.getName() + clusterInfo.getRunningNumberOffset1());
                gigaSessionManager.setIdManager(sessionIdManager);

                // copy over the session listeners
                try {
                    Field field = AbstractSessionManager.class.getDeclaredField("_sessionAttributeListeners");
                    field.setAccessible(true);
                    Object sessionAttributeListeners = field.get(sessionHandler.getSessionManager());
                    if (sessionAttributeListeners != null) {
                        for (int i = 0; i < LazyList.size(sessionAttributeListeners); i++) {
                            gigaSessionManager.addEventListener((HttpSessionAttributeListener) LazyList.get(sessionAttributeListeners, i));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to copy over _sessionAttributeListeners", e);
                }
                try {
                    Field field = AbstractSessionManager.class.getDeclaredField("_sessionListeners");
                    field.setAccessible(true);
                    Object sessionListeners = field.get(sessionHandler.getSessionManager());
                    if (sessionListeners != null) {
                        for (int i = 0; i < LazyList.size(sessionListeners); i++) {
                            gigaSessionManager.addEventListener((HttpSessionListener) LazyList.get(sessionListeners, i));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to copy over _sessionListeners", e);
                }

                sessionHandler.setSessionManager(gigaSessionManager);

                try {
                    sessionHandler.start();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to start session handler to inject our own session manager", e);
                }
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
