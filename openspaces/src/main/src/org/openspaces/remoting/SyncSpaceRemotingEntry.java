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
import net.jini.core.transaction.Transaction;

import java.util.Arrays;

/**
 * Default implementation of a remoting entry that acts both as a remote invocation and a remote
 * result.
 *
 * @author kimchy
 */
public class SyncSpaceRemotingEntry extends MetaDataEntry implements SpaceRemotingInvocation, SpaceRemotingResult, Cloneable {

    public Long uid;

    public String lookupName;

    public String methodName;

    public Object[] arguments;

    public Object[] metaArguments;

    public Boolean oneWay;

    public Integer routing;

    public Object result;

    public Throwable ex;

    public Integer instanceId;

    public Transaction transaction;

    public SyncSpaceRemotingEntry() {
        makeTransient();
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


    public SyncSpaceRemotingEntry buildInvocation(String lookupName, String methodName, Object[] arguments) {
        clearResultData();
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
        return this;
    }

    public SyncSpaceRemotingEntry buildResult(Throwable e) {
        clearInvocationData();
        this.ex = e;
        return this;
    }

    public SyncSpaceRemotingEntry buildResult(Object result) {
        clearInvocationData();
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

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (result == null && ex == null) {
            sb.append("lookupName [").append(lookupName).append("]");
            sb.append(" methodName[").append(methodName).append("]");
            sb.append(" arguments[").append(Arrays.toString(arguments)).append("]");
            sb.append(" routing[").append(routing).append("]");
            sb.append(" oneWay[").append(oneWay).append("]");
            sb.append(" transaction[").append(transaction).append("]");
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
}