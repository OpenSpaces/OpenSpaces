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
package org.openspaces.admin.alert.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * A provision failure alert triggered when a processing unit has less actual instances than planned
 * instances. The alert is resolved when the processing unit actual instance count is equal to the
 * planned instance count.
 * <p>
 * This alert will be received on the call to
 * {@link AlertTriggeredEventListener#alertTriggered(Alert)} for registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0.3
 */
public class ProvisionFailureAlert extends AbstractAlert {
    
    private static final long serialVersionUID = -9088423967507099343L;
    
    /** required by java.io.Externalizable */
    public ProvisionFailureAlert() {
    }
    
    public ProvisionFailureAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link ProcessingUnit#getName()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
}
