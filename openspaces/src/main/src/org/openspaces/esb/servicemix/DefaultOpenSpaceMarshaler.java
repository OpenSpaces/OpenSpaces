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

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.TransactionStatus;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;


/**
 * Default implmention of <code>org.openspaces.esb.servicemix.OpenSpaceMarshaler </code>
 * Carring the data pogo as a normalized message property.
 *
 * @author yitzhaki
 */
public final class DefaultOpenSpaceMarshaler implements OpenSpaceMarshaler {

    public void toNMS(NormalizedMessage inMessage, MessageExchange messageExchange, Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        inMessage.setProperty("pojo", data);
    }

    public Object fromNMS(MessageExchange messageExchange, NormalizedMessage message) {
        return message.getProperty("pojo");
    }

}
