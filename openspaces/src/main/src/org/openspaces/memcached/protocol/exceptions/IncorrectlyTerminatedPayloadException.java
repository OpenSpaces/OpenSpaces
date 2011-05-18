package org.openspaces.memcached.protocol.exceptions;

/**
 */
public class IncorrectlyTerminatedPayloadException extends ClientException {
    private static final long serialVersionUID = 1009982290187803006L;

    public IncorrectlyTerminatedPayloadException() {
    }

    public IncorrectlyTerminatedPayloadException(String s) {
        super(s);
    }

    public IncorrectlyTerminatedPayloadException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public IncorrectlyTerminatedPayloadException(Throwable throwable) {
        super(throwable);
    }
}