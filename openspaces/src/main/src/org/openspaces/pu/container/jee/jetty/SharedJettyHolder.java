package org.openspaces.pu.container.jee.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

/**
 * A shared jetty holder that keeps upon first construction will store a static jetty instnace and will
 * reused it from then on. Upon the "last" call to stop, will actually stop the jetty instance.
 *
 * @author kimchy
 */
public class SharedJettyHolder implements JettyHolder {

    private static final Log logger = LogFactory.getLog(SharedJettyHolder.class);

    private static volatile Server server;

    private static final Object serverLock = new Object();

    private static volatile int serverCount = 0;

    public SharedJettyHolder(Server localServer) {
        synchronized (serverLock) {
            if (server == null) {
                server = localServer;
                if (logger.isDebugEnabled()) {
                    logger.debug("Usign new jetty server [" + server + "]");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Usign existing jetty server [" + server + "]");
                }
            }
        }
    }

    public void start() throws Exception {
        synchronized (serverLock) {
            if (++serverCount == 1) {
                server.start();
            }
        }
    }

    public void stop() throws Exception {
        synchronized (serverLock) {
            if (--serverCount == 0) {
                server.stop();
            }
        }
    }

    public Server getServer() {
        return server;
    }
}