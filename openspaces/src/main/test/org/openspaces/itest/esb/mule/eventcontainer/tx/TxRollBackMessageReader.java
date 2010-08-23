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

package org.openspaces.itest.esb.mule.eventcontainer.tx;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.openspaces.itest.esb.mule.SimpleMessage;


/**
 * This service throws <code>Error</code> at the first invocation, thus causing the transction to rollback.
 * At the second invocation it will just return the payload concatenated with the service name.
 *
 * @author kimchy
 */
public class TxRollBackMessageReader implements Callable {

    private static boolean alreadyRollbacked = false;

    public Object onCall(MuleEventContext eventContext) throws Exception {
        if (alreadyRollbacked) {
            SimpleMessage message = (SimpleMessage) eventContext.getMessage().getPayload();
            message.setRead(true);
            message.setMessage("rolledback");
            return message;
        } 
        alreadyRollbacked = true;
        throw new RuntimeException("Rolling back (-:");
    }
}