package org.openspaces.example.helloworld;

/**
 * A simple message object that is written to the space. Note, this
 * message uses GigaSpaces support for POJO entries. With GigaSpaces
 * support for POJO entries there is no need even to mark the class
 * using annotations or xml though further customization is allowed
 * when using it. 
 *
 * @author kimchy
 */
public class Message {

    private String message;

    public Message() {
    }

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
