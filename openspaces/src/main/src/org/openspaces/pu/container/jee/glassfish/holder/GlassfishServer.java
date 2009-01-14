/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.jee.glassfish.holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.Server;

import java.net.URL;

/**
 * @author kimchy
 */
public class GlassfishServer {

    private static final Log logger = LogFactory.getLog(GlassfishServer.class);

    private int port;

    private URL domainXmlUrl;

    private Server server;

    public GlassfishServer(int port, URL domainXml) throws Exception {
        this.port = port;
        this.domainXmlUrl = domainXml;
    }

    public void start() throws Exception {
        EmbeddedInfo embeddedInfo = new EmbeddedInfo();
        embeddedInfo.setHttpPort(port);
        if (domainXmlUrl != null) {
            embeddedInfo.setDomainXmlUrl(domainXmlUrl);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Starting " + toString());
        }
        server = Server.create(embeddedInfo);
        server.start();
    }

    public void stop() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping " + toString());
        }
        server.stop();
    }

    public Server getServer() {
        return server;
    }

    public int getPort() {
        return this.port;
    }

    public void incPort() {
        this.port++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Glassfish Server, Port [").append(port).append("]");
        if (domainXmlUrl != null) {
            sb.append(", domain [").append(domainXmlUrl).append("]");
        } else {
            sb.append(", default domain");
        }
        return sb.toString();
    }
}
