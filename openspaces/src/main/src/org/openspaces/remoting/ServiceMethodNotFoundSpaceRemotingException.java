package org.openspaces.remoting;

import org.openspaces.core.GigaSpaceException;

/**
 * @author kimchy
 */
public class ServiceMethodNotFoundSpaceRemotingException extends GigaSpaceException {

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
