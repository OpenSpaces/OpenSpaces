package org.openspaces.admin.gsa.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitFailureEvent;

import com.gigaspaces.internal.io.IOUtils;

public class ElasticGridServiceAgentProvisioningFailureEvent implements ElasticProcessingUnitFailureEvent {

private static final long serialVersionUID = 1L;
    
    private String failureDescription;
    private String[] processingUnitNames;
    
    /**
     * de-serialization constructor
     */
    public ElasticGridServiceAgentProvisioningFailureEvent() {
    }
    
    public ElasticGridServiceAgentProvisioningFailureEvent(String failureDescription, String[] processingUnitNames) {
        this.failureDescription = failureDescription;
        this.processingUnitNames = processingUnitNames;
    }
    
    @Override
    public String[] getProcessingUnitNames() {
        return processingUnitNames;
    }

    @Override
    public String getFailureDescription() {
        return failureDescription;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeString(out, failureDescription);
        IOUtils.writeStringArray(out, processingUnitNames);
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        failureDescription = IOUtils.readString(in);
        processingUnitNames = IOUtils.readStringArray(in);
    }

}
