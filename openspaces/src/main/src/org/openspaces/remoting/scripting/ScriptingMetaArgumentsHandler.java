package org.openspaces.remoting.scripting;

import org.openspaces.remoting.MetaArgumentsHandler;
import org.openspaces.remoting.SpaceRemotingInvocation;

/**
 *
 * 
 * @author kimchy
 */
public class ScriptingMetaArgumentsHandler implements MetaArgumentsHandler {

    public Object[] obtainMetaArguments(SpaceRemotingInvocation remotingEntry) {
        return ((Script) remotingEntry.getArguments()[0]).getMetaArguments();
    }
}
