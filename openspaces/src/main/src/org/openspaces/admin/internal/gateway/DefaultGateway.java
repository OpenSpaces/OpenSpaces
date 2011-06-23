package org.openspaces.admin.internal.gateway;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Sink;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.zone.Zone;
import org.openspaces.core.gateway.GateDelegatorServiceDetails;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;

public class DefaultGateway implements Gateway {

    private final ProcessingUnitInstance processingUnitInstance;
    private final InternalAdmin admin;
    private final GatewaySinkServiceDetails sinkDetails;
    private final GateDelegatorServiceDetails delegatorDetails;

    public DefaultGateway(ProcessingUnitInstance processingUnitInstance, InternalAdmin admin, GatewaySinkServiceDetails sinkDetails, GateDelegatorServiceDetails delegatorDetails) {
        this.processingUnitInstance = processingUnitInstance;
        this.admin = admin;
        this.sinkDetails = sinkDetails;
        this.delegatorDetails = delegatorDetails;
    }

    public Sink getSink() {
        if (sinkDetails == null)
            return null;
        
        return new DefaultSink(this, sinkDetails, admin);
    }
    
    public ProcessingUnitInstance getProcessingUnitInstance() {
        return processingUnitInstance;
    }

    public String getName() {
        if (sinkDetails != null)
            return sinkDetails.getLocalGatewayName();
        return delegatorDetails.getLocalGatewayName();
    }
    
    public String getHostingProcessingUnitName() {
        return processingUnitInstance.getName();
    }

    public String getUid() {
        return processingUnitInstance.getUid();
    }

    public boolean isDiscovered() {
        return processingUnitInstance.isDiscovered();
    }

    public Admin getAdmin() {
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
    
    public ProcessingUnit getHostingProcessingUnit() {
        return processingUnitInstance.getProcessingUnit();
    }

}
