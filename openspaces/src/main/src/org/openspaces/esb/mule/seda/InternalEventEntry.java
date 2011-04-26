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

package org.openspaces.esb.mule.seda;

import com.j_spaces.core.client.IReplicatable;
import com.j_spaces.core.client.MetaDataEntry;
import org.mule.api.MuleEvent;
import org.openspaces.core.util.ThreadLocalMarshaller;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An internal entry holding the name of the serice (that has the SEDA queue) and the
 * actual mule event to be used.
 *
 * @author kimchy
 */
public class InternalEventEntry extends MetaDataEntry implements Externalizable, IReplicatable {

    public String name;

    public MuleEvent event;

    public byte[] marshalledObject;

    public InternalEventEntry() {
    }

    public InternalEventEntry(MuleEvent event, String name) {
        this.event = event;
        this.name = name;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"name"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        out.writeUTF(name);
        if (event != null) {
            marshalledObject = ThreadLocalMarshaller.objectToByteBuffer(event);
        }
        // null the event so it won't be stored in the space when working remotely
        event = null;
        if (marshalledObject == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(marshalledObject.length);
            out.write(marshalledObject);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        name = in.readUTF();
        int size = in.readInt();
        if (size != -1) {
            marshalledObject = new byte[size];
            in.readFully(marshalledObject);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MuleEvent getEvent() throws IOException, ClassNotFoundException {
        if (event != null) {
            return event;
        }
        if (marshalledObject == null) {
            return null;
        }
        event = (MuleEvent) ThreadLocalMarshaller.objectFromByteBuffer(marshalledObject);
        marshalledObject = null;
        return event;
    }

    public void setEvent(MuleEvent event) {
        this.event = event;
    }
}