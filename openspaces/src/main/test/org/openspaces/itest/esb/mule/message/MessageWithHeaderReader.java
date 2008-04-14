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

package org.openspaces.itest.esb.mule.message;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;


/**
 * <code>MessageWithHeaderReader</code> used as a UMOComponent and implements Callable,
 * meaning that the onCall method invoked by the mule framework.
 *
 * @author yitzhaki
 */
public class MessageWithHeaderReader implements Callable {

    /**
     * Change the value of name property to "new" + name value.
     */
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage umoMsg = eventContext.getMessage();
        Object payload = umoMsg.getPayload();
        MessageWithMessageHeader message = ((MessageWithMessageHeader) payload);
        String name = (String) message.getProperty("name");
        //pay attention that we are setting the new value on the umoMsg instead of the payload.
        //this is due to the fact the the transformer will override the value of thie property on the orignal message
        //by coping the values from the Adapter to the message.
        umoMsg.setProperty("name", "new " + name);
        message.setRead(true);
        return message;
    }

}