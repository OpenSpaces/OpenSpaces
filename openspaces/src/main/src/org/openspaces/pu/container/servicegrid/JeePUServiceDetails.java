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

package org.openspaces.pu.container.servicegrid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A service that holds a jee container (such as jetty).
 *
 * @author kimchy
 */
public class JeePUServiceDetails implements PUServiceDetails, Externalizable {

    private static final long serialVersionUID = 1L;

    private String host;

    private int port;

    private int sslPort;

    private String contextPath;

    private boolean shared;

    private String type;

    public JeePUServiceDetails() {
    }

    public JeePUServiceDetails(String host, int port, int sslPort, String contextPath, boolean shared, String type) {
        this.host = host;
        this.port = port;
        this.sslPort = sslPort;
        this.contextPath = contextPath;
        this.shared = shared;
        this.type = type;
    }

    /**
     * The service type. Returns a constant value of <code>jee-container</code>.
     */
    public String getServiceType() {
        return "jee-container";
    }

    /**
     * Returns the description of the service. The value is <code>host:port/contextpath</code>.
     */
    public String getDescription() {
        return host + ":" + port + contextPath;
    }

    /**
     * Same as {@link #getDescription()}.
     */
    public String getLongDescription() {
        return getDescription();
    }

    /**
     * Returns the host of where the service is running on.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Returns the port of where the service is running on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the ssl port of where the service is running on.
     */
    public int getSslPort() {
        return sslPort;
    }

    /**
     * Returns the context path of the web application.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns <code>true</code> if this web service is running on a shared instance of a
     * web container. <code>false</code> if the web application instance is running on its
     * own dedicated web container.
     */
    public boolean isShared() {
        return shared;
    }

    /**
     * The type of the web container used. For example, <code>jetty</code>.
     */
    public String getType() {
        return type;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(host);
        out.writeInt(port);
        out.writeInt(sslPort);
        out.writeUTF(contextPath);
        out.writeBoolean(shared);
        out.writeUTF(type);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        host = in.readUTF();
        port = in.readInt();
        sslPort = in.readInt();
        contextPath = in.readUTF();
        shared = in.readBoolean();
        type = in.readUTF();
    }
}
