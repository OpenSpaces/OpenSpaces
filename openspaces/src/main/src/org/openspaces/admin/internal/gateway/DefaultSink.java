package org.openspaces.admin.internal.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gateway.BootstrapResult;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Sink;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;

public class DefaultSink implements Sink {

    private final GatewaySinkServiceDetails sinkDetails;
    private final ProcessingUnitInstance processingUnitInstance;
    private final InternalAdmin admin;
    private final DefaultGateway gateway;

    public DefaultSink(DefaultGateway gateway, GatewaySinkServiceDetails sinkDetails, InternalAdmin admin) {
        this.gateway = gateway;
        this.sinkDetails = sinkDetails;
        this.admin = admin;
        this.processingUnitInstance = gateway.getProcessingUnitInstance();
    }

    public Gateway getGateway() {
        return gateway;
    }
    
    public BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName) {
        return bootstrapFromGatewayAndWait(bootstrapSourceGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName, long timeout,
            TimeUnit timeUnit) {
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("bootstrapFromGateway", bootstrapSourceGatewayName);
        namedArgs.put("bootstrapTimeout", timeUnit.toSeconds(timeout));
        Future<Object> future = ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
        try {
            Object result = future.get(timeout, timeUnit);
            return new DefaultBootstrapResult();
        } catch (InterruptedException e) {
            return DefaultBootstrapResult.getFailedResult(e);
        } catch (ExecutionException e) {
            return DefaultBootstrapResult.getFailedResult(e.getCause());
        } catch (TimeoutException e) {
            return DefaultBootstrapResult.getFailedResult(e);
        }
    }
    
    public void enableIncomingReplication() {
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("enableIncomingReplication", "");
        Future<Object> future = ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
        try {
            future.get();
        } catch (InterruptedException e) {
            return;
        } catch (ExecutionException e) {
            //TODO WAN: return some failure?
            return;
        }
    }
    
    public String[] getSourceGatewayNames() {
        return sinkDetails.getGatewaySourceNames();
    }
    
    public boolean requiresBootstrapOnStartup() {
        return sinkDetails.requiresBootstrap();
    }
    
    public String getLocalSpaceUrl() {
        return sinkDetails.getLocalSpaceUrl();
    }

}
