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
 * API for accessing Space runtime details - classes, templates, count, etc.
 * <p>
 * These calls avoid establishing a proxy to the Space, but is equivalent to
 * <code>
 * ...
 * GigaSpace gigaspace = spaceInstance.getGigaSpace();
 * IJSpace spaceProxy = gigaspace.getSpace();
 * IRemoteJSpaceAdmin spaceAdmin = spaceProxy.getAdmin();
 * SpaceRuntimeInfo info = spaceAdmin.getRuntimeInfo(); 
 * </code>
 * @author Moran Avigdor
 * @since 8.0.3
 */
public interface SpaceInstanceRuntimeDetails {

    /**
     * A count of any null-template matching entry/object in the Space.
     * <p>
     * Count could also be gained by establishing a proxy to the Space.
     * @see SpaceInstance#getGigaSpace()
     * @see GigaSpace#count(Object)
     * @see GigaSpace#count(Object, int)
     * @return a count (gathered periodically).
     * 
     * @deprecated @see SpaceInstanceStatistics#getObjectCount()
     */
    @Deprecated
    int getCount();
    
    /**
     * Returns an array of class names of entry/objects in the Space.
     * @return an array of class names.
     */
    String[] getClassNames();
    
    /**
     * Returns a mapping between each class name and the number of entries/objects in the Space.
     * @return a mapping of class name to entry/object count.
     */
    Map<String, Integer> getCountPerClassName();
    
    /**
     * Returns a mapping between each class name and the number of notify-templates in the Space.
     * @return a mapping of class name to template count.
     */
    Map<String, Integer> getNotifyTemplateCountPerClassName();
    
    /**
     * @return the transaction details of this Space instance.
     * @since 9.0.0
     * @deprecated use {@link SpaceInstanceStatistics#getActiveTransactionCount()}
     */
    @Deprecated
    SpaceInstanceTransactionDetails getTransactionDetails();
    
    /**
     * @return the connection details of this Space instance.
     * @since 9.0.0
     * @deprecated use {@link SpaceInstanceStatistics#getActiveConnectionCount()}
     */
    @Deprecated
    SpaceInstanceConnectionDetails getConnectionDetails();
}
