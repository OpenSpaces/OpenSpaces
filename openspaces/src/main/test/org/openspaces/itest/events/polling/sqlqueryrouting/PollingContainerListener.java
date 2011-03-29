package org.openspaces.itest.events.polling.sqlqueryrouting;

import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.itest.events.polling.sqlqueryrouting.SQLQueryRoutingPollingContainerTest.MyClass;

import com.j_spaces.core.client.SQLQuery;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
@Polling
public class PollingContainerListener {

    private volatile int counter = 0;
    private Object routing = null;
    
    public PollingContainerListener() {
    }
    
    public PollingContainerListener(Object routing) {
        this.routing = routing;
    }
    
    @EventTemplate
    public Object getTemplate() {
        SQLQuery<MyClass> query = new SQLQuery<MyClass>(MyClass.class, "processed = false");
        query.setRouting(routing);
        return query;
    }
    
    public int getCount() {
        return counter;
    }
    
    @SpaceDataEvent
    public void event(MyClass value) {
        value.setProcessed(true);
        System.out.println("PollingContainer #" + routing + ": " + value);
        counter++;
    }
    
}
