package org.openspaces.admin.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

public abstract class AbstractElasticProcessingUnitProgressChangedEvent implements ElasticProcessingUnitProgressChangedEvent {
    private boolean isComplete;
    private String processingUnitName;
    private boolean isUndeploying;
    
    /**
     * de-serialization constructor
     */
    public AbstractElasticProcessingUnitProgressChangedEvent() {
    }
    
    public AbstractElasticProcessingUnitProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        this.isComplete = isComplete;
        this.isUndeploying = isUndeploying;
        this.processingUnitName = processingUnitName;
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
}
