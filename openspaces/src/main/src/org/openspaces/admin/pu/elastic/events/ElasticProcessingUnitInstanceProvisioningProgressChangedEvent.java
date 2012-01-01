package org.openspaces.admin.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;


public class ElasticProcessingUnitInstanceProvisioningProgressChangedEvent extends AbstractElasticProcessingUnitProgressChangedEvent {

       private static final long serialVersionUID = 1L;
       
       /**
        * de-serialization constructor
        */
       public ElasticProcessingUnitInstanceProvisioningProgressChangedEvent() {
       }
       
       public ElasticProcessingUnitInstanceProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
           super(isComplete, isUndeploying, processingUnitName);
       }

}
