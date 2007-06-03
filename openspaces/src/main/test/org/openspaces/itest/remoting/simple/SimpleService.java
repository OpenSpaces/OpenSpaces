package org.openspaces.itest.remoting.simple;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleService {

    String say(String message);

    Future asyncSay(String message);
}
