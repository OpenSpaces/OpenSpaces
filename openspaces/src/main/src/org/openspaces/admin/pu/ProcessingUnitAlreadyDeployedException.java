package org.openspaces.admin.pu;

import org.openspaces.admin.AdminException;

/**
 * @author kimchy (shay.banon)
 */
public class ProcessingUnitAlreadyDeployedException extends AdminException {

    private static final long serialVersionUID = 7237728063214305847L;
    private final String processingUnitName;

    public ProcessingUnitAlreadyDeployedException(String name) {
        super("processing unit [" + name + "] already deployed");
        this.processingUnitName = name;
    }
    
    public String getProcessingUnitName() {
        return this.processingUnitName;
    }
}
