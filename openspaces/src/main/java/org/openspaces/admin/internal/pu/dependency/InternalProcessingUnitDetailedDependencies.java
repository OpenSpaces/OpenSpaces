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

import org.jini.rio.core.RequiredDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

/*
 * @since 8.0.6
 * @author itaif
 */
    
public interface InternalProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> 
    extends ProcessingUnitDetailedDependencies<T> {
    
    /**
     * Adds the specified dependency.
     * If a with the same name exits, then the old dependency is merged with the new dependency.
     * 
     * @since 8.0.6
     */
    void addDependency(T dependency);
        
    /**
     * Converts this to GSM compatible format.
     * 
     * @since 8.0.6
     */
    RequiredDependencies toRequiredDependencies();
    
    /**
     * Merges with dependencies in GSM format
     */
    void addDependencies(RequiredDependencies requiredDependencies);

    String getCommandLineOption();
}
