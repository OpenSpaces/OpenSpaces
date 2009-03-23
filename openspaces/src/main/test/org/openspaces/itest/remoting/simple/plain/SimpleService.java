package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.AutowireArguments;
import org.openspaces.remoting.Routing;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@AutowireArguments
public interface SimpleService {

    String say(@Routing String message);

    Future<String> asyncSay(String message);

    boolean wire(WiredParameter wiredParameter);

    void testException() throws MyException;

    Future asyncTestException() throws MyException;
    
    class MyException extends RuntimeException {

    }
}
