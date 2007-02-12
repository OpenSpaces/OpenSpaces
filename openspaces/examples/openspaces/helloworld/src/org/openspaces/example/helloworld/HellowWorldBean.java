package org.openspaces.example.helloworld;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.openspaces.core.GigaSpace;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomData;

/**
 * @author kimchy
 */
public class HellowWorldBean implements InitializingBean {

    private GigaSpace gigaSpace;

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(gigaSpace, "gigaSpace property is required");

        RandomData randomData = new RandomDataImpl();

        Message message = new Message("Hello World " + randomData.nextLong(0l, 100l));
        System.out.println("Writing Message [" + message.getMessage() + "]");
        gigaSpace.write(message);

        message = (Message) gigaSpace.take(new Message());
        System.out.println("Took Message [" + message.getMessage() + "]");
    }
}
