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
package org.openspaces.itest.esb.servicemix;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.RuntimeJBIException;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * A service engine that set the <code>org.openspaces.itest.esb.servicemix</code> read to true.    
 *
 * @author yitzhaki
 */
public class MessageReaderSE extends ComponentSupport implements MessageExchangeListener {

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus().equals(ExchangeStatus.DONE)) {
            return;
        }
        try {
            NormalizedMessage msg = exchange.getMessage("in");
            Message message = (Message) msg.getProperty("pojo");
            message.setRead(true);
            InOnly messageExchange = getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();
            inMessage.setProperty("pojo", message);
            messageExchange.setInMessage(inMessage);
            send(messageExchange);
            done(exchange);
        }
        catch (JBIException e) {
            throw new RuntimeJBIException(e);
        }
    }
}
