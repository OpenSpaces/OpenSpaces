package org.openspaces.admin.internal.gateway;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.GatewayDelegator;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewaySink;
import org.openspaces.admin.gateway.GatewaySinkSource;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.core.gateway.GatewayUtils;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGateway implements Gateway {

    private final String gatewayName;
    private final DefaultAdmin admin;

    public DefaultGateway(DefaultAdmin admin, String gatewayName) {
        this.admin = admin;
        this.gatewayName = gatewayName;
    }

    public Iterator<GatewayProcessingUnit> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewayProcessingUnit[] getGatewayProcessingUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return gatewayName;
    }

    public boolean waitFor(int numberOfGatewayProcessingUnits) {
        return waitFor(numberOfGatewayProcessingUnits, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfGatewayProcessingUnits, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfGatewayProcessingUnits);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            private final Set<String> gatewayProcessingUnitNames = new HashSet<String>();
            
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    if (gatewayProcessingUnitNames.add(processingUnitInstance.getProcessingUnit().getName())){
                        latch.countDown();
                    }
                }
            }
        };
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName) {
        return waitForGatewayProcessingUnit(processingUnitName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName, long timeout, TimeUnit timeUnit) {
        //TODO WAN: calculate new timeout
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(processingUnitName, timeout, timeUnit);
        if (processingUnit.waitFor(1, timeout, timeUnit)){
            if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnit.getInstances()[0]))
                return new DefaultGatewayProcessingUnit(admin, this, processingUnit.getInstances()[0]);
            throw new IllegalArgumentException("requested processing unit is not part of this gateway [" + processingUnitName + "]");
        }
        return null;
    }

    public GatewayProcessingUnit getGatewayProcessingUnit(String processingUnitName) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().getProcessingUnit(processingUnitName);
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances == null || instances.length == 0)
            return null;
        
        if (GatewayUtils.isPuInstanceOfGateway(gatewayName, instances[0]))
            return new DefaultGatewayProcessingUnit(admin, this, instances[0]);
        throw new IllegalArgumentException("requested processing unit is not part of this gateway [" + processingUnitName + "]");
    }

    public Map<String, GatewayProcessingUnit> getNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewaySink getSink(String sourceGatewayName) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewaySink waitForSink(String sourceGatewayName) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewaySink waitForSink(String sourceGatewayName, long timeout, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewaySinkSource waitForSinkSource(String sourceGatewayName) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewaySinkSource waitForSinkSource(String sourceGatewayName, long timeout, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewayDelegator getDelegator(String targetGatewayName) {
        // TODO Auto-generated method stub
        return null;
    }

    public GatewayDelegator waitForDelegator(String targetGatewayName, long timeout, TimeUnit unit) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

}
