package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.AsyncProxy;
import org.openspaces.remoting.ExecutorProxy;
import org.openspaces.remoting.SyncProxy;

/**
 * @author kimchy
 */
public class SimpleAnnotationBean {

    @AsyncProxy
    SimpleService asyncSimpleService;

    @SyncProxy
    SimpleService syncSimpleService;

    @ExecutorProxy
    SimpleService executorSimpleService;
}
