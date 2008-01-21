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

package org.openspaces.esb.mule;

import com.j_spaces.core.client.UpdateModifiers;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import org.mule.MuleRuntimeException;
import org.mule.config.i18n.CoreMessages;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.openspaces.core.GigaSpace;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;

import java.util.Arrays;
import java.util.Properties;

/**
 * <code>OpenSpacesMessageDispatcher</code> is responsible for sending messages to GigaSpaces space.
 *
 * @author yitzhaki
 */
public class OpenSpacesMessageDispatcher extends AbstractMessageDispatcher {

    private static final String ENDPOINT_PARAM_WRITE_LEASE = "writeLease";

    private static final String ENDPOINT_PARAM_UPDATE_OR_WRITE = "updateOrWrite";

    private static final String ENDPOINT_PARAM_UPDATE_TIMEOUT = "updateTimeout";

    private GigaSpace gigaSpace;

    private long writeLease = Lease.FOREVER;

    private boolean updateOrWrite = true;

    private long updateTimeout = JavaSpace.NO_WAIT;


    public OpenSpacesMessageDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
        ApplicationContext applicationContext = ((OpenSpacesConnector) getConnector()).getApplicationContext();
        String spaceId = endpoint.getEndpointURI().getAddress();
        initWritingAttributes(endpoint);
        gigaSpace = (GigaSpace) applicationContext.getBean(spaceId);
    }

    /**
     * Extract the writeLease, updateOrWrite & updateTimeout from the URI.
     * If atrribute is missing sets the default.
     */
    private void initWritingAttributes(UMOImmutableEndpoint endpoint) {
        Properties params = endpoint.getEndpointURI().getParams();
        if (params != null) {
            try {
                String writeLeaseStr = (String) params.get(ENDPOINT_PARAM_WRITE_LEASE);
                if (writeLeaseStr != null) {
                    writeLease = Long.valueOf(writeLeaseStr);
                }
                String updateOrWriteStr = (String) params.get(ENDPOINT_PARAM_UPDATE_OR_WRITE);
                if (updateOrWriteStr != null) {
                    updateOrWrite = Boolean.valueOf(updateOrWriteStr);
                }
                String updateTimeoutStr = (String) params.get(ENDPOINT_PARAM_UPDATE_TIMEOUT);
                if (updateTimeoutStr != null) {
                    updateTimeout = Long.valueOf(updateTimeoutStr);
                }
            } catch (NumberFormatException e) {
                throw new MuleRuntimeException(CoreMessages.failedToCreateConnectorFromUri(endpoint.getEndpointURI()), e);
            }
        }
    }

    protected void doDispose() {
    }

    protected void doDispatch(UMOEvent event) throws Exception {
        doSend(event);
    }

    protected UMOMessage doSend(UMOEvent event) throws Exception {
        Object payload = event.transformMessage();

        if (payload != null) {
            if (payload instanceof Object[]) {
                Object[] payloadArr = (Object[]) payload;
                if (updateOrWrite) {
                    long[] leases = new long[payloadArr.length];
                    Arrays.fill(leases, writeLease);
                    Object[] retVals = gigaSpace.updateMultiple(payloadArr, leases, UpdateModifiers.UPDATE_OR_WRITE);
                    for (Object retVal : retVals) {
                        if (retVal instanceof DataAccessException) {
                            throw (DataAccessException) retVal;
                        }
                    }
                } else {
                    gigaSpace.writeMultiple(payloadArr, writeLease);
                }
            } else {
                if (updateOrWrite) {
                    gigaSpace.write(payload, writeLease, updateTimeout, UpdateModifiers.UPDATE_OR_WRITE);
                } else {
                    gigaSpace.write(payload, writeLease, updateTimeout, UpdateModifiers.WRITE_ONLY);
                }
            }
        }
        return null;
    }



    protected void doConnect() throws Exception {
    }

    protected void doDisconnect() throws Exception {
    }

    protected UMOMessage doReceive(long timeout) throws Exception {
        return null;
    }
}
