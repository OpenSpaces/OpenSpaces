package org.openspaces.itest.remoting.simple;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleServiceAsync {

    Future say(String message);
}