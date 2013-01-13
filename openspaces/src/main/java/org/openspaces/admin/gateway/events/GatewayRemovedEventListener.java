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

package org.openspaces.admin.gateway.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gateway.Gateway;

/**
 * An event listener allowing to listen for {@link org.openspaces.admin.gateway.Gateway} removal (undeployment).
 *
 * @since 9.5
 * @author evgeny
 * @see org.openspaces.admin.gateway.Gateways#getGatewayRemoved()
 */
public interface GatewayRemovedEventListener extends AdminEventListener {

    /**
     * A callback indicating that a Gateway was removed (undeployed).
     */
    void gatewayRemoved(Gateway gateway);
}