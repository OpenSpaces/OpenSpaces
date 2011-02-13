package org.openspaces.itest.remoting.methodannotations;

import org.openspaces.remoting.EventDrivenRemotingMethod;

import java.util.concurrent.Future;

/**
 * @author uri
 */
public interface SimpleEventDrivenRemotingService {


    @EventDrivenRemotingMethod(remoteRoutingHandler = "constantRoutingHandler")
    int testInjectedRoutingHandler(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandler = "constantRoutingHandler")
    Future<Integer> asyncTestInjectedRoutingHandler(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandlerType = ConstantRoutingHandler.class)
    int testRoutingHandlerType(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandlerType = ConstantRoutingHandler.class)
    Future<Integer> asyncTestRoutingHandlerType(int value);

    @EventDrivenRemotingMethod(remoteInvocationAspect = "returnTrueRemoteInvocationAspect", remoteRoutingHandler = "constantRoutingHandler")
    boolean testInjectedInvocationAspect();

    @EventDrivenRemotingMethod(remoteInvocationAspectType = ReturnTrueRemoteInvocationAspect.class, remoteRoutingHandler = "constantRoutingHandler")
    boolean testInvocationAspectType();

    @EventDrivenRemotingMethod(metaArgumentsHandler = "singleValueMetaArgumentsHandler", remoteRoutingHandler = "constantRoutingHandler")
    boolean testInjectedMetaArgumentsHandler();

    @EventDrivenRemotingMethod(metaArgumentsHandler = "singleValueMetaArgumentsHandler", remoteRoutingHandler = "constantRoutingHandler")
    Future<Boolean> asyncTestInjectedMetaArgumentsHandler();

    @EventDrivenRemotingMethod(metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class, remoteRoutingHandler = "constantRoutingHandler")
    boolean testMetaArgumentsHandlerType();

    @EventDrivenRemotingMethod(metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class, remoteRoutingHandler = "constantRoutingHandler")
    Future<Boolean> asyncTestMetaArgumentsHandlerType();
}