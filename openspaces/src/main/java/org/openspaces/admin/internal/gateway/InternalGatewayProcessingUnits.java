package org.openspaces.admin.internal.gateway;

import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewayProcessingUnits;

public interface InternalGatewayProcessingUnits extends GatewayProcessingUnits{

	void addGatewayProcessingUnit(GatewayProcessingUnit gatewayProcessingUnit);
	void removeGatewayProcessingUnit(String uid);
}