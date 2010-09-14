package org.openspaces.remoting;

/**
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public interface SpaceRemotingEntry extends SpaceRemotingInvocation, SpaceRemotingResult, Cloneable {

    void setInvocation(Boolean invocation);
    
    void setInstanceId(Integer instanceId);
    
    void setLookupName(String lookupName);
    
    void setRouting(Object routing);

    void setMetaArguments(Object[] metaArguments);

    Boolean getOneWay();
    void setOneWay(Boolean oneWay);
        
    void setFifo(boolean isfifo);
    
    SpaceRemotingEntry buildResult(Object obj);
    
    SpaceRemotingEntry buildResultTemplate();
    
    Object clone() throws CloneNotSupportedException;
}
