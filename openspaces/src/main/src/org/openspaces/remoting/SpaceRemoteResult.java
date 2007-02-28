package org.openspaces.remoting;

import net.jini.core.entry.Entry;

/**
 * @author kimchy
 */
public class SpaceRemoteResult implements Entry {

    public String invocationId;

    public Object result;

    public Exception ex;

    public Integer routing;

    public SpaceRemoteResult() {

    }

    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.routing = remoteInvocation.routing;
    }

    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, Exception ex) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.ex = ex;
        this.routing = remoteInvocation.routing;
    }

    public SpaceRemoteResult(SpaceRemoteInvocation remoteInvocation, Object result) {
        this.invocationId = remoteInvocation.__getEntryInfo().m_UID;
        this.result = result;
        this.routing = remoteInvocation.routing;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing", "invocationId"};
    }
}
