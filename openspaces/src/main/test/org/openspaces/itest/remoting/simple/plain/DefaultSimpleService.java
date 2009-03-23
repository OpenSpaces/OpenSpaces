package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.RemotingService;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@RemotingService
public class DefaultSimpleService implements SimpleService {

    public String say(String message) {
        return "SAY " + message;
    }

    public Future asyncSay(String message) {
        return null;
    }

    public boolean wire(WiredParameter wiredParameter) {
        return wiredParameter.gigaSpace != null;
    }

    public void testException() throws MyException {
        throw new MyException();
    }

    public Future asyncTestException() throws MyException {
        return null;
    }
}
