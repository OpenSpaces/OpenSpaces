package org.openspaces.admin.internal.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Gateways;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.core.gateway.GatewayDelegatorServiceDetails;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;
import org.openspaces.pu.service.ServiceDetails;

/**
 * 
 * @author eitany
 * @since 8.0.3
 */
public class DefaultGateways implements Gateways {

    private final InternalAdmin admin;

    public DefaultGateways(InternalAdmin admin) {
        this.admin = admin;
    }
    
    public Admin getAdmin() {
        return admin;
    }

    public Gateway[] getGateways() {
        LinkedList<Gateway> gateways = new LinkedList<Gateway>();
        ProcessingUnits processingUnits = admin.getProcessingUnits();       
        for (ProcessingUnit processingUnit : processingUnits) {
            if (processingUnit.getProcessingUnitType() == ProcessingUnitType.GATEWAY){
                DefaultGateway defaultGateway = wrapAsGateway(processingUnit);
                if (defaultGateway != null)
                    gateways.add(defaultGateway);
            }
        }
        return gateways.toArray(new Gateway[gateways.size()]);
    }

    protected DefaultGateway wrapAsGateway(ProcessingUnit processingUnit) {
        for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
            ServiceDetails[] serviceDetails = processingUnitInstance.getServicesDetailsByServiceType("gateway");
            if (serviceDetails != null && serviceDetails.length > 0){
                //This is a gateway pu
                GatewaySinkServiceDetails sinkDetails = null;
                GatewayDelegatorServiceDetails delegatorDetails = null; 
                for (ServiceDetails serviceDetail : serviceDetails) {
                    if (serviceDetail instanceof GatewaySinkServiceDetails)
                        sinkDetails = (GatewaySinkServiceDetails) serviceDetail;
                    if (serviceDetail instanceof GatewayDelegatorServiceDetails)
                        delegatorDetails = (GatewayDelegatorServiceDetails) serviceDetail;
                }
                return new DefaultGateway(processingUnitInstance, admin, sinkDetails, delegatorDetails);
            }
        }
        return null;
    }

    public Iterator<Gateway> iterator() {
        return Arrays.asList(getGateways()).iterator(); 
    }

    public int getSize() {
        return getGateways().length;
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

    public Gateway getGateway(String processingUnitName) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().getProcessingUnit(processingUnitName);        
        if (processingUnit == null || processingUnit.getProcessingUnitType() != ProcessingUnitType.GATEWAY)
            return null;
        return wrapAsGateway(processingUnit);
    }

    public Gateway waitFor(String processingUnitName) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(processingUnitName);
        if (processingUnit == null || processingUnit.getProcessingUnitType() != ProcessingUnitType.GATEWAY)
            return null;
        return wrapAsGateway(processingUnit);
    }

    public Gateway waitFor(String processingUnitName, long timeout, TimeUnit timeUnit) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(processingUnitName, timeout, timeUnit);
        if (processingUnit == null || processingUnit.getProcessingUnitType() != ProcessingUnitType.GATEWAY)
            return null;
        return wrapAsGateway(processingUnit);
    }
    
    public Map<String, Gateway> getNames() {
        
        Gateway[] gateways = getGateways();
        Map<String, Gateway> names = new HashMap<String, Gateway>();
        for (Gateway gateway : gateways) {
            names.put(gateway.getHostingProcessingUnitName(), gateway);
        }
        return names;
    }
}
