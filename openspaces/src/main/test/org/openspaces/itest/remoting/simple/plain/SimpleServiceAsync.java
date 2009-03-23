package org.openspaces.itest.remoting.simple.plain;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleServiceAsync {

    Future say(String message);
}