package org.openspaces.remoting;

/**
 * @author kimchy
 */
public class ServiceNotFoundSpaceRemotingException extends SpaceRemotingException {

    private String serviceName;

    public ServiceNotFoundSpaceRemotingException(String serviceName) {
        super("Service [" + serviceName + "] not found");
        this.serviceName = serviceName;
    }

    public ServiceNotFoundSpaceRemotingException(String serviceName, Throwable cause) {
        super("Service [" + serviceName + "] not found", cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

}
