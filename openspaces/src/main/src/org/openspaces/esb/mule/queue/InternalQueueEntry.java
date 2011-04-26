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

import com.j_spaces.core.client.IReplicatable;
import com.j_spaces.core.client.MetaDataEntry;
import org.mule.api.MuleMessage;
import org.openspaces.core.util.ThreadLocalMarshaller;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An internal queue entry holding the endopint address and the actual message.
 *
 * @author kimchy
 */
public class InternalQueueEntry extends MetaDataEntry implements Externalizable, IReplicatable {

    public String endpointURI;

    public MuleMessage message;

    public byte[] marshalledObject;

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"endpointURI"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        out.writeUTF(endpointURI);
        if (message != null) {
            marshalledObject = ThreadLocalMarshaller.objectToByteBuffer(message);
        }
        // null the message so it won't be stored in the space when working remotely
        message = null;
        if (marshalledObject == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(marshalledObject.length);
            out.write(marshalledObject);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        endpointURI = in.readUTF();
        int size = in.readInt();
        if (size != -1) {
            marshalledObject = new byte[size];
            in.readFully(marshalledObject);
        }
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    public String getEndpointURI() {
        return endpointURI;
    }

    public void setMessage(MuleMessage message) {
        this.message = message;
    }

    public MuleMessage getMessage() throws IOException, ClassNotFoundException {
        if (message != null) {
            return message;
        }
        if (marshalledObject == null) {
            return null;
        }
        message = (MuleMessage) ThreadLocalMarshaller.objectFromByteBuffer(marshalledObject);
        marshalledObject = null;
        return message;
    }
}
