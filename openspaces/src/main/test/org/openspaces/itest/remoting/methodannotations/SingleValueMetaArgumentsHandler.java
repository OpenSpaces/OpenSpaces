package org.openspaces.itest.remoting.methodannotations;

import org.openspaces.remoting.MetaArgumentsHandler;
import org.openspaces.remoting.SpaceRemotingInvocation;

/**
 * @author uri
 */
public class SingleValueMetaArgumentsHandler implements MetaArgumentsHandler {
    public Object[] obtainMetaArguments(SpaceRemotingInvocation remotingEntry) {
        return new Object[] {Boolean.TRUE};
    }
}
