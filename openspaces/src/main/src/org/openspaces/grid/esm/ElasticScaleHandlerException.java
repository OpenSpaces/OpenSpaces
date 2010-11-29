package org.openspaces.grid.esm;


/**
 * An abstraction for any runtime exception that could be raised by the elastic scale handler.
 * @author itaif
 *
 */
public class ElasticScaleHandlerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ElasticScaleHandlerException(String message) {
        super(message);
    }

    public ElasticScaleHandlerException(String message, Throwable cause) {
        super(message,cause);
    }

}