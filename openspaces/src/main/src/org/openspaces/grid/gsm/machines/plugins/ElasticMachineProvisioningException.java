package org.openspaces.grid.gsm.machines.plugins;


/**
 * An abstraction for any runtime exception that could be raised by an elastic machine provisioning implementation.
 * @author itaif
 *
 */
public class ElasticMachineProvisioningException extends Exception {

    private static final long serialVersionUID = 1L;

    public ElasticMachineProvisioningException(String message) {
        super(message);
    }

    public ElasticMachineProvisioningException(String message, Throwable cause) {
        super(message,cause);
    }

}