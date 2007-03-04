package org.openspaces.example.data.feeder.support;

import org.openspaces.example.data.common.Data;
import org.openspaces.remoting.RemoteRoutingHandler;
import org.openspaces.remoting.SpaceRemoteInvocation;

/**
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
