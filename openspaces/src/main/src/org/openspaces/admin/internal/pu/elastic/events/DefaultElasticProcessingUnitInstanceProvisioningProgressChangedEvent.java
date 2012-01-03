package org.openspaces.admin.internal.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitInstanceProvissioningProgressChangedEvent;


public class DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent 
        extends AbstractElasticProcessingUnitProgressChangedEvent 
        implements ElasticProcessingUnitInstanceProvissioningProgressChangedEvent {

       private static final long serialVersionUID = 1L;
       
       /**
        * de-serialization constructor
        */
       public DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent() {
       }
       
       public DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
           super(isComplete, isUndeploying, processingUnitName);
       }

}
