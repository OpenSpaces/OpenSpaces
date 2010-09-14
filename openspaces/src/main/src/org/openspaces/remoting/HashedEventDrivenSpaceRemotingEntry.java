package org.openspaces.remoting;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kimchy (shay.banon)
 * @deprecated
 */
@Deprecated
public class HashedEventDrivenSpaceRemotingEntry extends EventDrivenSpaceRemotingEntry
    implements HashedSpaceRemotingEntry {

    public RemotingUtils.MethodHash methodHash;

    public RemotingUtils.MethodHash getMethodHash() {
        return methodHash;
    }

    public HashedSpaceRemotingEntry buildInvocation(String lookupName, String methodName, RemotingUtils.MethodHash methodHash, Object[] arguments) {
        setResult(null);
        setException(null);
        setInvocation(Boolean.TRUE);
        setLookupName(lookupName);
        setMethodName(methodName);
        this.methodHash = methodHash;
        setArguments(arguments);
        return this;
    }

    @Override
    public SpaceRemotingEntry buildResultTemplate() {
        methodHash = null;
        return super.buildResultTemplate();
    }

    @Override
    public SpaceRemotingEntry buildResult(Throwable e) {
        methodHash = null;
        return super.buildResult(e);
    }

    @Override
    public HashedSpaceRemotingEntry buildResult(Object result) {
        methodHash = null;
        return (HashedSpaceRemotingEntry) super.buildResult(result);
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        if (in.readBoolean()) {
            methodHash = new RemotingUtils.MethodHash();
            methodHash.readExternal(in);
        }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (methodHash == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            methodHash.writeExternal(out);
        }
    }
}
