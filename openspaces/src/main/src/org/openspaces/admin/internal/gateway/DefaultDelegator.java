package org.openspaces.admin.internal.gateway;

import org.openspaces.admin.gateway.Delegator;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.IDelegation;
import org.openspaces.core.gateway.GatewayDelegatorServiceDetails;

public class DefaultDelegator implements Delegator {

    private final DefaultGateway defaultGateway;
    private final GatewayDelegatorServiceDetails delegatorDetails;

    public DefaultDelegator(DefaultGateway defaultGateway, GatewayDelegatorServiceDetails delegatorDetails) {
        this.defaultGateway = defaultGateway;
        this.delegatorDetails = delegatorDetails;
    }

    public Gateway getGateway() {
        return defaultGateway;
    }

    public IDelegation[] getDelegationTargets() {
        return delegatorDetails.getDelegationTargets();
    }

}
