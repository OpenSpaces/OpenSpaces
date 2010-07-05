package org.openspaces.admin.pu;

import org.openspaces.admin.AdminException;

/**
 * @author kimchy (shay.banon)
 */
public class ProcessingUnitAlreadyDeployedException extends AdminException {

    public ProcessingUnitAlreadyDeployedException(String name) {
        super("processing unit [" + name + "] already deployed");
    }
}
