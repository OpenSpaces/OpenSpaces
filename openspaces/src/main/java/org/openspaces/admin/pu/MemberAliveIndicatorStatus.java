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
package org.openspaces.admin.pu;

/**
 * Member alive indicator status is communicated by the Grid Service Manager when monitoring managed processing unit instances.
 * The indicator status transitions are:
 * <ul>
 * <li><tt>ALIVE</tt> --> <tt>SUSPECTING_FAILURE</tt> : instance has failed to reply on monitoring attempt</li>
 * <li><tt>SUSPECTING_FAILURE</tt> --> <tt>ALIVE</tt> : instance has yet to reply on monitoring <b>retry</b> attempt</li>
 * <li><tt>SUSPECTING_FAILURE</tt> --> <tt>DETECTED_FAILURE</tt>: instance has failed to respond for all monitoring <b>retry</b> attempts</li>
 * </ul>
 * 
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener
 * 
 * @since 8.0.6
 * @author moran
 */
public enum MemberAliveIndicatorStatus {

    /** processing unit instance has replied to monitoring attempts */
    ALIVE,
    /** processing unit instance has yet to respond to monitoring retry attempts */
    SUSPECTING_FAILURE,
    /** processing unit instance has failed to respond for all monitoring retry attempts */
    DETECTED_FAILURE;   
}
