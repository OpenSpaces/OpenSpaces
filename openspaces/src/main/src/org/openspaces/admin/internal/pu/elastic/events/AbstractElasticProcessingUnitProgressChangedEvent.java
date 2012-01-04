package org.openspaces.admin.internal.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jini.rio.monitor.event.Event;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitProgressChangedEvent;

import com.gigaspaces.internal.io.IOUtils;

public abstract class AbstractElasticProcessingUnitProgressChangedEvent implements ElasticProcessingUnitProgressChangedEvent , Event{
    private boolean isComplete;
    private String processingUnitName;
    private boolean isUndeploying;
    
    /**
     * de-serialization/reflection constructor
     */
    public AbstractElasticProcessingUnitProgressChangedEvent() {
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * @return the processing units that requires new machines
     */
    @Override
    public String getProcessingUnitName() {
        return processingUnitName;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(isComplete);
        out.writeBoolean(isUndeploying);
        IOUtils.writeString(out, processingUnitName);
        
    }

    @Override
    public boolean isUndeploying() {
        return isUndeploying;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        isComplete = in.readBoolean();
        isUndeploying = in.readBoolean();
        processingUnitName = IOUtils.readString(in);
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public void setUndeploying(boolean isUndeploying) {
        this.isUndeploying = isUndeploying;
    }

    public void setProcessingUnitName(String processingUnitName) {
        this.processingUnitName = processingUnitName;
    }
}
