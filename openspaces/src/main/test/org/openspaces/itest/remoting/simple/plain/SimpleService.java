package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.AutowireArguments;
import org.openspaces.remoting.Routing;

import java.util.List;
import java.util.Map;
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

    String overloaded(List list);

    Future<String> asyncOverloaded(List list);

    String overloaded(Map map);

    Future<String> asyncOverloaded(Map map);

    class MyException extends RuntimeException {

        private static final long serialVersionUID = 4286999982167551193L;

    }
}
