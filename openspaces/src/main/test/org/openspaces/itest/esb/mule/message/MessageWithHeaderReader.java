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

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;


/**
 * <code>MessageWithHeaderReader</code> used as a UMOComponent and implements Callable,
 * meaning that the onCall method invoked by the mule framework.
 *
 * @author yitzhaki
 */
public class MessageWithHeaderReader implements Callable {


    public Object onCall(UMOEventContext eventContext) throws Exception {
        Object payload = eventContext.getMessage().getPayload();
        return changeName((MessageWithMessageHeader) payload);
    }

    /**
     * Change the value of name property to "new" + name value.
     */
    private Object changeName(MessageWithMessageHeader message) {
        String name = (String) message.getProperty("name");
        message.setProperty("name", "new " + name);
        return message;
    }
}