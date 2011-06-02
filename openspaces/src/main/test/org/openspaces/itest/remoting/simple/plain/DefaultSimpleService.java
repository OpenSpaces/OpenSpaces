package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.RemotingService;

import java.util.List;
import java.util.Map;
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

    public String overloaded(List list) {
        return "L" + list.size();
    }

    public String overloaded(Map map) {
        return "M" + map.size();
    }
    
    public Future<String> asyncOverloaded(List list) {
        return null;
    }

    public Future<String> asyncOverloaded(Map map) {
        return null;
    }

    public String superSay(String message) {
        return "Super SAY " + message;
    }

}
