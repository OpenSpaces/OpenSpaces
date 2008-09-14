package org.openspaces.itest.executor.eventcontainer;

import com.gigaspaces.async.AsyncFuture;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.support.RegisterEventContainerTask;
import org.openspaces.events.support.UnregisterEventContainerTask;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class EventContainerExecutorTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    protected GigaSpace distGigaSpace;

    private volatile boolean receivedEvent;

    public EventContainerExecutorTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/executor/eventcontainer/context.xml"};
    }

    protected void onSetUp() throws Exception {
        distGigaSpace.clear(null);
    }

    protected void onTearDown() throws Exception {
        distGigaSpace.clear(null);
    }

    public void testDynamicRegistrationOfEvents() throws Exception {
        gigaSpace1.write(new Object());
        Thread.sleep(200);
        assertFalse(receivedEvent);
        AsyncFuture future = distGigaSpace.execute(new RegisterEventContainerTask(new DynamicEventListener()), 0);
        future.get(500, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        assertTrue(receivedEvent);

        receivedEvent = false;
        future = distGigaSpace.execute(new UnregisterEventContainerTask("test"), 0);
        future.get(500, TimeUnit.MILLISECONDS);
        gigaSpace1.write(new Object());
        Thread.sleep(500);
        assertFalse(receivedEvent);
    }

    @EventDriven
    @Polling(value = "test", gigaSpace = "gigaSpace1")
    public class DynamicEventListener {

        @EventTemplate
        public Object template() {
            return new Object();
        }

        @SpaceDataEvent
        public void onEvent() {
            receivedEvent = true;
        }
    }
}