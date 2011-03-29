package org.openspaces.itest.events.polling.sqlqueryrouting;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * Test SQLQuery.setRouting with two polling containers (1 for each partition).
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class SQLQueryRoutingPollingContainerTest extends AbstractDependencyInjectionSpringContextTests {
    
    protected GigaSpace gigaSpace;
    
    public SQLQueryRoutingPollingContainerTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/events/polling/sqlqueryrouting/sqlquery-routing.xml"};
    }
    
    public void testSqlQueryRouting() throws Exception {
        PollingContainerListener listener1 = new PollingContainerListener(Integer.valueOf(1));
        PollingContainerListener listener2 = new PollingContainerListener(Integer.valueOf(2));
        SimplePollingEventListenerContainer container1 = new SimplePollingContainerConfigurer(gigaSpace).eventListenerAnnotation(listener1).pollingContainer();
        container1.start();
        SimplePollingEventListenerContainer container2 = new SimplePollingContainerConfigurer(gigaSpace).eventListenerAnnotation(listener2).pollingContainer();
        container2.start();
        
        gigaSpace.write(new MyClass(false, 1));
        gigaSpace.write(new MyClass(false, 2));
        
        Thread.sleep(500);
        
        assertEquals(1, listener1.getCount());
        assertEquals(1, listener2.getCount());
    }
    
    
    public static class MyClass {
        private String id;
        private Boolean processed;
        private Integer routing;
        
        public MyClass() {
        }
        public MyClass(Boolean processed, Integer routing) {
            this.processed = processed;
            this.setRouting(routing);
        }
        @SpaceId(autoGenerate = true)
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public Boolean getProcessed() {
            return processed;
        }
        public void setProcessed(Boolean processed) {
            this.processed = processed;
        }
        
        @Override
        public String toString() {
            return "MyClass [id=" + id + ", processed=" + processed + "]";
        }
        public void setRouting(Integer routing) {
            this.routing = routing;
        }
        @SpaceRouting
        public Integer getRouting() {
            return routing;
        }
        
    }
    
    
    
}
