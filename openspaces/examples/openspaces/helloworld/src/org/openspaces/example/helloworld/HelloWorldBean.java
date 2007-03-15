package org.openspaces.example.helloworld;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public class HelloWorldBean implements InitializingBean {

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

        message = gigaSpace.take(new Message());
        System.out.println("Took Message [" + message.getMessage() + "]");
    }
}
