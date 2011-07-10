package org.openspaces.admin.internal.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

import com.gigaspaces.internal.utils.concurrent.ExchangeCountDownLatch;

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
        return Arrays.asList(getGatewayProcessingUnits()).iterator();
    }

    public GatewayProcessingUnit[] getGatewayProcessingUnits() {
        List<GatewayProcessingUnit> result = new LinkedList<GatewayProcessingUnit>();
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            ProcessingUnitInstance puInstance = GatewayUtils.extractInstanceIfPuOfGateway(gatewayName, processingUnit);
            if (puInstance != null)
                result.add(new DefaultGatewayProcessingUnit(admin, this, puInstance));
        } 
        return result.toArray(new GatewayProcessingUnit[result.size()]);
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
        Map<String, GatewayProcessingUnit> names = new HashMap<String, GatewayProcessingUnit>();
        for (GatewayProcessingUnit gatewayProcessingUnit : this) {
            names.put(gatewayProcessingUnit.getProcessingUnit().getName(), gatewayProcessingUnit);
        }
        return names;
    }

    public GatewaySink getSink(String sourceGatewayName) {
        for (GatewayProcessingUnit gatewayProcessingUnit: this) {
            GatewaySink sink = gatewayProcessingUnit.getSink();
            if (sink != null && sink.containsSource(sourceGatewayName)){
                return sink; 
            }
        }
        return null;
    }

    public GatewaySink waitForSink(String sourceGatewayName) {
        return waitForSink(sourceGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GatewaySink waitForSink(final String sourceGatewayName, long timeout, TimeUnit timeUnit) {
        final ExchangeCountDownLatch<GatewaySink> latch = new ExchangeCountDownLatch<GatewaySink>(1);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    DefaultGatewayProcessingUnit tempPUI = new DefaultGatewayProcessingUnit(admin, DefaultGateway.this, processingUnitInstance);
                    GatewaySink sink = tempPUI.getSink();
                    if (sink != null && sink.containsSource(sourceGatewayName)){
                        latch.exchange(sink);
                        latch.countDown();
                    }
                }
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return latch.get();
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public GatewaySinkSource getSinkSource(String sourceGatewayName) {
        GatewaySink sink = getSink(sourceGatewayName);
        if (sink != null)
            return sink.getSourceByName(sourceGatewayName);
        
        return null;
    }
    
    public GatewaySinkSource waitForSinkSource(String sourceGatewayName) {
        return waitForSinkSource(sourceGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GatewaySinkSource waitForSinkSource(String sourceGatewayName, long timeout, TimeUnit timeUnit) {
        GatewaySink sink = waitForSink(sourceGatewayName, timeout, timeUnit);
        if (sink != null)
            return sink.getSourceByName(sourceGatewayName);
        
        return null;
    }

    public GatewayDelegator getDelegator(String targetGatewayName) {
        for (GatewayProcessingUnit gatewayProcessingUnit: this) {
            GatewayDelegator delegator = gatewayProcessingUnit.getDelegator();
            if (delegator != null && delegator.containsTarget(targetGatewayName)){
                return delegator; 
            }
        }
        return null;
    }

    public GatewayDelegator waitForDelegator(String targetGatewayName) {
        return waitForDelegator(targetGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    public GatewayDelegator waitForDelegator(final String targetGatewayName, long timeout, TimeUnit timeUnit) {
        final ExchangeCountDownLatch<GatewayDelegator> latch = new ExchangeCountDownLatch<GatewayDelegator>(1);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    DefaultGatewayProcessingUnit tempPUI = new DefaultGatewayProcessingUnit(admin, DefaultGateway.this, processingUnitInstance);
                    GatewayDelegator delegator = tempPUI.getDelegator();
                    if (delegator != null && delegator.containsTarget(targetGatewayName)){
                        latch.exchange(delegator);
                        latch.countDown();
                    }
                }
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return latch.get();
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public int getSize() {
        return getGatewayProcessingUnits().length;
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

}
