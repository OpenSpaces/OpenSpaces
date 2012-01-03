package org.openspaces.admin.alert.config;

/**
 * A processing unit instance fault-detection configurer. An alert is raised if the fault-detection
 * mechanism has detected failure of a processing unit instance. An alert is resolved if the
 * fault-detection mechanism succeeded to monitor a suspected processing unit instance.
 * <p>
 * <b>note:</b> The member alive fault-detection mechanism (retries and timeout) should be configured separately
 * in the pu.xml (see os-sla:member-alive-indicator)
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration} configuration.
 * 
 * @see ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration
 * @since 8.0.6
 * 
 * @author Moran Avigdor
 */
public class ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer implements AlertConfigurer {

    private final ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration config = new ProcessingUnitInstanceMemberAliveIndicatorAlertConfiguration();
    
    /**
     * Constructs an empty provision failure alert configuration.
     */
    public ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public ProcessingUnitInstanceMemberAliveIndicatorAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }
    
    /**
     * Get a fully configured fault-detection alert configuration (after all properties have been set).
     * @return a fully configured alert configuration.
     */
    @Override
    public AlertConfiguration create() {
        return config;
    }
}
