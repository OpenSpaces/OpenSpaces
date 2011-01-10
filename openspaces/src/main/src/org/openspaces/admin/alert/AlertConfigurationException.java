package org.openspaces.admin.alert;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.bean.BeanConfigException;

/**
 * An alert configuration exception; the {@link #getCause()} can be one the subclasses of
 * {@link BeanConfigException}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertConfigurationException extends AdminException {

    private static final long serialVersionUID = 1L;

    public AlertConfigurationException(String message) {
        super(message);
    }

    public AlertConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }


}
