package org.openspaces.admin.internal.gateway;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.core.gateway.GatewayServiceDetails;
import org.openspaces.pu.service.ServiceDetails;

public class GatewayUtils {
    public static boolean isPuInstanceOfGateway(final String gatewayName, ProcessingUnitInstance processingUnitInstance) {
        if (processingUnitInstance.getProcessingUnit().getType() == ProcessingUnitType.GATEWAY){
            ServiceDetails[] serviceDetails = processingUnitInstance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
            if (serviceDetails != null && serviceDetails.length > 0){
                if (((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName().equals(gatewayName)){
                    return true;
                }
            }
        }
        return false;
    }
}
