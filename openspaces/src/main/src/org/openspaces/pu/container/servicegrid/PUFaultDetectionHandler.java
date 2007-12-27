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

import com.sun.jini.config.Config;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import org.jini.rio.resources.client.AbstractFaultDetectionHandler;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A processing unit fault detection handler. Invokes {@link PUServiceBean#isAlive()}.
 *
 * @author kimchy
 */
public class PUFaultDetectionHandler extends AbstractFaultDetectionHandler {

    public static final String INVOCATION_DELAY_KEY = "invocationDelay";
    /**
     * Component name, used for config and logger
     */
    private static final String COMPONENT =
            "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler";
    /**
     * A Logger
     */
    static Logger logger = Logger.getLogger(COMPONENT);

    /**
     * @see org.jini.rio.core.FaultDetectionHandler#setConfiguration
     */
    public void setConfiguration(String[] configArgs) {
        if (configArgs == null)
            throw new NullPointerException("configArgs is null");
        try {
            this.configArgs = new String[configArgs.length];
            System.arraycopy(configArgs, 0, this.configArgs, 0, configArgs.length);

            this.config = ConfigurationProvider.getInstance(configArgs);

            invocationDelay = Config.getLongEntry(config,
                    COMPONENT,
                    INVOCATION_DELAY_KEY,
                    DEFAULT_INVOCATION_DELAY,
                    0,
                    Long.MAX_VALUE);
            retryCount = Config.getIntEntry(config,
                    COMPONENT,
                    RETRY_COUNT_KEY,
                    DEFAULT_RETRY_COUNT,
                    0,
                    Integer.MAX_VALUE);
            retryTimeout = Config.getLongEntry(config,
                    COMPONENT,
                    RETRY_TIMEOUT_KEY,
                    DEFAULT_RETRY_TIMEOUT,
                    0,
                    Long.MAX_VALUE);

            if (logger.isLoggable(Level.FINEST)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("AdminFaultDetectionHandler Properties : ");
                buffer.append("retry count=" + retryCount + ", ");
                buffer.append("retry timeout=" + retryTimeout);
                logger.finest(buffer.toString());
            }
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Setting Configuration", e);
        }
    }

    /**
     * Get the class which implements the ServiceMonitor
     */
    protected ServiceMonitor getServiceMonitor() throws Exception {
        return new ServiceAdminManager();
    }

    class ServiceAdminManager implements ServiceMonitor {

        /**
         * Its all over
         */
        public void drop() {
        }

        /**
         * Verify service can be reached. If the service cannot be reached
         * return false
         */
        public boolean verify() {
            try {
                return ((PUServiceBean) proxy).isAlive();
            } catch (RemoteException e) {
                return false;
            }
        }
    }

}
