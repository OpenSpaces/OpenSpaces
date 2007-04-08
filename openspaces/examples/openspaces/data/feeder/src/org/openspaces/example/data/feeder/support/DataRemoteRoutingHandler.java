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
