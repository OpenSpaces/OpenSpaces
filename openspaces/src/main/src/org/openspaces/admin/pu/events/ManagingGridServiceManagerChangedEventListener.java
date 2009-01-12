/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener allowing to register for {@link org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager
 * @see org.openspaces.admin.pu.ProcessingUnit#getManagingGridServiceManagerChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getManagingGridServiceManagerChanged()
 */
public interface ManagingGridServiceManagerChangedEventListener extends AdminEventListener {

    /**
     * Callback indicating that a managing GSM for a processing unit was changed.
     */
    void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event);
}
