package org.openspaces.remoting;

/**
 * A Space remoting exception caused by not finding the method within the service to invoke.
 * 
 * @author kimchy
 */
public class ServiceMethodNotFoundSpaceRemotingException extends SpaceRemotingException {

    private static final long serialVersionUID = 2471358257471257661L;

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
