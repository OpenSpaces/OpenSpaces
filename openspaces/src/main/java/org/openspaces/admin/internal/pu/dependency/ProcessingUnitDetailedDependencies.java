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
package org.openspaces.admin.internal.pu.dependency;

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

/**
 * Defines dependency between one processing unit and multiple required processing units.
 * 
 * @author itaif
 * @since 8.0.6
 */
public interface ProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency> {

    /**
     * @return true if there are no dependencies on other processing units
     * @since 8.0.6
     */
    boolean isEmpty();

    /**
     * @return The different dependencies comprising this object.   
     * @since 8.0.6
     */
    String[] getRequiredProcessingUnitsNames();
    
    /**
     * @return The processing unit dependency on the specified required processing unit, or null if such dependency does not exist.
     * @since 8.0.6
     */
    T getDependencyByName(String requiredProcessingUnitName);
}
