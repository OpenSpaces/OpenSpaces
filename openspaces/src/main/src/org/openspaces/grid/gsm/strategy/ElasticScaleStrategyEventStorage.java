package org.openspaces.grid.gsm.strategy;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

public class ElasticScaleStrategyEventStorage {

    class CircularList {
        
        private final Map<Long,ElasticProcessingUnitEvent> items = new HashMap<Long,ElasticProcessingUnitEvent>();
        private final int maxSize;
        private long firstIndex;
        private long nextIndex;
        
        public CircularList(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("maxSize must be positive");
            }
            this.maxSize = maxSize;
            nextIndex = 0;
            firstIndex = 0;
        }

        public void add(ElasticProcessingUnitEvent event) {
            
            items.put(nextIndex,event);
            nextIndex++;
            while (getSize() > maxSize) {
                removeFirst();
            }
        }
    
        private int getSize() {
            return (int)(nextIndex - firstIndex);
        }

        private void removeFirst() {
            items.remove(firstIndex);
            firstIndex++;
        }

        public ElasticProcessingUnitEvent[] snapshot(long startIndex, int maxSize) {
            if (startIndex < firstIndex) {
                throw new IllegalArgumentException("startIndex must be bigger than firstIndex");
            }
            if (maxSize< 0) {
                throw new IllegalArgumentException("maxSize must be positive");
            }
            if (startIndex > nextIndex) {
                throw new IllegalArgumentException("startIndex cannot be bigger than extIndex");
            }
            int size = (int)(nextIndex-startIndex);
            if (size > maxSize) {
                size = maxSize;
            }
            
            ElasticProcessingUnitEvent[] snapshot = new ElasticProcessingUnitEvent[size];
            for (int i = 0 ; i < size ; i++) {
                snapshot[i]= items.get(startIndex+i);
            }
            return snapshot;
        }

        public long getNextIndex() {
            return nextIndex;
        }

        public long getFirstIndex() {
            return firstIndex;
        }
    }
    
    private final CircularList list;
        
    public ElasticScaleStrategyEventStorage(int maxNumberOfEvents) {
        list = new CircularList(maxNumberOfEvents);
    }
    
    /**
     * @return the events starting with the event after the event with the specified cursor. 
     * Specify a zero cursor to start with the earliest event in the event storage
     * @exception IllegalArgumentException if the cursor is not zero or not retrieved from {@link ElasticScaleStrategyEvents#getNextCursor()} 
     */
    public ElasticScaleStrategyEvents getEventsFromCursor(long cursor, int maxNumberOfEvents) {
        
        if (cursor > list.getNextIndex()) {
            throw new IllegalArgumentException("Invalid cursor " + cursor);
        }
        
        if (cursor < list.getFirstIndex()) {
            cursor = list.getFirstIndex();
        }
        
        ElasticProcessingUnitEvent[] events = list.snapshot(cursor, maxNumberOfEvents);
        return new ElasticScaleStrategyEvents(cursor+events.length, events);
    }

    public void enqueu(ElasticProcessingUnitEvent event) {
        list.add(event);
    }
}
