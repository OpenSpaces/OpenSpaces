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
package org.openspaces.admin.internal;

import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;

public class InternalAdminFactory extends AdminFactory {

    /**
     * Enables a single event loop threading model in which all
     * event listeners and admin state updates are done on the same thread.
     * The underlying assumption is that event listeners do not perform an I/O operation
     * so they won't block the single event thread.
     * @return this (fluent API)
     */
    public AdminFactory singleThreadedEventListeners() {
        ((InternalAdmin)super.getAdmin()).singleThreadedEventListeners();
        return this;
    }

}
