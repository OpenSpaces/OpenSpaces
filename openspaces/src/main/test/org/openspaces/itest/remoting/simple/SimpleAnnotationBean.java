package org.openspaces.itest.remoting.simple;

import org.openspaces.remoting.AsyncProxy;
import org.openspaces.remoting.SyncProxy;

/**
 * @author kimchy
 */
public class SimpleAnnotationBean {

    @AsyncProxy
    SimpleService asyncSimpleService;

    @SyncProxy
    SimpleService syncSimpleService;
}
