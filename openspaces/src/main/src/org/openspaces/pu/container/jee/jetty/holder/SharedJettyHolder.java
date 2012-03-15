/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.pu.container.jee.jetty.holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.MultiException;

/**
 * A shared jetty holder that keeps upon first construction will store a static jetty instance and will
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
                server.setStopAtShutdown(false);
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

    public void openConnectors() throws Exception {
        Connector[] connectors = server.getConnectors();
        for (Connector c : connectors) {
            c.open();
        }
    }

    public void closeConnectors() throws Exception {
        Connector[] connectors = server.getConnectors();
        MultiException ex = new MultiException();
        for (Connector c : connectors) {
            try {
                c.close();
            }
            catch (Exception e) {
                ex.add(e);
            }
        }
        ex.ifExceptionThrowMulti();
    }


    public void start() throws Exception {
        synchronized (serverLock) {
            if (++serverCount == 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting jetty server [" + server + "]");
                }
                server.start();
            }
        }
    }

    public void stop() throws Exception {
        synchronized (serverLock) {
            if (--serverCount == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping jetty server [" + server + "]");
                }
                server.stop();
                server.destroy();
            }
        }
    }

    public Server getServer() {
        return server;
    }

    public boolean isSingleInstance() {
        return true;
    }
}
