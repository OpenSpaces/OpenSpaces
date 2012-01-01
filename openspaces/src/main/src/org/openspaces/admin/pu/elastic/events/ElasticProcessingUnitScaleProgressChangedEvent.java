package org.openspaces.admin.pu.elastic.events;


/**
 * Describes the progress of a processing unit during scale 
 * @author itaif
 *
 */
public class ElasticProcessingUnitScaleProgressChangedEvent extends AbstractElasticProcessingUnitProgressChangedEvent {

private static final long serialVersionUID = 1L;
           
    /**
     * de-serialization constructor
     */
    public ElasticProcessingUnitScaleProgressChangedEvent() {
    }

    public ElasticProcessingUnitScaleProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }
}
