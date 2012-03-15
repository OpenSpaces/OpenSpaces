/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
