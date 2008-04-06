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
package org.openspaces.itest.esb.servicemix.flow;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.resolver.NullEndpointFilter;
import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.SenderComponent;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;

/**
 * Extends {@link org.apache.servicemix.tck.SenderComponent} adding it the ability to decide if
 * to send the mesage stateless or statefull.
 *
 * @author yitzhaki
 */
public class StatelessSenderComponent extends SenderComponent implements MessageExchangeListener {

    private MessageList responseMessageList = new MessageList();

    private boolean stateless = true;


    public void sendMessages(int messageCount, boolean sync) throws JBIException {
        ComponentContext context = getContext();

        for (int i = 0; i < messageCount; i++) {
            InOnly exchange = context.getDeliveryChannel().createExchangeFactory().createInOnlyExchange();

            if (stateless) {
                exchange.setProperty(JbiConstants.STATELESS_CONSUMER, true);
            }

            NormalizedMessage msg = exchange.createMessage();

            ServiceEndpoint destination = null;
            if (getResolver() != null) {
                destination = getResolver().resolveEndpoint(getContext(), exchange, NullEndpointFilter.getInstance());
            }
            if (destination != null) {
                // lets explicitly specify the destination - otherwise
                // we'll let the container choose for us
                exchange.setEndpoint(destination);
            }

            exchange.setInMessage(msg);
            // lets set the XML as a byte[], String or DOM etc
            msg.setContent(new StringSource(this.getMessage()));
            if (sync) {
                boolean result = context.getDeliveryChannel().sendSync(exchange, 1000);
                if (!result) {
                    throw new MessagingException("Message delivery using sendSync has timed out");
                } else {
                    if (exchange.getStatus() == ExchangeStatus.DONE) {
                        responseMessageList.addMessage(exchange.getMessage(MessageExchangeImpl.OUT));
                    } else {
                        throw new MessagingException("Exchange Status supposed to be Done.");
                    }
                }
            } else {
                context.getDeliveryChannel().send(exchange);
            }
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            responseMessageList.addMessage(exchange.getMessage(MessageExchangeImpl.OUT));
        } else {
            throw new MessagingException("Exchange Status supposed to be Done.");
        }
    }

    public MessageList getResponseMessageList() {
        return responseMessageList;
    }

    public boolean isStateless() {
        return stateless;
    }

    public void setStateless(boolean stateless) {
        this.stateless = stateless;
    }
}
