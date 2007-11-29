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

import java.lang.reflect.InvocationTargetException;

/**
 * A service execution callback allows to wrap the execution of "server side" service
 * with different callbacks.
 *
 * <p>A typical scenario is to apply "aspect" logic before executing a service (such as
 * security checks) usually using {@link org.openspaces.remoting.SpaceRemotingInvocation#getMetaArguments()}.
 *
 * @author kimchy
 */
public interface ServiceExecutionCallback {

    /**
     * Called before the actual service is executed.
     */
    void beforeInvocation(SpaceRemotingInvocation invocation) throws InvocationTargetException;

    /**
     * Called after the service is executed. Returns the value thatwill be returned to the caller.
     */
    Object afterInvocation(SpaceRemotingInvocation invocation, Object result) throws InvocationTargetException;
}
