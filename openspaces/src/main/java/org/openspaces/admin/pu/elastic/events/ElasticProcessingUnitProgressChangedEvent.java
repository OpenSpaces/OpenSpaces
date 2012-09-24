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
package org.openspaces.admin.pu.elastic.events;


/**
 * An interface for ESM events that denote a processing unit deployment progress change
 * @since 8.0.6
 * @author itaif
 */
public interface ElasticProcessingUnitProgressChangedEvent extends ElasticProcessingUnitEvent {

    /**
     * @return true if the progress event indicates the process (described by the event class type) is complete.
     */
    public boolean isComplete();
    
    /**
     * @return true if the event indicates the processing unit is being undeployed 
     */
    public boolean isUndeploying();
}
