package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioningException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class NoClassDefFoundElasticMachineProvisioningException extends ElasticMachineProvisioningException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    private static final String MISSING_JARS_ERROR_MESSAGE = "Some JARs are missing in the classpath. Place them in the lib/platform/esm folder: ";

    public NoClassDefFoundElasticMachineProvisioningException(ProcessingUnit pu, NoClassDefFoundError cause) {
        super(MISSING_JARS_ERROR_MESSAGE + cause.getMessage(), cause);
        this.affectedProcessingUnits = new String[] { pu.getName() };
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return this.affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof NoClassDefFoundElasticMachineProvisioningException) {
            NoClassDefFoundElasticMachineProvisioningException otherEx = (NoClassDefFoundElasticMachineProvisioningException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits, this.affectedProcessingUnits) &&
                   otherEx.getCause().getMessage().equals(this.getCause().getMessage());
        }
        return same;  
    }
}
