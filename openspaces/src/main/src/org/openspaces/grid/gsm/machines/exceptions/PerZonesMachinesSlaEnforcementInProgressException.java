/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.grid.gsm.machines.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class PerZonesMachinesSlaEnforcementInProgressException extends MachinesSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    Map<Set<String>,MachinesSlaEnforcementInProgressException> reasons;
    public PerZonesMachinesSlaEnforcementInProgressException(String[] puNames) {
        super(puNames, "Multiple Exceptions");
        reasons = new HashMap<Set<String>, MachinesSlaEnforcementInProgressException>();
    }

    public void addReason(Set<String> zones, MachinesSlaEnforcementInProgressException reason) {
        reasons.put(zones, reason);
    }

    @Override
    public String getMessage() {
        return reasons.toString();
    }

    public boolean hasReason() {
        return !reasons.isEmpty();
    }
}
