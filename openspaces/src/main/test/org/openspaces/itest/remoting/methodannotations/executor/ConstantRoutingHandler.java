package org.openspaces.itest.remoting.methodannotations.executor;

import org.openspaces.remoting.RemoteRoutingHandler;
import org.openspaces.remoting.SpaceRemotingInvocation;

/**
 * @author uri
 */
public class ConstantRoutingHandler implements RemoteRoutingHandler<Integer>{
    public Integer computeRouting(SpaceRemotingInvocation remotingEntry) {
        return 0;
    }
}
