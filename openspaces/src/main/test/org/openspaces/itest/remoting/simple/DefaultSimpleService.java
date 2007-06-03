package org.openspaces.itest.remoting.simple;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class DefaultSimpleService implements SimpleService {

    public String say(String message) {
        return "SAY " + message;
    }

    public Future asyncSay(String message) {
        return null;
    }
}
