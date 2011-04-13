package org.openspaces.admin.alert.config;

/**
 * A provision failure alert configurer. An alert is raised if the processing unit has less actual
 * instances than planned instances. An alert is resolved when the processing unit actual instance
 * count is equal to the planned instance count.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ProvisionFailureAlertConfiguration} configuration.
 * 
 * @see ProvisionFailureAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0.2
 */
public class ProvisionFailureAlertConfigurer implements AlertConfigurer {

    private final ProvisionFailureAlertConfiguration config = new ProvisionFailureAlertConfiguration();
    
    /**
     * Constructs an empty provision failure alert configuration.
     */
    public ProvisionFailureAlertConfigurer() {
    }
    
    /**
     * Get a fully configured provision failure alert configuration (after all properties have been set).
     * @return a fully configured alert configuration.
     */
    public AlertConfiguration create() {
        return config;
    }

}
