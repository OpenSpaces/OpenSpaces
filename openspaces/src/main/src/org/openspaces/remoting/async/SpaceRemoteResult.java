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

package org.openspaces.remoting.async;

import com.j_spaces.core.client.EntryInfo;
import com.j_spaces.core.client.MetaDataEntry;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents a Space remote inocation result. Either holds the remote inocation result or an
 * exception that occured during the invocation.
 *
 * <p>The result has the a routing field (used in partitioned spaces) that reflects the same
 * value to remoting invocation holds.
 *
 * @author kimchy
 */
public class SpaceRemoteResult<T> extends MetaDataEntry implements Externalizable {

    private static final long serialVersionUID = -5466117072163590804L;

    public T result;

    public Exception ex;

    public Integer routing;

    /**
     * Constructs a new remote invocation result.
     */
    public SpaceRemoteResult() {
        setNOWriteLeaseMode(true);
    }

    /**
     * Constructs a new remote invocation result based on the remote invocation. Uses the remote
     * invocation to initalize the the {@link #routing} field and the result UID (acts as the
     * correlation id).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation) {
        setNOWriteLeaseMode(true);
        __setEntryInfo(new EntryInfo(remoteInvocation.__getEntryInfo().m_UID + "Result", 0));
        this.routing = remoteInvocation.routing;
    }

    /**
     * Constructs a new remote invocation result based on the remote invocation and an exception.
     * Uses the remote invocation to initalize the the {@link #routing} field and the result UID
     * (acts as the correlation id).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, Exception ex) {
        setNOWriteLeaseMode(true);
        __setEntryInfo(new EntryInfo(remoteInvocation.__getEntryInfo().m_UID + "Result", 0));
        this.ex = ex;
        this.routing = remoteInvocation.routing;
    }

    /**
     * Constructs a new remote invocation result based on the remote invocation and a result. Uses
     * the remote invocation to initalize the the {@link #routing} field and the result UID (acts
     * as the correlation id).
     */
    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, T result) {
        setNOWriteLeaseMode(true);
        __setEntryInfo(new EntryInfo(remoteInvocation.__getEntryInfo().m_UID + "Result", 0));
        this.result = result;
        this.routing = remoteInvocation.routing;
    }

    /**
     * Returns the result of the remote invocation. Can be <code>null</code> if the remote service
     * methods has a void return value or if an exception occured during the invocation.
     */
    public T getResult() {
        return result;
    }

    /**
     * Returns an exception that was thrown from the remote service method. Can be <code>null</code>
     * if no exception was thrown.
     */
    public Exception getEx() {
        return ex;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        if (result != null) {
            out.writeBoolean(true);
            out.writeObject(result);
        } else {
            out.writeBoolean(false);
        }
        if (ex != null) {
            out.writeBoolean(true);
            out.writeObject(ex);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(routing);
    }

    @SuppressWarnings({"unchecked"})
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        if (in.readBoolean()) {
            result = (T) in.readObject();
        }
        if (in.readBoolean()) {
            ex = (Exception) in.readObject();
        }
        routing = in.readInt();
    }
}
