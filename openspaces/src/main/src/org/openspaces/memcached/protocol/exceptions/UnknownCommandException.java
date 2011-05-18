package org.openspaces.memcached.protocol.exceptions;

/**
 */
public class UnknownCommandException extends ClientException {

    private static final long serialVersionUID = 322896615625776078L;

    public UnknownCommandException() {
    }

    public UnknownCommandException(String s) {
        super(s);
    }

    public UnknownCommandException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnknownCommandException(Throwable throwable) {
        super(throwable);
    }
}