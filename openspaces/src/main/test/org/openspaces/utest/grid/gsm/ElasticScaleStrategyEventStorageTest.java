package org.openspaces.utest.grid.gsm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;
import org.openspaces.grid.gsm.strategy.ElasticScaleStrategyEventStorage;
import org.openspaces.grid.gsm.strategy.ElasticScaleStrategyEvents;
import org.springframework.util.StringUtils;

public class ElasticScaleStrategyEventStorageTest {

    ElasticScaleStrategyEventStorage eventStorage ;
    @Before
    public void beforeMethod() {
        eventStorage = new ElasticScaleStrategyEventStorage(3);
        eventStorage.enqueu(createEvent("0"));
        eventStorage.enqueu(createEvent("1"));
        eventStorage.enqueu(createEvent("2"));
    }
    
    @Test
    public void testSimple() {
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(0, 3);
        Assert.assertEquals("0,1,2",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
    }
    
    @Test
    public void testMaxEventsTooBig() {
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(0, 4);
        Assert.assertEquals("0,1,2",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
    }
    
    @Test
    public void testTooLowNextCursor() {
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(-1, 4);
        Assert.assertEquals("0,1,2",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
    }
    
    @Test
    public void testMaxEventsTooSmall() {    
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(0, 2);
        Assert.assertEquals("0,1",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(2,events.getNextCursor());
    }
    
    @Test
    public void testMaxEventsTooBigNonZeroCursor() {    
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(1,3);
        Assert.assertEquals("1,2",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
    }
    
    @Test
    public void testMaxEventsOne() {    
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(2,1);
        Assert.assertEquals("2",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
    }
    
    @Test
    public void testMoreEvents() {    
        ElasticScaleStrategyEvents events = eventStorage.getEventsFromCursor(3,10);
        Assert.assertEquals("",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(3,events.getNextCursor());
        
        eventStorage.enqueu(createEvent("3"));
        events = eventStorage.getEventsFromCursor(3,10);
        Assert.assertEquals("3",StringUtils.arrayToCommaDelimitedString(events.getEvents()));
        Assert.assertEquals(4,events.getNextCursor());
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalCursor() {
        eventStorage.getEventsFromCursor(4,10);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMaxSize() {
        eventStorage.getEventsFromCursor(1,-1);
    }
    
    private ElasticProcessingUnitEvent createEvent(String message) {
        return new MyEvent(message);
    }

    class MyEvent implements ElasticProcessingUnitEvent {

        private String message;

        public MyEvent(String message) {
            this.message = message;
        }
        
        public String toString() {
            return message;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }
        
    }
}
