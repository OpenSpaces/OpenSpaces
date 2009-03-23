package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author kimchy
 */
public class WiredParameter {

    @Autowired
    transient GigaSpace gigaSpace;
}
