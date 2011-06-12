package org.openspaces.pu.service;

/**
 * @author uri
 * @since 8.0.3
 */
public class InvocableServiceLookupFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvocableServiceLookupFailureException(String message) {
        super(message);
    }
}
