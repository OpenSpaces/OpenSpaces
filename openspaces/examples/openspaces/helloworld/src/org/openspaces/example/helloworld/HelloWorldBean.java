package org.openspaces.example.helloworld;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>A simple bean that is run within a Spring context that acts
 * as the processing unit context as well (when executed within a
 * processing unit container).
 *
 * <p>Gets injected with a {@link org.openspaces.core.GigaSpace} interface
 * which provides a simpler programming model on top of JavaSpaces core interface.
 *
 * <p>The bean writes and takes a {@link org.openspaces.example.helloworld.Message}
 * object from the space using the injected {@link org.openspaces.core.GigaSpace}
 * when the bean starts up (using Spring {@link org.springframework.beans.factory.InitializingBean}
 * callback). This means that spring context is started or the processing unit is
 * deployed, this code will be executed.
 *
 * @author kimchy
 */
public class HelloWorldBean implements InitializingBean {

    private GigaSpace gigaSpace;

    /**
     * A GigaSpace interface used to interact with the space. Injected
     * by Spring.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * A spring callback invoked when the container starts. Uses
     * the injected GigaSpace to write and then take a single Message
     * object.
     */
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
