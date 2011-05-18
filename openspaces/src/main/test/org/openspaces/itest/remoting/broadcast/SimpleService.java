package org.openspaces.itest.remoting.broadcast;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleService {

    int sum(int value);

    Future<Integer> asyncSum(int value);

    void testException() throws MyException;

    Future asyncTestException() throws MyException;

    class MyException extends RuntimeException {

        private static final long serialVersionUID = 5612556672198167469L;

    }
}