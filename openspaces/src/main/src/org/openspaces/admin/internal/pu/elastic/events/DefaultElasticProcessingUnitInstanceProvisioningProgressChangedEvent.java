package org.openspaces.admin.internal.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitInstanceProvisioningProgressChangedEvent;


public class DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent 
        extends AbstractElasticProcessingUnitProgressChangedEvent 
        implements ElasticProcessingUnitInstanceProvisioningProgressChangedEvent {

       private static final long serialVersionUID = 1L;
       
       /**
        * de-serialization constructor
        */
       public DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent() {
       }
}
