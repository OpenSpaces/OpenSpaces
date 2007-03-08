package org.openspaces.example.data.processor;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A simple bean printing out messages when it is loaded and when it is destroyed. Used
 * to demonstrate {@link org.openspaces.core.space.mode.SpaceModeContextLoader}.
 *
 * @author kimchy
 */
public class SpaceModeContextBean implements InitializingBean, DisposableBean {

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    public void afterPropertiesSet() throws Exception {
        System.out.println("SPACE MODE BEAN LOADED, SPACE [" + gigaSpace + "]");
    }

    public void destroy() throws Exception {
        System.out.println("SPACE MODE BEAN DESTROYED, SPACE [" + gigaSpace + "]");
    }
}
