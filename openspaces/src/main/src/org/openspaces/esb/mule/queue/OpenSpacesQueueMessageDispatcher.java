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

package org.openspaces.esb.mule.queue;

import org.mule.config.i18n.CoreMessages;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.DispatchException;

/**
 * Dispatches (writes) a message to an intenral queue. The queue is a virtualized queue represented
 * by the {@link org.openspaces.esb.mule.queue.InternalQueueEntry} with its endpoint address
 * set (and not the message).
 *
 * @author kimchy
 */
public class OpenSpacesQueueMessageDispatcher extends AbstractMessageDispatcher {

    private final OpenSpacesQueueConnector connector;

    public OpenSpacesQueueMessageDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
        this.connector = (OpenSpacesQueueConnector) endpoint.getConnector();
    }

    protected void doDispatch(UMOEvent event) throws Exception {
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        //Apply any outbound transformers on this event before we dispatch
        event.transformMessage();

        if (endpointUri == null) {
            throw new DispatchException(
                    CoreMessages.objectIsNull("Endpoint"), event.getMessage(), event.getEndpoint());
        }

        InternalQueueEntry entry = new InternalQueueEntry();
        entry.message = event.getMessage();
        entry.endpointURI = endpointUri.getAddress();
        entry.setFifo(connector.isFifo());
        if (connector.isPersistent()) {
            entry.makePersistent();
        } else {
            entry.makeTransient();
        }

        connector.getGigaSpaceObj().write(entry);

        if (logger.isDebugEnabled()) {
            logger.debug("dispatched Event on endpointUri: " + endpointUri);
        }
    }

    protected UMOMessage doSend(UMOEvent event) throws Exception {
        UMOMessage retMessage;
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        OpenSpacesQueueMessageReceiver receiver = connector.getReceiver(endpointUri);
        //Apply any outbound transformers on this event before we dispatch
        event.transformMessage();
        if (receiver == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Writing to queue as there is no receiver on connector: "
                        + connector.getName() + ", for endpointUri: "
                        + event.getEndpoint().getEndpointURI());
            }
            doDispatch(event);
            return null;
        }

        UMOMessage message = event.getMessage();
        connector.getSessionHandler().storeSessionInfoToMessage(event.getSession(), message);
        retMessage = (UMOMessage) receiver.onCall(message, event.isSynchronous());

        if (logger.isDebugEnabled()) {
            logger.debug("sent event on endpointUri: " + event.getEndpoint().getEndpointURI());
        }
        return retMessage;
    }

    protected void doDispose() {
        // template method
    }

    protected void doConnect() throws Exception {
        // template method
    }

    protected void doDisconnect() throws Exception {
        // template method
    }

}
