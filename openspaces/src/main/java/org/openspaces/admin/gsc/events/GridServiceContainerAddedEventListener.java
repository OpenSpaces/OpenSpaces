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

package org.openspaces.admin.gsc.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;

/**
 * An event listener to be notified when a grid service container is added.
 *
 * @author kimchy
 * @see org.openspaces.admin.gsc.GridServiceContainers#getGridServiceContainerAdded()
 */
public interface GridServiceContainerAddedEventListener extends AdminEventListener {

    /**
     * Called when a grid service container is added.
     */
    void gridServiceContainerAdded(GridServiceContainer gridServiceContainer);
}