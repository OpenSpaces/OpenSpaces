package org.openspaces.admin.alert.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.os.OperatingSystem;

public class ElasticGridServiceAgentProvisioningAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    /** required by java.io.Externalizable */
    public ElasticGridServiceAgentProvisioningAlert() {
    }
    
    public ElasticGridServiceAgentProvisioningAlert(Alert alert) {
        super(alert);
    }

    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link OperatingSystem#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
}
