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

package org.openspaces.pu.container.jee.jetty.holder;

import org.mortbay.jetty.Server;

/**
 * A generic holder that holds a Jetty server and controls its lifecycle. Note,
 * make not to call start and stop on {@link org.mortbay.jetty.Server}.
 *
 * @author kimchy
 */
public interface JettyHolder {

    /**
     * Open Jetty ports.
     */
    void openConnectors() throws Exception;

    /**
     * Closes Jetty ports.
     */
    void closeConnectors() throws Exception;

    /**
     * Start Jetty. Note, if this fails, make sure to call {@link #stop()}
     */
    void start() throws Exception;

    /**
     * Stops Jetty.
     */
    void stop() throws Exception;

    /**
     * Returns the jetty server.
     */
    Server getServer();

    /**
     * Returns <code>true</code> if this is a single instance.
     */
    boolean isSingleInstance();
}
