package org.openspaces.grid.gsm.machines;

import org.openspaces.core.bean.Bean;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;

public interface CloudMachinesSlaEnforcementBean 
    extends ServiceLevelAgreementEnforcement<MachinesSlaPolicy,String,MachinesSlaEnforcementEndpoint>, Bean {
}
