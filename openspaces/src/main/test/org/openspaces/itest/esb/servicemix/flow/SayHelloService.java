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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * @author yitzhaki
 */
public class SayHelloService extends ComponentSupport implements MessageExchangeListener {
    private static transient Log log = LogFactory.getLog(SayHelloService.class);

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            log.info("message received, exchange.status=" + exchange.getStatus());
            NormalizedMessage out = exchange.createMessage();
            out.setContent(new StringSource("<response> Say hello back </response>"));
            log.info("sending response, exchange.status=" + exchange.getStatus());
            exchange.setMessage(out, "out");
            getDeliveryChannel().sendSync(exchange);
            log.info("response sent, exchange.status=" + exchange.getStatus());
        } else {
            log.info("got a message, exchange.status=" + exchange.getStatus());
        }
    }
}