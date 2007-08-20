package org.openspaces.itest.events.notify.configurer;

/**
 * @author kimchy
 */
public class TestMessage {

    private String value;

    public TestMessage() {
    }

    public TestMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
