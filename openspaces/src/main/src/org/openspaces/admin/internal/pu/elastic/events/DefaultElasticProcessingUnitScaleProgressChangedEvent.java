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
     * de-serialization/reflection constructor
     */
    public DefaultElasticProcessingUnitScaleProgressChangedEvent() {
    }
}
