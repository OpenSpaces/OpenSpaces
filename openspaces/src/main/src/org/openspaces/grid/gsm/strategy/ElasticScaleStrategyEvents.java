package org.openspaces.grid.gsm.strategy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;


public class ElasticScaleStrategyEvents implements Externalizable {

    private static final long serialVersionUID = 1L;
    
    private ElasticProcessingUnitEvent[] events;
    private long nextCursor;

    // de-serialization constructor
    public ElasticScaleStrategyEvents() {
        
    }
    
    public ElasticScaleStrategyEvents(long nextCursor, ElasticProcessingUnitEvent[] events) {
        this.nextCursor = nextCursor;
        this.events = events;
    }
    
    public ElasticProcessingUnitEvent[] getEvents() {
        return events;
    }

    public long getNextCursor() {
        return nextCursor;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(nextCursor);
        out.writeObject(events);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        nextCursor = in.readLong();
        events = (ElasticProcessingUnitEvent[]) in.readObject();
    }
}
