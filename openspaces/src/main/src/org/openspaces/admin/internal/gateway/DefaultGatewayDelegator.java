package org.openspaces.admin.internal.gateway;

import org.openspaces.admin.gateway.GatewayDelegator;
import org.openspaces.admin.gateway.GatewayDelegatorTarget;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.core.gateway.GatewayDelegation;
import org.openspaces.core.gateway.GatewayDelegatorServiceDetails;

import com.gigaspaces.internal.utils.StringUtils;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGatewayDelegator implements GatewayDelegator {

    private final DefaultGatewayProcessingUnit gatewayProcessingUnit;
    private final GatewayDelegatorServiceDetails serviceDetails;

    public DefaultGatewayDelegator(DefaultGatewayProcessingUnit gatewayProcessingUnit,
            GatewayDelegatorServiceDetails serviceDetails) {
                this.gatewayProcessingUnit = gatewayProcessingUnit;
                this.serviceDetails = serviceDetails;
    }

    @Override
    public GatewayProcessingUnit getGatewayProcessingUnit() {
        return gatewayProcessingUnit;
    }

    @Override
    public GatewayDelegatorTarget[] getDelegationTargets() {
        GatewayDelegation[] delegationTargets = serviceDetails.getDelegationTargets();
        DefaultGatewayDelegatorTarget[] result = new DefaultGatewayDelegatorTarget[delegationTargets.length];
        for (int i = 0; i < result.length; i++) {
            GatewayDelegation gatewayDelegation = delegationTargets[i];
            result[i] = new DefaultGatewayDelegatorTarget(gatewayDelegation.getTarget(), gatewayDelegation.getDelegateThrough());
        }
        return result;
    }
    
    @Override
    public boolean containsTarget(String targetGatewayName) {
        GatewayDelegation[] delegationTargets = serviceDetails.getDelegationTargets();
        for (GatewayDelegation gatewayDelegation : delegationTargets) {
            if (gatewayDelegation.getTarget().equals(targetGatewayName))
                return true;
        }
        return false;
    }
    
    public class DefaultGatewayDelegatorTarget implements GatewayDelegatorTarget {

        private final String target;
        private final String delegateThrough;

        public DefaultGatewayDelegatorTarget(String target, String delegateThrough) {
            this.target = target;
            this.delegateThrough = delegateThrough;
        }

        @Override
        public GatewayDelegator getDelegator() {
            return DefaultGatewayDelegator.this;
        }

        @Override
        public String getTargetGatewayName() {
            return target;
        }

        @Override
        public boolean isDelegateThroughOtherGateway() {
            return StringUtils.hasText(getDelegateThroughGatewayName());
        }

        @Override
        public String getDelegateThroughGatewayName() {
            return delegateThrough;
        }

    }

}
