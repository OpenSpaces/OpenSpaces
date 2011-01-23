package org.openspaces.admin.alert.config.parser;

import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.AlertConfiguration;

/**
 * A configuration parser for setting a bulk of alert configurations using the call to
 * {@link AlertManager#configure(AlertConfiguration[])}.
 * <p>
 * @see XmlAlertConfigurationParser which reads from an xml file the alert configurations.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertConfigurationParser {

    /**
     * Parse and return an array of {@link AlertConfiguration}.
     * <p>
     * <ul>
     * <li>{@link AlertConfiguration#getProperties()} - String key-value properties used to
     * configure the alert.</li>
     * <li>{@link AlertConfiguration#isEnabled()} - enable the configuration for this alert.</li>
     * <li>{@link AlertConfiguration#getBeanClassName()} - the server-side <tt>alert trigger</tt>
     * class name.</li>
     * </ul>
     * 
     * @return a non-null array of {@link AlertConfiguration}s.
     * @throws AlertConfigurationParserException
     *             if failed to parse.
     */
    AlertConfiguration[] parse() throws AlertConfigurationParserException;
}
