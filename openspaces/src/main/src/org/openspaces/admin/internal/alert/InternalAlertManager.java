package org.openspaces.admin.internal.alert;

import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;

public interface InternalAlertManager extends AlertManager {
	
    /**
     * @return the weakly typed configuration API. 
     */
    BeanConfigPropertiesManager getBeanConfigPropertiesManager();
    
	AlertRepository getAlertRepository();
}
