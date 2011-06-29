package org.openspaces.admin.alert.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * A provision failure alert triggered when a processing unit has less actual instances than planned
 * instances. The alert is resolved when the processing unit actual instance count is equal to the
 * planned instance count.
 * <p>
 * This alert will be received on the call to
 * {@link AlertTriggeredEventListener#alertTriggered(Alert)} for registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0.3
 */
public class ProvisionFailureAlert extends AbstractAlert {
    
    /** required by java.io.Externalizable */
    public ProvisionFailureAlert() {
    }
    
    public ProvisionFailureAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link ProcessingUnit#getName()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
}
