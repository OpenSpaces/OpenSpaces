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
package org.openspaces.admin.internal.space;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceRuntimeDetails;

/**
 * @author kimchy
 */
public interface InternalSpace extends Space, InternalSpaceInstancesAware {

    void addInstance(SpaceInstance spaceInstance);

    InternalSpaceInstance removeInstance(String uid);

    void refreshScheduledSpaceMonitors();

    Admin getAdmin();
    
    SpaceRuntimeDetails waitForRuntimeDetails(long timeout, TimeUnit timeUnit);
}
