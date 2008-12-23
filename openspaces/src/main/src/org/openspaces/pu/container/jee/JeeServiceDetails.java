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

package org.openspaces.pu.container.jee;

import org.openspaces.pu.service.PlainServiceDetails;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A service that holds a jee container (such as jetty).
 *
 * @author kimchy
 */
public class JeeServiceDetails extends PlainServiceDetails {

    public static final String ATTRIBUTE_HOST = "host";
    public static final String ATTRIBUTE_PORT = "port";
    public static final String ATTRIBUTE_SSLPORT = "ssl-port";
    public static final String ATTRIBUTE_CONTEXTPATH = "context-path";
    public static final String ATTRIBUTE_SHARED = "shared";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_JEETYPE = "jee-type";

    public JeeServiceDetails() {
    }

    public JeeServiceDetails(String id, String host, int port, int sslPort, String contextPath, boolean shared,
                                           String type, JeeType jeeType) {
        super(id, "jee-container", type, host + ":" + port + contextPath, host + ":" + port + contextPath);
        getAttributes().put(ATTRIBUTE_HOST, host);
        getAttributes().put(ATTRIBUTE_PORT, port);
        getAttributes().put(ATTRIBUTE_SSLPORT, sslPort);
        getAttributes().put(ATTRIBUTE_CONTEXTPATH, contextPath);
        getAttributes().put(ATTRIBUTE_SHARED, shared);
        getAttributes().put(ATTRIBUTE_TYPE, type);
        getAttributes().put(ATTRIBUTE_JEETYPE, jeeType);
    }

    /**
     * Returns the host of where the service is running on.
     */
    public String getHost() {
        return (String) getAttributes().get(ATTRIBUTE_HOST);
    }

    /**
     * Returns the port of where the service is running on.
     */
    public int getPort() {
        return (Integer) getAttributes().get(ATTRIBUTE_PORT);
    }

    /**
     * Returns the ssl port of where the service is running on.
     */
    public int getSslPort() {
        return (Integer) getAttributes().get(ATTRIBUTE_SSLPORT);
    }

    /**
     * Returns the context path of the web application.
     */
    public String getContextPath() {
        return (String) getAttributes().get(ATTRIBUTE_CONTEXTPATH);
    }

    /**
     * Returns <code>true</code> if this web service is running on a shared instance of a
     * web container. <code>false</code> if the web application instance is running on its
     * own dedicated web container.
     */
    public boolean isShared() {
        return (Boolean) getAttributes().get(ATTRIBUTE_SHARED);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
