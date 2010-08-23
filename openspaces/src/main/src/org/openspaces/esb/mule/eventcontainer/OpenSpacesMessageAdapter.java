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
package org.openspaces.esb.mule.eventcontainer;

import org.mule.api.MessagingException;
import org.mule.api.ThreadSafeAccess;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.transport.AbstractMessageAdapter;
import org.mule.util.UUID;
import org.openspaces.esb.mule.message.CorrelationMessageHeader;
import org.openspaces.esb.mule.message.MessageHeader;
import org.openspaces.esb.mule.message.ReplyToMessageHeader;
import org.openspaces.esb.mule.message.UniqueIdMessageHeader;

import java.util.Iterator;

/**
 * @author yitzhaki
 */
public class OpenSpacesMessageAdapter extends AbstractMessageAdapter {

    private Object message;

    public OpenSpacesMessageAdapter(Object msg) throws MessagingException {
        super();
        this.setMessage(msg);
    }

    public OpenSpacesMessageAdapter(OpenSpacesMessageAdapter template) {
        super(template);
        this.message = template.message;
    }

    /**
     * @param message new value for the message.
     */
    private void setMessage(Object message) throws MessagingException {
        if (message == null) {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
        this.message = message;

        if (message instanceof MessageHeader) {
            Iterator keys = ((MessageHeader) message).getProperties().keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = ((MessageHeader) message).getProperty(key);
                if (value != null) {
                    setProperty(key, value);
                }
            }
        }
        if (getProperty(UniqueIdMessageHeader.UNIQUE_ID) == null) {
            setProperty(UniqueIdMessageHeader.UNIQUE_ID, UUID.getUUID());
        }
    }

    /**
     * @return the current message
     */
    public Object getPayload() {
        return message;
    }

    @Override
    public String getUniqueId() {
        return (String) getProperty(UniqueIdMessageHeader.UNIQUE_ID);
    }

    @Override
    public String getCorrelationId() {
        return (String) getProperty(CorrelationMessageHeader.CORRELATION_ID);
    }

    @Override
    public void setCorrelationId(String correlationId) {
        setProperty(CorrelationMessageHeader.CORRELATION_ID, correlationId);
    }

    @Override
    public int getCorrelationSequence() {
        Object correlationSequence = getProperty(CorrelationMessageHeader.CORRELATION_SEQUENCE);
        if(correlationSequence==null){
            return -1;
        }
        return (Integer) correlationSequence;
    }

    @Override
    public void setCorrelationSequence(int sequence) {
        setProperty(CorrelationMessageHeader.CORRELATION_SEQUENCE, sequence);
    }

    @Override
    public int getCorrelationGroupSize() {
        Object correlationGroupSize = getProperty(CorrelationMessageHeader.CORRELATION_GROUP_SIZE);
        if (correlationGroupSize == null) {
            return -1;
        }
        return (Integer) correlationGroupSize;
    }

    @Override
    public void setCorrelationGroupSize(int correlationGroupSize) {
        setProperty(CorrelationMessageHeader.CORRELATION_GROUP_SIZE, correlationGroupSize);
    }

    @Override
    public Object getReplyTo() {
        return getProperty(ReplyToMessageHeader.REPLY_TO);
    }

    @Override
    public void setReplyTo(Object replyTo) {
        setProperty(ReplyToMessageHeader.REPLY_TO, replyTo);
    }

    @Override
    public ThreadSafeAccess newThreadCopy() {
        return new OpenSpacesMessageAdapter(this);
    }

}
