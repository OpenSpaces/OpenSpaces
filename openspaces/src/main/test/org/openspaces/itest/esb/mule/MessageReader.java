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

package org.openspaces.itest.esb.mule;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

/**
 * <code>MessageReader</code> used as a UMOComponent and implements Callable,
 * meaning that the onCall method invoked by the mule framework.
 *
 * @author yitzhaki
 */
public class MessageReader implements Callable {


    public Object onCall(MuleEventContext eventContext) throws Exception {
        Object payload = eventContext.getMessage().getPayload();
        return read(payload);
    }

    /**
     * Sets to true the obj read attribute.
     */
    private Object read(Object obj) {
        if (obj instanceof Object[]) {
            for (Object o : ((Object[]) obj)) {
                ((SimpleMessage) o).setRead(true);
            }
        } else {
            ((SimpleMessage) obj).setRead(true);
        }
        return obj;
    }

}