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
package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEventManager;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;

public class DefaultElasticMachineProvisioningProgressChangedEventManager 
    extends AbstractElasticProcessingUnitProgressChangedEventManager<ElasticMachineProvisioningProgressChangedEvent, ElasticMachineProvisioningProgressChangedEventListener>
    implements InternalElasticMachineProvisioningProgressChangedEventManager {

    public DefaultElasticMachineProvisioningProgressChangedEventManager(InternalAdmin admin) {
        super(admin);
    }

    @Override
    public void elasticMachineProvisioningProgressChanged(final ElasticMachineProvisioningProgressChangedEvent event) {
        super.pushEventToAllListeners(event);
    }
    
    @Override
    protected void fireEventToListener(ElasticMachineProvisioningProgressChangedEvent event, ElasticMachineProvisioningProgressChangedEventListener listener) {
        listener.elasticMachineProvisioningProgressChanged(event);
    }

}
