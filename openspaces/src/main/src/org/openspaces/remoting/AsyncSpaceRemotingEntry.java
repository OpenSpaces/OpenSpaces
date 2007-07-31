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

package org.openspaces.remoting;

import com.j_spaces.core.client.MetaDataEntry;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Default implementation of a remoting entry that acts both as a remote invocation and a remote
 * result.
 *
 * @author kimchy
 */
public class AsyncSpaceRemotingEntry extends MetaDataEntry implements SpaceRemotingInvocation, SpaceRemotingResult,
        Cloneable, Externalizable {

    public Boolean isInvocation;

    public String lookupName;

    public String methodName;

    public Object[] arguments;

    public Boolean oneWay;

    public Object routing;

    public Object result;

    public Exception ex;

    public AsyncSpaceRemotingEntry() {
        setNOWriteLeaseMode(false);
    }

    public String getLookupName() {
        return lookupName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getRouting() {
        return routing;
    }

    public void setRouting(Object routing) {
        this.routing = routing;
    }

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return ex;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing"};
    }


    public AsyncSpaceRemotingEntry buildInvocation(String lookupName, String methodName, Object[] arguments) {
        clearResultData();
        this.isInvocation = true;
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
        return this;
    }

    public AsyncSpaceRemotingEntry buildResultTemplate() {
        clearInvocationData();
        clearResultData();
        buildResultUID();
        this.isInvocation = false;
        return this;
    }

    public AsyncSpaceRemotingEntry buildResult(Exception e) {
        clearInvocationData();
        buildResultUID();
        this.isInvocation = false;
        this.ex = e;
        return this;
    }

    public AsyncSpaceRemotingEntry buildResult(Object result) {
        clearInvocationData();
        buildResultUID();
        this.isInvocation = false;
        this.result = result;
        return this;
    }

    private void clearResultData() {
        this.result = null;
        this.ex = null;
    }

    private void clearInvocationData() {
        this.lookupName = null;
        this.methodName = null;
        this.arguments = null;
        this.oneWay = null;
    }

    private void buildResultUID() {
        if (__getEntryInfo() != null && __getEntryInfo().m_UID != null) {
            __getEntryInfo().m_UID += "Result";
        }
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        out.writeBoolean(isInvocation);
        if (isInvocation) {
            out.writeUTF(lookupName);
            out.writeUTF(methodName);
            out.writeObject(routing);
            if (oneWay != null && oneWay) {
                out.writeBoolean(true);
            } else {
                out.writeBoolean(false);
            }
            if (arguments == null || arguments.length == 0) {
                out.writeInt(0);
            } else {
                out.writeInt(arguments.length);
            }
            for (Object argument : arguments) {
                out.writeObject(argument);
            }
        } else {
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
            out.writeObject(routing);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        isInvocation = in.readBoolean();
        if (isInvocation) {
            lookupName = in.readUTF();
            methodName = in.readUTF();
            routing = in.readInt();
            oneWay = in.readBoolean();
            int argumentNumber = in.readInt();
            if (argumentNumber > 0) {
                arguments = new Object[argumentNumber];
                for (int i = 0; i < argumentNumber; i++) {
                    arguments[i] = in.readObject();
                }
            }
        } else {
            if (in.readBoolean()) {
                result = in.readObject();
            }
            if (in.readBoolean()) {
                ex = (Exception) in.readObject();
            }
            routing = in.readObject();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isInvocation) {
            sb.append("lookupName [").append(lookupName).append("]");
            sb.append(" methodName[").append(methodName).append("]");
            sb.append(" routing[").append(routing).append("]");
            sb.append(" oneWay[").append(oneWay).append("]");
        } else {
            if (result != null) {
                sb.append("result[").append(result).append("]");
            }
            if (ex != null) {
                sb.append("ex").append(ex).append("]");
            }
            sb.append(" routing[").append(routing).append("]");
        }
        return sb.toString();
    }
}
