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
package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.config.AbstractElasticProcessingUnitConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigFactory;


/**
 * @author itaif
 * @since 9.0.1
 */
public class ElasticStatelessProcessingUnitConfig extends AbstractElasticProcessingUnitConfig 
    implements ProcessingUnitConfigFactory{

    @Override
    public ProcessingUnitConfig toProcessingUnitConfig(Admin admin) {
        
        ProcessingUnitConfig processingUnitConfig = super.toProcessingUnitConfig();
        
        // disallow two instances to deploy on same Container
        processingUnitConfig.setMaxInstancesPerVM(1);
        
        // allow any number of instances to deploy on same Machine
        processingUnitConfig.setMaxInstancesPerMachine(0);
        
        return processingUnitConfig;
    }
}
