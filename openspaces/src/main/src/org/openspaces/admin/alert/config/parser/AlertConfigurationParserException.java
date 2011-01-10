package org.openspaces.admin.alert.config.parser;

import org.openspaces.admin.AdminException;

/**
 * An exception thrown on a parsing error from {@link AlertConfigurationParser}.
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertConfigurationParserException extends AdminException {

    private static final long serialVersionUID = 1L;

    public AlertConfigurationParserException(String message) {
        super(message);
    }
    
    public AlertConfigurationParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
