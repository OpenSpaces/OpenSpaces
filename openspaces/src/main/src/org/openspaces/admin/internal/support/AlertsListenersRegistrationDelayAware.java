package org.openspaces.admin.internal.support;

/**
 * Allows to postpone alerts listener registration.
 * @author evgenyf
 * @since 8.0.6 
 */
public interface AlertsListenersRegistrationDelayAware {
    long getAlertRegistrationDelay();
}