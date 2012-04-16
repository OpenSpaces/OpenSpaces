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
package org.openspaces.admin.space;

import java.util.Map;

import org.openspaces.core.GigaSpace;

/**
 * Aggregated runtime details of all the currently discovered {@link org.openspaces.admin.space.SpaceInstance}s.
 * <p>
 * These calls avoid establishing a proxy to the Space instances. In case of failover, using the API will always return
 * results from currently discovered instances.
 * 
 * @author Moran Avigdor
 * @since 8.0.3
 */
public interface SpaceRuntimeDetails {

    /**
     * An aggregated count of any null-template matching entry/object in the Space.
     * <p>
     * Count could also be gained by establishing a proxy to the Space.
     * @see SpaceInstance#getGigaSpace()
     * @see GigaSpace#count(Object)
     * @see GigaSpace#count(Object, int)
     * @return a count (gathered periodically).
     */
    int getCount();
    
    /**
     * Returns an aggregated class names array of entry/objects in all the Space instances.
     * @return an array of class names.
     */
    String[] getClassNames();
    
    /**
     * Returns an aggregated mapping between each class name and the number of entries/objects in all the Space instances.
     * @return a mapping of class name to entry/object count.
     */
    Map<String, Integer> getCountPerClassName();
    
    /**
     * Returns an aggregated mapping between each class name and the number of notify-templates in the Space instances.
     * @return a mapping of class name to template count.
     */
    Map<String, Integer> getNotifyTemplateCountPerClassName();
    
    /**
     * @return the aggregated transaction details of all the Space instances.
     * @since 9.0.0
     */
    SpaceTransactionDetails getTransactionDetails();
    
    /**
     * @return the aggregated connection details of all the Space instances.
     * @since 9.0.0
     */
    SpaceConnectionDetails getConnectionDetails();
}
