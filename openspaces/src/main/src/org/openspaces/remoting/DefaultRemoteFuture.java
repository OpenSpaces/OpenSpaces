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

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author kimchy
 */
public class DefaultRemoteFuture<T> implements RemoteFuture<T> {

    private GigaSpace gigaSpace;

    private SpaceRemoteInvocation remoteInvocation;

    private boolean cancelled;

    private SpaceRemoteResult<T> remoteResult;

    private SpaceRemoteResult<T> template;

    public DefaultRemoteFuture(GigaSpace gigaSpace, SpaceRemoteInvocation remoteInvocation) {
        this.gigaSpace = gigaSpace;
        this.remoteInvocation = remoteInvocation;
        this.template = new SpaceRemoteResult<T>(remoteInvocation);
    }

    public void cancel() throws RemoteAccessException, DataAccessException {
        Object retVal = gigaSpace.take(remoteInvocation, 0);
        if (retVal != null) {
            cancelled = true;
        }
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public T get() throws Exception {
        T retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = gigaSpace.take(template, Integer.MAX_VALUE);
        return handleResult();
    }

    public T get(long timeout) throws Exception {
        T retVal = handleResult();
        if (retVal != null) {
            return retVal;
        }
        remoteResult = gigaSpace.take(template, timeout);
        return handleResult();
    }

    private T handleResult() throws Exception {
        if (remoteResult == null) {
            return null;
        }
        if (remoteResult.getEx() != null) {
            throw remoteResult.getEx();
        }
        return remoteResult.getResult();
    }
}
