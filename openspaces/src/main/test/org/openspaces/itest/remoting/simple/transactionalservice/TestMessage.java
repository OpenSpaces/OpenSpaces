package org.openspaces.itest.remoting.simple.transactionalservice;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * @author kimchy
 */
@SpaceClass
public class TestMessage {

    private String message;

    public TestMessage(String message) {
        this.message = message;
    }

    public TestMessage() {
    }

    @SpaceId(autoGenerate = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}