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
package org.openspaces.admin.internal.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEvent;

/**
 * An event carrying progress information about the auto scaling process of the elastic pu. 
 * @author itaif
 * @since 9.0.0
 */
public class DefaultElasticAutoScalingProgressChangedEvent 
        extends AbstractElasticProcessingUnitProgressChangedEvent 
        implements ElasticAutoScalingProgressChangedEvent {

   private static final long serialVersionUID = 1L;
   
   /**
    * de-serialization constructor
    */
   public DefaultElasticAutoScalingProgressChangedEvent() {
   }

   @Override
   public String toString() {
       return toStringHelper("automatic scaling rules");
   }
}
