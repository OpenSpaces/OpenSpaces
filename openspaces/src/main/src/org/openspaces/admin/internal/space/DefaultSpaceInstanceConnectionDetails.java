/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.internal.space;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.space.SpaceInstanceConnectionDetails;

import com.gigaspaces.management.transport.ITransportConnection;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;

/**
 * @author moran
 */
public class DefaultSpaceInstanceConnectionDetails implements SpaceInstanceConnectionDetails {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);
    private final DefaultSpaceInstance defaultSpaceInstance;

    public DefaultSpaceInstanceConnectionDetails(DefaultSpaceInstance defaultSpaceInstance) {
        this.defaultSpaceInstance = defaultSpaceInstance;
    }
    
    @Override
    public int getActiveConnectionCount() {
        int count = 0;
        IInternalRemoteJSpaceAdmin spaceAdmin = defaultSpaceInstance.getSpaceAdmin();
        if (spaceAdmin != null) {
            try {
                List<ITransportConnection> connectionsInfo = spaceAdmin.getConnectionsInfo();
                count = connectionsInfo.size();
            } catch (RemoteException e) {
                logger.debug("RemoteException caught while trying to get Space connection information from "
                        + defaultSpaceInstance.getSpaceName(), e);
            }
        }
        return count;
    }
}
