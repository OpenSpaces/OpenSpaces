package org.openspaces.pu.container.jee.glassfish.holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.embed.Server;

/**
 * @author kimchy
 */
public class SharedGlassfishHolder implements GlassfishHolder {

    private static final Log logger = LogFactory.getLog(SharedGlassfishHolder.class);

    private static volatile GlassfishServer server;

    private static final Object serverLock = new Object();

    private static volatile int serverCount = 0;

    public SharedGlassfishHolder(GlassfishServer localServer) {
        synchronized (serverLock) {
            if (server == null) {
                server = localServer;
                if (logger.isDebugEnabled()) {
                    logger.debug("Usign new Glassfish server [" + server + "]");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Usign existing Glassfish server [" + server + "]");
                }
            }
        }
    }

    public void start() throws Exception {
        synchronized (serverLock) {
            if (++serverCount == 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting Glassfish server [" + server + "]");
                }
                server.start();
            }
        }
    }

    public void stop() throws Exception {
        synchronized (serverLock) {
            if (--serverCount == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping Glassfish server [" + server + "]");
                }
                server.stop();
            }
        }
    }

    public Server getServer() {
        return server.getServer();
    }

    public boolean isSingleInstance() {
        return true;
    }

    public int getPort() {
        return server.getPort();
    }

    public void incPort() {
        server.incPort();
    }
}