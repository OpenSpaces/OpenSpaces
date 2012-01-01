package org.openspaces.admin.pu.elastic.events;

/**
 * An interface for ESM events that denote a processing unit deployment progress change
 * @since 8.0.6
 * @author itaif
 */
public interface ElasticProcessingUnitProgressChangedEvent extends ElasticProcessingUnitEvent {

    /**
     * @return the processing units that this progress change refers to
     */
    public String getProcessingUnitName();
    
    /**
     * @return true if the progress event indicates the process (described by the event class type) is complete.
     */
    public boolean isComplete();
    
    /**
     * @return true if the event indicates the processing unit is being undeployed 
     */
    public boolean isUndeploying();
}
