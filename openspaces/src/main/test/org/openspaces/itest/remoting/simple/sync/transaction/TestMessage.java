package org.openspaces.itest.remoting.simple.sync.transaction;

/**
 * @author kimchy
 */
public class TestMessage {

    private String message;

    public TestMessage(String message) {
        this.message = message;
    }

    public TestMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
