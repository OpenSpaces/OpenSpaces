package org.openspaces.itest.remoting.methodannotations.executor;

import java.util.concurrent.Future;

/**
 * @author uri
 */
public class DefaultSimpleService implements SimpleService {

    public int sumWithInjectedReducer(int value) {
        return value;
    }

    public Future<Integer> asyncSumWithInjectedReducer(int value) {
        return null;
    }

    public int sumWithReducerType(int value) {
        return value;
    }

    public Future<Integer> asyncSumWithReducerType(int value) {
        return null;
    }

    public int testInjectedRoutingHandler(int value) {
        return value;
    }

    public Future<Integer> asyncTestInjectedRoutingHandler(int value) {
        return null;
    }

    public int testRoutingHandlerType(int value) {
        return value;
    }

    public Future<Integer> asyncTestRoutingHandlerType(int value) {
        return null;
    }

    public boolean testInjectedInvocationAspect() {
        return false;
    }

    public boolean testInvocationAspectType() {
        return false;
    }

    public boolean testInjectedMetaArgumentsHandler() {
        return false;
    }

    public Future<Boolean> asyncTestInjectedMetaArgumentsHandler() {
        return null;
    }

    public boolean testMetaArgumentsHandlerType() {
        return false;
    }

    public Future<Boolean> asyncTestMetaArgumentsHandlerType() {
        return null;
    }
}