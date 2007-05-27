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

package org.openspaces.example.data.feeder.support;

import org.openspaces.example.data.common.Data;
import org.openspaces.remoting.RemoteRoutingHandler;
import org.openspaces.remoting.SpaceRemoteInvocation;

/**
 * An intenral interface of OpenSpaces Remoting support allows to control the
 * routing field used when working against a partitioned space. In this case,
 * when processData API is invoked, we set the routing field to be the Data
 * object type property. In case of sayData API, wil do nothing, which will
 * use OpenSpaces Remoting default routing calculation.
 *
 * @author kimchy
 */
public class DataRemoteRoutingHandler implements RemoteRoutingHandler {

    public void setRemoteInvocationRouting(SpaceRemoteInvocation remoteInvocation) {
        if (remoteInvocation.getMethodName().equals("processData")) {
            Data data = (Data) remoteInvocation.getArguments()[0];
            remoteInvocation.setRouting(data.getType().intValue());
        }
    }
}
