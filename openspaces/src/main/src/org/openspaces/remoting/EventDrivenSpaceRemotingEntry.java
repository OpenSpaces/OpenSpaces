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
public class EventDrivenSpaceRemotingEntry extends MetaDataEntry implements SpaceRemotingInvocation, SpaceRemotingResult,
Cloneable, Externalizable {

    static int bitIndexCounter=0;
    private static final int LOOKUP_NAME_BIT_MASK = 1 << bitIndexCounter++;
    private static final int METHOD_NAME_BIT_MASK = 1 << bitIndexCounter++;
    private static final int ROUTING_BIT_MASK = 1 << bitIndexCounter++;
    private static final int ONE_WAY_BIT_MASK = 1<< bitIndexCounter++;
    private static final int ARGUMENTS_BIT_MASK = 1<< bitIndexCounter++;
    private static final int META_ARGUMENTS_BIT_MASK = 1 << bitIndexCounter++;
    private static final int RESULT_BIT_MASK = 1 << bitIndexCounter++;
    private static final int EX_BIT_MASK = 1 << bitIndexCounter++;
    private static final int INSTANCE_ID_BIT_MASK = 1 << bitIndexCounter++;


    public Boolean isInvocation;

    public String lookupName;

    public String methodName;

    public Object[] arguments;

    public Object[] metaArguments;

    public Boolean oneWay;

    public Integer routing;

    public Object result;

    public Throwable ex;

    public Integer instanceId;

    /**
     * Constructs a new Async remoting entry. By default a transient one witn that does not
     * return a lease. Also, by default, this is an invocation entry.
     */
    public EventDrivenSpaceRemotingEntry() {
        setNOWriteLeaseMode(true);
        makeTransient();
        isInvocation = true;
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

    public Object[] getMetaArguments() {
        return metaArguments;
    }

    public Integer getRouting() {
        return routing;
    }

    public void setRouting(Object routing) {
        this.routing = routing.hashCode();
    }

    public Object getResult() {
        return this.result;
    }

    public Throwable getException() {
        return this.ex;
    }

    public Integer getInstanceId() {
        return this.instanceId;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing"};
    }


    public EventDrivenSpaceRemotingEntry buildInvocation(String lookupName, String methodName, Object[] arguments) {
        clearResultData();
        this.isInvocation = true;
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
        return this;
    }

    public EventDrivenSpaceRemotingEntry buildResultTemplate() {
        clearInvocationData();
        clearResultData();
        buildResultUID();
        this.isInvocation = false;
        return this;
    }

    public EventDrivenSpaceRemotingEntry buildResult(Throwable e) {
        clearInvocationData();
        buildResultUID();
        this.isInvocation = false;
        this.ex = e;
        return this;
    }

    public EventDrivenSpaceRemotingEntry buildResult(Object result) {
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        short nullableFieldsBitMask = getNullableFieldsBitMask();
        out.writeShort(nullableFieldsBitMask);
        out.writeBoolean(isInvocation);

        if (isInvocation) {
            if (lookupName != null) {
                out.writeUTF(lookupName);
            }

            if (methodName != null) {
                out.writeUTF(methodName);
            }

            if (routing != null) {
                out.writeInt(routing);
            }

            if (oneWay != null && oneWay) {
                out.writeBoolean(true);
            }

            if (arguments != null && arguments.length != 0) {
                out.writeInt(arguments.length);
                for (Object argument : arguments) {
                    out.writeObject(argument);
                }
            }
            if (metaArguments != null && metaArguments.length != 0) {
                out.writeInt(metaArguments.length);
                for (Object argument : metaArguments) {
                    out.writeObject(argument);
                }
            }
        } else {
            if (result != null) {
                out.writeObject(result);
            }
            if (ex != null) {
                out.writeObject(ex);
            }
            if (routing != null) {
                out.writeInt(routing);
            }
            if (instanceId != null) {
                out.writeInt(instanceId);
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        short bitMask = in.readShort();
        isInvocation = in.readBoolean();
        if (isInvocation) {
            if (!isFieldNull(bitMask, LOOKUP_NAME_BIT_MASK)) {
                lookupName = in.readUTF();
            }
            if (!isFieldNull(bitMask, METHOD_NAME_BIT_MASK)) {
                methodName = in.readUTF();
            }
            if (!isFieldNull(bitMask, ROUTING_BIT_MASK)) {
                routing = in.readInt();
            }
            if (!isFieldNull(bitMask, ONE_WAY_BIT_MASK)) {
                oneWay = in.readBoolean();
            }

            if (!isFieldNull(bitMask, ARGUMENTS_BIT_MASK)) {
                int argumentNumber = in.readInt();
                arguments = new Object[argumentNumber];
                for (int i = 0; i < argumentNumber; i++) {
                    arguments[i] = in.readObject();
                }
            }

            if (!isFieldNull(bitMask, META_ARGUMENTS_BIT_MASK)) {
                int argumentNumber = in.readInt();
                metaArguments = new Object[argumentNumber];
                for (int i = 0; i < argumentNumber; i++) {
                    metaArguments[i] = in.readObject();
                }
            }
        } else {
            if (!isFieldNull(bitMask, RESULT_BIT_MASK)) {
                result = in.readObject();
            }
            if (!isFieldNull(bitMask, EX_BIT_MASK)) {
                ex = (Throwable) in.readObject();
            }
            if (!isFieldNull(bitMask, ROUTING_BIT_MASK)) {
                routing = in.readInt();
            }
            if (!isFieldNull(bitMask, INSTANCE_ID_BIT_MASK)) {
                instanceId = in.readInt();
            }
        }
    }

    @Override
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
            sb.append(" instanceId[").append(instanceId).append("]");
        }
        return sb.toString();
    }

    /**
     * Returns a bit mask. Fields with non-null values have 1 in their respective index, fields with
     * null values have 0.
     */
    private short getNullableFieldsBitMask() {
        int bitMask = 0;
        bitMask = ((lookupName != null)  ? bitMask | LOOKUP_NAME_BIT_MASK : bitMask) ;
        bitMask = ((methodName != null)  ? bitMask | METHOD_NAME_BIT_MASK : bitMask) ;
        bitMask = ((routing != null)  ? bitMask | ROUTING_BIT_MASK : bitMask) ;
        bitMask = ((oneWay != null)  ? bitMask | ONE_WAY_BIT_MASK : bitMask) ;
        bitMask = ((arguments != null && arguments.length > 0)  ? bitMask | ARGUMENTS_BIT_MASK : bitMask) ;
        bitMask = ((metaArguments != null && metaArguments.length > 0)  ? bitMask | META_ARGUMENTS_BIT_MASK : bitMask) ;
        bitMask = ((result != null)  ? bitMask | RESULT_BIT_MASK : bitMask) ;
        bitMask = ((ex != null)  ? bitMask | EX_BIT_MASK : bitMask) ;
        bitMask = ((instanceId != null)  ? bitMask | INSTANCE_ID_BIT_MASK : bitMask) ;
        return (short)bitMask;
    }

    private boolean isFieldNull(short bitMask, int fieldBitMask) {
        return (bitMask & fieldBitMask) == 0;

    }
}
