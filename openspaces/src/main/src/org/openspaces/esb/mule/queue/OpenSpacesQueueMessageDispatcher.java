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

import java.io.IOException;
import java.util.UUID;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transaction.TransactionCallback;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.Message;
import org.mule.transaction.TransactionTemplate;
import org.mule.transport.AbstractMessageDispatcher;

import com.gigaspaces.document.DocumentProperties;



/**
 * Dispatches (writes) a message to an internal queue. The queue is a virtualized queue represented
 * by the {@link org.openspaces.esb.mule.queue.OpenSpacesQueueObject} with its endpoint address
 * set (and not the message).
 *
 * @author kimchy
 */
public class OpenSpacesQueueMessageDispatcher extends AbstractMessageDispatcher {

    public static final String DEFAULT_RESPONSE_QUEUE = "_response_queue";

    private final OpenSpacesQueueConnector connector;

    public OpenSpacesQueueMessageDispatcher(OutboundEndpoint endpoint) {
        super(endpoint);
        this.connector = (OpenSpacesQueueConnector) endpoint.getConnector();
    }

    protected void doDispatch(final MuleEvent event) throws Exception {
        dispatchMessage(event, false);
    }       

    protected MuleMessage doSend(final MuleEvent event) throws Exception {
        return dispatchMessage(event, true);
    }
    
    private MuleMessage dispatchMessage(final MuleEvent event, boolean doSend) throws Exception
    {
        final EndpointURI endpointUri = event.getEndpoint().getEndpointURI();

        if (endpointUri == null) {
            Message objectIsNull = CoreMessages.objectIsNull("Endpoint");
            DispatchException ex = null;
            if (event.getEndpoint() instanceof MessageProcessor) {
                ex = new DispatchException(objectIsNull, event, (MessageProcessor) event.getEndpoint(), new Exception());
            } else {
                ex = new DispatchException(objectIsNull, event, null, new Exception());
            }
            throw ex;
        }
        final OpenSpacesQueueMessageReceiver receiver = connector.getReceiver(endpointUri);
        TransactionTemplate tt;
        if (receiver == null) {
            tt = new TransactionTemplate(event.getEndpoint().getTransactionConfig(), event.getMuleContext());
        } else {
            tt = new TransactionTemplate(receiver.getEndpoint().getTransactionConfig(), event.getMuleContext());
        }

        connector.getSessionHandler().storeSessionInfoToMessage(event.getSession(), event.getMessage());

        //handle transactional operations - don't put on queue - just execute recursively
        // note - transactions works only in the same mule scope.
        boolean isTransactional = event.getEndpoint().getTransactionConfig().isTransacted();
        if (isTransactional && receiver != null) {

            TransactionCallback cb = new TransactionCallback() {
                public Object doInTransaction() throws Exception {
                    return receiver.onCall(event.getMessage(), true);
                }
            };
            MuleEvent muleEvent = (MuleEvent) tt.execute(cb);
            return (MuleMessage) muleEvent.getMessage();
        }
        
        //check if a response should be returned for this endpoint
        boolean returnResponse = returnResponse(event, doSend) && !isTransactional;
       
        //assign correlationId for sync invocations - so that the request can be correlated with the response
        final String correlationId = createCorrelationId(event.getMessage(),returnResponse);
        
        MuleMessage message = event.getMessage();
        connector.getSessionHandler().storeSessionInfoToMessage(event.getSession(), message);

        TransactionCallback cb = new TransactionCallback()
        {
            public Object doInTransaction() throws Exception
            {
                OpenSpacesQueueObject entry = prepareMessageForDispatch(event.getMessage(), endpointUri, correlationId);

                connector.getGigaSpaceObj().write(entry);
                return null;
            }
        };
        
        tt.execute(cb); 
        
        if (logger.isDebugEnabled()) {
            logger.debug("sent event on endpointUri: " + event.getEndpoint().getEndpointURI());
        }
        
        // wait for reply if configured
        if (returnResponse) {
            return waitForResponse(event, correlationId);
        }
        return null;
    }

    private OpenSpacesQueueObject prepareMessageForDispatch(final  MuleMessage message,
            final EndpointURI endpointUri, final String correlationId) throws IOException {
        OpenSpacesQueueObject entry = new OpenSpacesQueueObject();
        entry.setPayload(message.getPayload());
        entry.setEndpointURI(endpointUri.getAddress());
        entry.setPersistent(connector.isPersistent());
        entry.setCorrelationID(correlationId);
        
        //copy the message properties
        DocumentProperties payloadMetaData = new DocumentProperties();
        for (String propertyName : message.getPropertyNames(PropertyScope.OUTBOUND)) {
            payloadMetaData.put(propertyName, message.getProperty(propertyName, PropertyScope.OUTBOUND));
        }
        entry.setPayloadMetaData(payloadMetaData);
        return entry;
    }
    
    private MuleMessage waitForResponse(final MuleEvent event, final String correlationId) {
        String replyTo = event.getEndpoint().getEndpointURI().getAddress() + DEFAULT_RESPONSE_QUEUE;
        
        int timeout = event.getTimeout();
        
        if (logger.isDebugEnabled()) {
            logger.debug("waiting for response Event on endpointUri: " + replyTo);
        } 
       
        OpenSpacesQueueObject template = new OpenSpacesQueueObject();
        template.setCorrelationID(correlationId);
        template.setEndpointURI(replyTo);
        
        try {
            OpenSpacesQueueObject responseEntry = connector.getGigaSpaceObj().take(template, timeout);
            if (logger.isDebugEnabled()) {
                logger.debug("got response Event on endpointUri: " + replyTo + " response=" + responseEntry);
            }

            MuleMessage createMuleMessage = createMuleMessage(responseEntry);
            return responseEntry == null ? null : createMuleMessage;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("got no response Event on endpointUri: " + replyTo);
            }
            return null;
        }
    }

    private String createCorrelationId(MuleMessage muleMessage, boolean returnResponse) {
        String correlationId = muleMessage.getCorrelationId();
        if(returnResponse && correlationId == null)
            correlationId = UUID.randomUUID().toString();
        return correlationId;
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
