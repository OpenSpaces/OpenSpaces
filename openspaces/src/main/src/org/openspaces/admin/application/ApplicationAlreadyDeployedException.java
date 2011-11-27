package org.openspaces.admin.application;

import org.openspaces.admin.AdminException;

public class ApplicationAlreadyDeployedException extends AdminException {

    private static final long serialVersionUID = 1L;

    public ApplicationAlreadyDeployedException(String applicationName) {
        super(createMessage(applicationName));
    }

    private static String createMessage(String applicationName) {
        return "application [" + applicationName + "] already deployed";
    }

    public ApplicationAlreadyDeployedException(String applicationName, Throwable cause) {
        super(createMessage(applicationName), cause);
    }
}
