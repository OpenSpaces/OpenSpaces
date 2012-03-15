/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.remoting.methodannotations;

import org.openspaces.remoting.ServiceExecutionAspect;
import org.openspaces.remoting.SpaceRemotingInvocation;

import java.lang.reflect.InvocationTargetException;

/**
 * @author uri
 */
public class ReturnFirstMetaArgumentExecutionAspect implements ServiceExecutionAspect{
    public Object invoke(SpaceRemotingInvocation invocation, MethodInvocation method, Object service) throws InvocationTargetException, IllegalAccessException {
        Object[] metaArguments = invocation.getMetaArguments();
        if (metaArguments != null) {
            return metaArguments[0];
        } else {
            return method.invoke(service, invocation.getArguments());
        }
    }
}
