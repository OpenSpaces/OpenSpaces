/*
* Copyright 2006-2007 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openspaces.esb.servicemix;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.RuntimeJBIException;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.AbstractEventListenerContainer;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * OpenSpaces {@link SpaceDataEventListener} which sends the inbound OpenSpaces message into the JBI container
 * for processing.
 *
 * @author yitzhaki
 */
public class OpenSpacesInBinding extends ComponentSupport implements SpaceDataEventListener, MessageExchangeListener, InitializingBean {

    private AbstractEventListenerContainer eventListenerContainer;

    private boolean synchronous = false;

    private OpenSpaceMarshaler marshaler;

    public void afterPropertiesSet() throws Exception {
        if (eventListenerContainer == null) {
            throw new IllegalArgumentException("Must have a eventListenerContainer set");
        }
        if (marshaler == null) {
            marshaler = new DefaultOpenSpaceMarshaler();
        }
        eventListenerContainer.setEventListener(this);
    }


    public void start() throws JBIException {
        eventListenerContainer.start();
        super.start();
    }

    public void stop() throws JBIException {
        super.stop();
        eventListenerContainer.stop();
    }

    public void shutDown() throws JBIException {
        eventListenerContainer.setEventListener(null);
        eventListenerContainer.shutdown();
    }

    /**
     * This component receives the event from the space marshals it to a normalized message and sends it (sync/async)
     * as an InOnly Message exchange.
     *
     * @param data      The actual data object of the event
     * @param gigaSpace A GigaSpace instance that can be used to perofrm additional operations against the
     *                  space
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    Optional additional data or the actual source event data object (where relevant)
     */
    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        try {
            InOnly messageExchange = getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();
            try {
                marshaler.toNMS(inMessage, messageExchange, data, gigaSpace, txStatus, source);
                messageExchange.setInMessage(inMessage);
                if (synchronous) {
                    sendSync(messageExchange);
                } else {
                    send(messageExchange);
                }
            }
            catch (Exception e) {
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
        }
        catch (JBIException e) {
            throw new RuntimeJBIException(e);
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (logger.isDebugEnabled()) {
            logger.debug("exchange returned status  " + exchange.getStatus());
        }
        // As we send in-only MEPS, we will only
        // receive DONE or ERROR status
        // Do nothing as we only send in-only
        // but this ensure that messages are not queued in the DeliveryChannel
    }

    public void setEventListenerContainer(AbstractEventListenerContainer eventListenerContainer) {
        this.eventListenerContainer = eventListenerContainer;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public OpenSpaceMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(OpenSpaceMarshaler marshaler) {
        this.marshaler = marshaler;
    }
}
