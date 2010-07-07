package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.EventDrivenProxy;
import org.openspaces.remoting.ExecutorProxy;

/**
 * @author kimchy
 */
public class SimpleAnnotationBean {

    @EventDrivenProxy
    SimpleService eventSimpleService;

    @ExecutorProxy
    SimpleService executorSimpleService;
}
