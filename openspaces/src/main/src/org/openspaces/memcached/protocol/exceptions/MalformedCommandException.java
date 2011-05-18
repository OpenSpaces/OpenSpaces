package org.openspaces.memcached.protocol.exceptions;

/**
 */
public class MalformedCommandException extends ClientException {
    private static final long serialVersionUID = 968285939188557080L;

    public MalformedCommandException() {
    }

    public MalformedCommandException(String s) {
        super(s);
    }

    public MalformedCommandException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MalformedCommandException(Throwable throwable) {
        super(throwable);
    }
}