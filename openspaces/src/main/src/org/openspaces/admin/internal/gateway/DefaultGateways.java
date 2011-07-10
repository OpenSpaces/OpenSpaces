package org.openspaces.admin.internal.gateway;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Gateways;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGateways implements Gateways {

    private final DefaultAdmin admin;

    public DefaultGateways(DefaultAdmin admin) {
        this.admin = admin;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Iterator<Gateway> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public Gateway[] getGateways() {
        // TODO Auto-generated method stub
        return null;
    }

    public Gateway getGateway(String gatewayName) {
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
              if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance))
                  return new DefaultGateway(admin, gatewayName);
            } 
        }
        return null;
    }

    public Map<String, Gateway> getNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public Gateway waitFor(String gatewayName) {
        return waitFor(gatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public Gateway waitFor(final String gatewayName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance))
                    latch.countDown();
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return new DefaultGateway(admin, gatewayName);
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
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
