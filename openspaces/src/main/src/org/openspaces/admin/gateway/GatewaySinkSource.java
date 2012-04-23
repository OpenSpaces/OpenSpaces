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
package org.openspaces.admin.gateway;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;

/**
 * A sink source is the target end point of a remote {@link Gateway} which is replicating into this gateway's sink. 
 * @author eitany
 * @since 8.0.4
 */
public interface GatewaySinkSource {
    
    /**
     * Returns the sink this source is part of.
     */
    GatewaySink getSink();
    
    /**
     * Returns the name of the remote gateway which this source acts as its end point.
     */
    String getSourceGatewayName();
    
    /**
     * Bootstrap the local {@link org.openspaces.admin.space.Space} which is associated to this sink from a remote space.
     * This bootstrap request will use default timeout set by {@link Admin#setDefaultTimeout(long, TimeUnit)}
     * A bootstrap request can only be executed if this sink {@link GatewaySink#requiresBootstrapOnStartup()}. 
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait();
    /**
     * Bootstrap the local {@link org.openspaces.admin.space.Space} which is associated to this sink from a remote space.
     * This bootstrap request will use the provided timeout.
     * A bootstrap request can only be executed if this sink {@link GatewaySink#requiresBootstrapOnStartup()}.
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(long timeout, TimeUnit timeUnit);
    
}
