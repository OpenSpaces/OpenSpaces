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

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.pu.ProcessingUnit;

public class SomeProcessingUnitsHaveNotCompletedStateRecoveryException extends
        GridServiceAgentSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public SomeProcessingUnitsHaveNotCompletedStateRecoveryException(List<ProcessingUnit> pusNotCompletedStateRecovery) {
        super(pusToArray(pusNotCompletedStateRecovery), "Waiting for the following processing units to complete state recovery: " + 
                  pusToString(pusNotCompletedStateRecovery));
    }

    private static String[] pusToArray(List<ProcessingUnit> pusNotCompletedStateRecovery) {
        return pusToList(pusNotCompletedStateRecovery).toArray(new String[pusNotCompletedStateRecovery.size()]);
    }

    private static String pusToString(List<ProcessingUnit> pus) {
        final List<String> puNames = pusToList(pus);
        return puNames.toString();
    }

    private static List<String> pusToList(List<ProcessingUnit> pus) {
        final List<String> puNames = new ArrayList<String>(pus.size());
        for (final ProcessingUnit pu : pus) {
            puNames.add(pu.getName());
        }
        return puNames;
    }
}
