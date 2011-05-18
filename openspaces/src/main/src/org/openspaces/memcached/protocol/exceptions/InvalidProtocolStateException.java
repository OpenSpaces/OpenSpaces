package org.openspaces.memcached.protocol.exceptions;

/**
 */
public class InvalidProtocolStateException extends Exception {
    private static final long serialVersionUID = 5256244010783276271L;

    public InvalidProtocolStateException() {
    }

    public InvalidProtocolStateException(String s) {
        super(s);
    }

    public InvalidProtocolStateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidProtocolStateException(Throwable throwable) {
        super(throwable);
    }
}