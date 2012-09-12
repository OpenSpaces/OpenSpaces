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

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.MemberAliveIndicatorStatus;

/**
 * An event listener allowing to listen for
 * {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent}s. <br>
 * The Grid Service Manager uses a fault-detection mechanism which periodically invokes an
 * <tt>isAlive()</tt> method of a processing unit instance. The member alive indicator mechanism has a
 * configurable amount of retries until a failure is considered. A failure on the first retry
 * attempt will raise an event of a suspected failure (
 * {@link MemberAliveIndicatorStatus#SUSPECTING_FAILURE}), if a retry attempt succeeds, the instance is
 * considered alive ({@link MemberAliveIndicatorStatus#ALIVE}). After all retry attempts have failed an
 * event of detected failure ({@link MemberAliveIndicatorStatus#DETECTED_FAILURE}) is raised.
 * <p>
 * <b>note:</b> The member-alive fault-detection mechanism (retries and timeout) should be configured separately
 * in the pu.xml (see os-sla:member-alive-indicator)
 * 
 * @since 8.0.6
 * @author moran
 */
public interface ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener extends AdminEventListener {

    /**
     * A callback indicating the member alive indicator status has changed.
     */
    void processingUnitInstanceMemberAliveIndicatorStatusChanged(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event);
}
