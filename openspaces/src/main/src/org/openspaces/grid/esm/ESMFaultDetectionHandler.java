package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.lookup.ServiceID;

import org.jini.rio.resources.client.AbstractFaultDetectionHandler;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.sun.jini.config.Config;

public class ESMFaultDetectionHandler extends AbstractFaultDetectionHandler {

    public static final String INVOCATION_DELAY_KEY = "invocationDelay";
    /**
     * Component name, used for config and logger
     */
    private static final String COMPONENT =
        "org.openspaces.grid.esm.ESMFaultDetectionHandler";
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

            if (logger.isLoggable(Level.CONFIG)) {
                StringBuilder buffer = new StringBuilder("ESMFaultDetectionHandler Properties : ");
                buffer.append("\n invocation delay=" + invocationDelay);
                buffer.append("\n retry count=" + retryCount + ", ");
                buffer.append("\n retry timeout=" + retryTimeout);
                buffer.append("\n configArgs: " + Arrays.toString( configArgs));
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

        Throwable lastThrown;
        ServiceDetails serviceDetails = new ServiceDetails();

        /**
         * printable service details
         */
        class ServiceDetails {
            ServiceID serviceId;
            String host;
            long processId = -1;
            int agentId = -1;

            @Override
            public String toString() {
                String toString;

                if (agentId != -1) {
                    toString = "ESM ["+agentId+"]";
                }else {
                    toString = "ESM";
                }

                if (processId != -1) {
                    toString += " pid[" + processId + "]";
                }

                toString += " host["+host+"]";

                if (logger.isLoggable(Level.FINE)) {
                    toString += " id[" + serviceId + "]";
                }

                return toString;
            }
        }

        public ServiceAdminManager() {
            serviceDetails.serviceId = getServiceID();
            try{
                NIODetails nioDetails = ((ESM) proxy).getNIODetails();
                JVMDetails jvmDetails = ((ESM) proxy).getJVMDetails();
                serviceDetails.host = nioDetails.getHostName() + "/" + nioDetails.getHostAddress();
                serviceDetails.processId = jvmDetails.getPid();
                serviceDetails.agentId = ((ESM)proxy).getAgentId();
            }catch(RemoteException re) {
                serviceDetails.host = proxy.toString();
            }
        }

        /**
         * Its all over
         */
        public void drop() {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Dropping monitor for service: " + serviceDetails );
            }
        }

        public void reportFirstError() {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Suspecting failure of service: " + serviceDetails + ". Retrying to reach service.", lastThrown);
            }
        }

        public void reportLastError() {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Detected failure of service: " + serviceDetails + ". This service cannot be reached.", lastThrown);
            }
        }

        /**
         * Verify service can be reached. If the service cannot be reached
         * return false
         */
        public boolean verify() {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Requesting ping() on service: " + serviceDetails );
                }

                ((ESM) proxy).ping();

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("ping() successfully verified on service: " + serviceDetails);
                }

                return true;
            } catch (Exception e) {
                if(logger.isLoggable(Level.FINER)) {
                    logger.log( Level.FINER, "Failed reaching service: " + serviceDetails + ", Reason: " + e, e);
                }
                lastThrown = e;
                return false;
            }
        }

        @Override
        public String toString() {
            return super.toString() + " " + serviceDetails;
        }
    }
}
