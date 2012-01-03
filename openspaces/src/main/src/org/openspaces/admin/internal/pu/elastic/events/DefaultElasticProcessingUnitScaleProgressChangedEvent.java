package org.openspaces.admin.internal.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitScaleProgressChangedEvent;


/**
 * Describes the progress of a processing unit during scale 
 * @author itaif
 *
 */
public class DefaultElasticProcessingUnitScaleProgressChangedEvent 
    extends AbstractElasticProcessingUnitProgressChangedEvent 
    implements ElasticProcessingUnitScaleProgressChangedEvent  {

private static final long serialVersionUID = 1L;
           
    /**
     * de-serialization constructor
     */
    public DefaultElasticProcessingUnitScaleProgressChangedEvent() {
    }

    public DefaultElasticProcessingUnitScaleProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }
}
