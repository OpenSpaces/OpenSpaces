package org.openspaces.remoting;

/**
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public interface HashedSpaceRemotingEntry extends SpaceRemotingEntry {

    public RemotingUtils.MethodHash getMethodHash();
    
    HashedSpaceRemotingEntry buildInvocation(String lookupName, String methodName, 
            RemotingUtils.MethodHash methodHash, Object[] arguments);
}
