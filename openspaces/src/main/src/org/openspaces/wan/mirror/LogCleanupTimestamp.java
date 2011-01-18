package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

public class LogCleanupTimestamp {

    private long timeStamp;

    
    public LogCleanupTimestamp() {
    
    }

    public LogCleanupTimestamp(long timeStamp) {
        super();
        this.timeStamp = timeStamp;
    }

    @SpaceId
    public int getId() { 
        return 1;  // There can only be one timestamp in the space.
    }
    
    public void setId(int id) {
        
    }
    
    @SpaceProperty(nullValue="0")
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
}
