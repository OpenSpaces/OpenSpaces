package org.openspaces.admin.internal.gateway;

import java.util.Map;

import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.GatewayDelegator;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewaySink;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.zone.Zone;
import org.openspaces.core.gateway.GatewayDelegatorServiceDetails;
import org.openspaces.core.gateway.GatewayServiceDetails;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;
import org.openspaces.pu.service.ServiceDetails;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGatewayProcessingUnit implements GatewayProcessingUnit {

    private final DefaultGateway gateway;
    private final ProcessingUnitInstance processingUnitInstance;
    private final DefaultAdmin admin;

    public DefaultGatewayProcessingUnit(DefaultAdmin admin, DefaultGateway gateway, ProcessingUnitInstance processingUnitInstance) {
        this.admin = admin;
        this.gateway = gateway;
        this.processingUnitInstance = processingUnitInstance;
    }

    public ProcessingUnitInstance getProcessingUnitInstance() {
        return processingUnitInstance;
    }
    
    public String getUid() {
        return processingUnitInstance.getUid();
    }

    public boolean isDiscovered() {
        return processingUnitInstance.isDiscovered();
    }

    public DefaultAdmin getAdmin() {
        return admin;
    }

    public Machine getMachine() {
        return processingUnitInstance.getMachine();
    }

    public Transport getTransport() {
        return processingUnitInstance.getTransport();
    }

    public OperatingSystem getOperatingSystem() {
        return processingUnitInstance.getOperatingSystem();
    }

    public VirtualMachine getVirtualMachine() {
        return processingUnitInstance.getVirtualMachine();
    }

    public Map<String, Zone> getZones() {
        return processingUnitInstance.getZones();
    }

    public Gateway getGateway() {
        return gateway;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnitInstance.getProcessingUnit();
    }

    public GatewaySink getSink() {
        ServiceDetails[] servicesDetailsByServiceType = processingUnitInstance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
        for (ServiceDetails serviceDetails : servicesDetailsByServiceType) {
            if (serviceDetails instanceof GatewaySinkServiceDetails)
                return new DefaultGatewaySink(this, (GatewaySinkServiceDetails)serviceDetails);
        }
        return null;
    }

    public GatewayDelegator getDelegator() {
        ServiceDetails[] servicesDetailsByServiceType = processingUnitInstance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
        for (ServiceDetails serviceDetails : servicesDetailsByServiceType) {
            if (serviceDetails instanceof GatewayDelegatorServiceDetails)
                return new DefaultGatewayDelegator(this, (GatewayDelegatorServiceDetails)serviceDetails);
        }
        return null;
    }

}
