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
package org.openspaces.esb.servicemix.flow;

import com.j_spaces.core.client.MetaDataEntry;

import javax.jbi.messaging.MessageExchange;

/**
 * Base class that represents an Entry to the space which contains
 * {@link javax.jbi.messaging.MessageExchange}.
 *
 * @author yitzhaki
 */
public abstract class MessageExchangeEntry extends MetaDataEntry {

    public MessageExchange message;

    @Override
    public String toString() {
        return message.toString();
    }
}
