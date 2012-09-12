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

/**
 * Specified the replication target type
 * @author eitany
 * @since 8.0.3
 */
public enum ReplicationTargetType {
    /**
     * The target is a space instance
     */
    SPACE_INSTANCE,
    /**
     * The target is a mirror service
     */
    MIRROR_SERVICE,
    /**
     * The target is a gateway
     */
    GATEWAY,
    /**
     * The target is a local view
     */
    LOCAL_VIEW,
    /**
     * The target is a registered durable notification
     */
    DURABLE_NOTIFICATION

}
