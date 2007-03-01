package org.openspaces.remoting;

/**
 * @author kimchy
 */
public class ServiceMethodNotFoundSpaceRemotingException extends SpaceRemotingException {

    private String serviceMethodName;

    public ServiceMethodNotFoundSpaceRemotingException(String serviceMethodName) {
        super("Service method [" + serviceMethodName + "] not found");
        this.serviceMethodName = serviceMethodName;
    }

    public ServiceMethodNotFoundSpaceRemotingException(String serviceMethodName, Throwable cause) {
        super("Service method [" + serviceMethodName + "] not found", cause);
        this.serviceMethodName = serviceMethodName;
    }

    public String getServiceMethodName() {
        return this.serviceMethodName;
    }

}
