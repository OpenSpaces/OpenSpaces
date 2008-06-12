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

import com.j_spaces.core.client.MetaDataEntry;
import org.mule.api.MuleMessage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.MarshalledObject;

/**
 * An internal queue entry holding the endopint address and the actual message.
 *
 * @author kimchy
 */
public class InternalQueueEntry extends MetaDataEntry implements Externalizable {

    public String endpointURI;

    public MuleMessage message;

    private MarshalledObject marshalledObject;

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"endpointURI"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        MarshalledObject marshalledObject = new MarshalledObject(message);
        out.writeUTF(endpointURI);
        out.writeObject(marshalledObject);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        endpointURI = in.readUTF();
        marshalledObject = (MarshalledObject) in.readObject();
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
        message = (MuleMessage) marshalledObject.get();
        marshalledObject = null;
        return message;
    }
}
