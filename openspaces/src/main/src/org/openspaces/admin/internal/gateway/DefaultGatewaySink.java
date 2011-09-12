package org.openspaces.admin.internal.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gateway.BootstrapResult;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewaySink;
import org.openspaces.admin.gateway.GatewaySinkSource;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGatewaySink implements GatewaySink {

    private final DefaultGatewayProcessingUnit gatewayProcessingUnit;
    private final GatewaySinkServiceDetails sinkServiceDetails;

    public DefaultGatewaySink(DefaultGatewayProcessingUnit gatewayProcessingUnit,
            GatewaySinkServiceDetails sinkServiceDetails) {
                this.gatewayProcessingUnit = gatewayProcessingUnit;
                this.sinkServiceDetails = sinkServiceDetails;
    }

    @Override
    public GatewayProcessingUnit getGatewayProcessingUnit() {
        return gatewayProcessingUnit;
    }

    @Override
    public void enableIncomingReplication() {
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("enableIncomingReplication", "");
        ((InternalProcessingUnitInstance)gatewayProcessingUnit.getProcessingUnitInstance()).invoke("sink", namedArgs);
    }

    @Override
    public GatewaySinkSource[] getSources() {
        String[] gatewaySourceNames = sinkServiceDetails.getGatewaySourceNames();
        GatewaySinkSource[] sources = new GatewaySinkSource[gatewaySourceNames.length];
        for (int i = 0; i < sources.length; i++) {
            sources[i] = new DefaultGatewaySinkSource(gatewaySourceNames[i]);
        }
        return sources;
    }

    @Override
    public boolean containsSource(String sourceGatewayName) {
        String[] gatewaySourceNames = sinkServiceDetails.getGatewaySourceNames();
        return Arrays.asList(gatewaySourceNames).contains(sourceGatewayName);
    }

    @Override
    public GatewaySinkSource getSourceByName(String sourceGatewayName) {
        if (!containsSource(sourceGatewayName))
            return null;
        
        return new DefaultGatewaySinkSource(sourceGatewayName);
    }

    @Override
    public boolean requiresBootstrapOnStartup() {
        return sinkServiceDetails.requiresBootstrap();
    }

    @Override
    public String getLocalSpaceUrl() {
        return sinkServiceDetails.getLocalSpaceUrl();
    }

    public class DefaultGatewaySinkSource implements GatewaySinkSource {

        private final String sourceGatewayName;

        public DefaultGatewaySinkSource(String sourceGatewayName) {
            this.sourceGatewayName = sourceGatewayName;
        }

        @Override
        public GatewaySink getSink() {
            return DefaultGatewaySink.this;
        }

        @Override
        public String getSourceGatewayName() {
            return sourceGatewayName;
        }

        @Override
        public BootstrapResult bootstrapFromGatewayAndWait() {
            return bootstrapFromGatewayAndWait(gatewayProcessingUnit.getAdmin().getDefaultTimeout(), gatewayProcessingUnit.getAdmin().getDefaultTimeoutTimeUnit());
        }

        @Override
        public BootstrapResult bootstrapFromGatewayAndWait(long timeout, TimeUnit timeUnit) {
            Map<String, Object> namedArgs = new HashMap<String, Object>();
            namedArgs.put("bootstrapFromGateway", sourceGatewayName);
            namedArgs.put("bootstrapTimeout", timeUnit.toMillis(timeout));
            Future<Object> future = ((InternalProcessingUnitInstance)gatewayProcessingUnit.getProcessingUnitInstance()).invoke("sink", namedArgs);
            try {
                future.get(timeout, timeUnit);
                return new BootstrapResult() {
                    
                    @Override
                    public boolean isSucceeded() {
                        return true;
                    }
                    
                    @Override
                    public Throwable getFailureCause() {
                        return null;
                    }
                };
            } catch (final InterruptedException e) {
                return new BootstrapResult() {
                    
                    @Override
                    public boolean isSucceeded() {
                        return false;
                    }
                    
                    @Override
                    public Throwable getFailureCause() {
                        return e;
                    }
                };
            } catch (final ExecutionException e) {
                return new BootstrapResult() {
                    
                    @Override
                    public boolean isSucceeded() {
                        return false;
                    }
                    
                    @Override
                    public Throwable getFailureCause() {
                        return e.getCause();
                    }
                };
            } catch (final TimeoutException e) {
                return new BootstrapResult() {
                    
                    @Override
                    public boolean isSucceeded() {
                        return false;
                    }
                    
                    @Override
                    public Throwable getFailureCause() {
                        return e;
                    }
                };
            }
        }

    }
    
}
