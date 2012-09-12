/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
