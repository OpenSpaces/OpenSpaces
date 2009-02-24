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

package org.openspaces.pu.container.jee.glassfish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.embed.Application;
import org.jini.rio.boot.BootUtil;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.container.jee.JeeType;
import org.openspaces.pu.container.jee.glassfish.holder.GlassfishHolder;
import org.openspaces.pu.container.jee.glassfish.holder.WebappConfiguration;
import org.openspaces.pu.service.ServiceDetails;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author kimchy
 */
public class GlassfishProcessingUnitContainer implements org.openspaces.pu.container.jee.JeeProcessingUnitContainer {

    private static final Log logger = LogFactory.getLog(GlassfishProcessingUnitContainer.class);

    private ApplicationContext applicationContext;

    private ApplicationContext webApplicationContext;

    private Application application;

    private GlassfishHolder glassfishHolder;

    private WebappConfiguration webappConfiguration;

    public GlassfishProcessingUnitContainer(ApplicationContext applicationContext, Application application,
                                            GlassfishHolder glassfishHolder, WebappConfiguration webappConfiguration) {
        this.applicationContext = applicationContext;
        this.application = application;
        this.glassfishHolder = glassfishHolder;
        this.webappConfiguration = webappConfiguration;
        this.webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        if (webApplicationContext == null) {
            webApplicationContext = applicationContext;
        }
    }

    /**
     * Returns the spring application context this processing unit container wraps.
     */
    public ApplicationContext getApplicationContext() {
        return webApplicationContext;
    }

    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[]{getJeeDetails()};
    }

    public JeeServiceDetails getJeeDetails() {
        int port = glassfishHolder.getPort();
        String host = null;
        if (host == null) {
            try {
                host = BootUtil.getHostAddress();
            } catch (UnknownHostException e) {
                logger.warn("Unknown host exception", e);
            }
        }
        InetSocketAddress addr = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
        return new JeeServiceDetails("glassfish:" + addr.getAddress().getHostAddress() + ":" + port,
                addr.getAddress().getHostAddress(),
                port,
                -1,
                webappConfiguration.getContextPath(),
                glassfishHolder.isSingleInstance(),
                "glassfish",
                JeeType.GLASSFISH);
    }

    /**
     * Closes the processing unit container by destroying the web application and the Spring application context.
     */
    public void close() throws CannotCloseContainerException {

        try {
            application.undeploy();
        } catch (Exception e) {
            logger.warn("Failed to stop/destroy web context", e);
        }

        // close the application context anyhow (it might be closed by the webapp context, but it
        // might not if it is not a pure Spring application).
        ConfigurableApplicationContext confAppContext = (ConfigurableApplicationContext) applicationContext;
        confAppContext.close();

        try {
            glassfishHolder.stop();
        } catch (Exception e) {
            logger.warn("Failed to stop glassfish server", e);
        }
    }

}