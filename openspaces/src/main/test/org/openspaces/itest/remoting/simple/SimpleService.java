package org.openspaces.itest.remoting.simple;

import org.openspaces.remoting.Routing;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleService {

    String say(@Routing String message);

    Future asyncSay(String message);
}
