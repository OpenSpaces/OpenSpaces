package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminException;

/**
 * An exception indicating that a failure has occurred while trying to instantiate a processing unit instance.
 * @see ProcessingUnitInstanceProvisionFailureEvent  
 * @author moran
 * @since 8.0.6
 */
public class ProcessingUnitInstanceProvisionFailureException extends AdminException {

    private static final long serialVersionUID = 1L;
    private final boolean uninstantiable;

    public ProcessingUnitInstanceProvisionFailureException(String message, boolean uninstantiable) {
        super(message);
        this.uninstantiable = uninstantiable;
    }
    
    /**
     * An un-instantiable instance is an instance that cannot be instantiated - probably due to
     * missing resources or classes. The general case is that all instances should be
     * re-instantiable in order to keep the requested SLA for number of instances.
     * 
     * @return <code>true</code> if this instance was unable to instantiate and will not be
     *         re-provisioned again.
     */
    public boolean isUninstantiable() {
        return uninstantiable;
    }
}
