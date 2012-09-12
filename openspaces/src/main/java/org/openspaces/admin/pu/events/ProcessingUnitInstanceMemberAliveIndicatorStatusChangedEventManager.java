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
package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove
 * {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener}s, in order to listen to
 * {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent}s.
 * 
 * @since 8.0.6
 * @author moran
 */
public interface ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager {

    /**
     * Adds an event listener. Note, the add callback will be called for currently discovered
     * processing unit instances as well with the current member alive indicator status.
     */
    void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener);
    
    /**
     * Adds an event listener. Allows to control if the event will be called with the current status for existing processing
     * unit instances as well.
     */
    void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener, boolean includeCurrentStatus);
    
    /**
     * Removes an event listener.
     */
    void remove(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener);
}
