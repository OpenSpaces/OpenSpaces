package org.openspaces.itest.remoting.broadcast;

import org.openspaces.remoting.RemotingService;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@RemotingService
public class DefaultSimpleService implements SimpleService {

    public int sum(int value) {
        return value;
    }

    public Future<Integer> asyncSum(int value) {
        return null;
    }

    public void testException() throws MyException {
        throw new MyException();
    }

    public Future asyncTestException() throws MyException {
        return null;
    }
    
}