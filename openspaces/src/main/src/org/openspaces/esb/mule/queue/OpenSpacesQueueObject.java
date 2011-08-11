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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.core.util.ThreadLocalMarshaller;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceDynamicProperties;
import com.gigaspaces.annotation.pojo.SpaceExclude;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpacePersist;
import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.internal.io.IOUtils;

/**
 * An internal queue entry holding the endpoint address and the queue payload. The payload can be
 * serialized when written to space, to avoid class loading issues on space side.
 * 
 * 
 * @author anna
 */
@SpaceClass(replicate = true, fifoSupport = FifoSupport.OPERATION)
public class OpenSpacesQueueObject implements Externalizable {

    private static final long serialVersionUID = 1L;

    private String endpointURI;

    private Object internalPayload;

    private DocumentProperties payloadMetaData = new DocumentProperties();

    private boolean isPersistent = true;

    // kept as a separate field and not in the meta data map for more efficient queries
    private String correlationID;

    public OpenSpacesQueueObject() {
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    @SpaceIndex
    public String getEndpointURI() {
        return endpointURI;
    }

    @SpaceExclude
    public Object getPayload() throws IOException, ClassNotFoundException {
        if (internalPayload == null)
            return null;
        return ThreadLocalMarshaller.objectFromByteBuffer((byte[]) internalPayload);
    }

    public void setPayload(Object payload) throws IOException {
        this.internalPayload = ThreadLocalMarshaller.objectToByteBuffer(payload);
    }

    /**
     * For internal usage only, to get the payload use {@link #getPayload()} method.
     * 
     * @return
     */
    public Object getInternalPayload() {
        return internalPayload;
    }

    /**
     * For internal usage only, to set the payload use {@link #setPayload()} method.
     * 
     * @param payload
     */
    public void setInternalPayload(Object payload) {
        this.internalPayload = payload;
    }

    @SpaceDynamicProperties
    public DocumentProperties getPayloadMetaData() {
        return payloadMetaData;
    }

    public void setPayloadMetaData(DocumentProperties payloadMetaData) {
        this.payloadMetaData = payloadMetaData;
    }

    @SpaceIndex
    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    @SpacePersist
    public boolean getPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeString(out, endpointURI);
        out.writeBoolean(isPersistent);
        IOUtils.writeObject(out, internalPayload);
        IOUtils.writeObject(out, payloadMetaData);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        endpointURI = IOUtils.readString(in);
        isPersistent = in.readBoolean();
        internalPayload = IOUtils.readObject(in);
        payloadMetaData = IOUtils.readObject(in);
    }

    @Override
    public String toString() {
        return "OpenSpacesQueueObject [endpointURI=" + endpointURI + ", internalPayload=" + internalPayload
                + ", payloadMetaData=" + payloadMetaData + ", isPersistent=" + isPersistent + "]";
    }

}
