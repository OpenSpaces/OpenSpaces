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
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.internal.jvm.JVMDetails;

import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.lookup.ServiceID;

import org.jini.rio.resources.client.AbstractFaultDetectionHandler;

import java.rmi.RemoteException;
import java.util.Arrays;
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
    private static final String COMPONENT = "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler";
    /**
     * A Logger
     */
    private static final Logger logger = Logger.getLogger(COMPONENT);

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

            invocationDelay = Config.getLongEntry(config, COMPONENT, INVOCATION_DELAY_KEY, DEFAULT_INVOCATION_DELAY, 0,
                    Long.MAX_VALUE);
            retryCount = Config.getIntEntry(config, COMPONENT, RETRY_COUNT_KEY, DEFAULT_RETRY_COUNT, 0,
                    Integer.MAX_VALUE);
            retryTimeout = Config.getLongEntry(config, COMPONENT, RETRY_TIMEOUT_KEY, DEFAULT_RETRY_TIMEOUT, 0,
                    Long.MAX_VALUE);

            if (logger.isLoggable(Level.CONFIG)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("PUFaultDetectionHandler Properties : ");
                buffer.append("\n invocation delay=" + invocationDelay);
                buffer.append("\n retry count=" + retryCount + ", ");
                buffer.append("\n retry timeout=" + retryTimeout);
                buffer.append("\n configArgs: " + Arrays.toString(configArgs));
                logger.config(buffer.toString());
            }
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Setting Configuration", e);
        }
    }

    /**
     * Get the class which implements the ServiceMonitor
     */
    @Override
    protected ServiceMonitor getServiceMonitor() throws Exception {
        return new ServiceAdminManager();
    }

    class ServiceAdminManager implements ServiceMonitor {

        volatile int retriesCount = 0;
        volatile Throwable lastThrown;
        volatile long roundtrip;

        final ServiceDetails serviceDetails = new ServiceDetails();

        /**
         * printable service details
         */
        class ServiceDetails {
            ServiceID serviceId;
            String presentationName;
            String host;
            long processId;

            @Override
            public String toString() {
                String toString = "[" + presentationName + "] pid[" + processId + "] host[" + host + "]";
                if (logger.isLoggable(Level.FINE)) {
                    toString += " Id[" + serviceId + "]";
                }
                return toString;
            }
        }

        public ServiceAdminManager() {
            serviceDetails.serviceId = getServiceID();
            try {
                NIODetails nioDetails = ((PUServiceBean) proxy).getNIODetails();
                JVMDetails jvmDetails = ((PUServiceBean) proxy).getJVMDetails();
                serviceDetails.host = nioDetails.getHostName() + "/" + nioDetails.getHostAddress();
                serviceDetails.processId = jvmDetails.getPid();
            } catch (Exception re) {
                // ignore
            }
            try {
                serviceDetails.presentationName = ((PUServiceBean) proxy).getPresentationName();
            } catch (Exception re) {
                serviceDetails.presentationName = proxy.toString();
            }
        }

        /**
         * Its all over
         */
        public void drop() {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Dropping monitor for service: " + serviceDetails);
            }
        }

        public void reportFirstError() {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Suspecting failure of service: " + serviceDetails + " - RTT[" + formatDuration(roundtrip) + "]", lastThrown);
            }
        }

        public void reportLastError() {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Detected failure of service: " + serviceDetails, lastThrown);
            }
        }

        /**
         * Verify service can be reached. If the service cannot be reached return false
         */
        public boolean verify() {
            long start = System.nanoTime();
            try {
                final boolean isAlive = ((PUServiceBean) proxy).isAlive();

                if (isAlive) {
                    retriesCount = 0;

                    if (logger.isLoggable(Level.FINEST)) {
                        long roundtrip = System.nanoTime() - start;
                        logger.finest("Successfully verified service: " + serviceDetails + " is alive - RTT[" + formatDuration(roundtrip) + "]");
                    }
                } else {
                    throw new MemberReturnFalseException();
                }
                return isAlive;
            } catch (Exception e) {
                lastThrown = e;
                roundtrip = System.nanoTime() - start;
                int retry = retriesCount++;

                if (logger.isLoggable(Level.FINER)) {
                    if (e instanceof MemberReturnFalseException) {
                        logger.log(Level.FINER, "Service Failure (isAlive:false): " + serviceDetails + " - RTT[" + formatDuration(roundtrip) + "], retry[" + retry+"]");
                    } else {
                        logger.log(Level.FINER, "Service Failure: " + serviceDetails + " - RTT[" + formatDuration(roundtrip) + "], retry[" + retry+"]", e);
                    }
                }

                return false;
            }
        }

        /**
         * format the duration (in nanos) to milliseconds or seconds - which ever is more
         * representable.
         * 
         * @param nanos
         *            duration in nanoseconds.
         * @return a string representing the duration + units
         */
        private final String formatDuration(long nanos) {
            String unit = " ms";
            double value = (1.0 / 1000000) * (nanos); // to millis
            if (value > 1000) {
                unit = " sec";
                value = (1.0 / 1000) * value; // to seconds
            }
            // trim decimals fraction digits to 2
            String svalue = String.valueOf(value);
            int dot = svalue.indexOf('.');
            if (dot != -1 && (dot + 2 < svalue.length())) {
                svalue = svalue.substring(0, dot + 2);
            }
            return svalue + unit;
        }

        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return super.toString() + " " + serviceDetails;
        }
    }

    private static class MemberReturnFalseException extends Exception {

    }
}
