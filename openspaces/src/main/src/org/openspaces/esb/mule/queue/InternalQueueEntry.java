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

package org.openspaces.esb.mule.queue;

import com.gigaspaces.annotation.pojo.SpacePersist;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import org.mule.api.MuleMessage;


/**
 * An internal queue entry holding the endopint address and the actual message.
 *
 * @author kimchy
 */
public class InternalQueueEntry {

    protected String endpointURI;

    protected MuleMessage message;

    protected Boolean persist;


    @SpaceProperty(index = SpaceProperty.IndexType.BASIC)
    public String getEndpointURI() {
        return endpointURI;
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    public MuleMessage getMessage() {
        return message;
    }

    public void setMessage(MuleMessage message) {
        this.message = message;
    }

    @SpacePersist
    public boolean isPersist() {
        return (persist != null && persist.booleanValue());
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    @Override
    public String toString() {
        return "endpointURI = " + endpointURI + ", message = " + message + " persist = " + persist;
    }
}
