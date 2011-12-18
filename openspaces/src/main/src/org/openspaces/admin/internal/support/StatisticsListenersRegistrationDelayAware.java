package org.openspaces.admin.internal.support;

/**
 * Allows to postpone statistics listener registration. 
 * @author evgenyf
 * @since 8.0.6
 */
public interface StatisticsListenersRegistrationDelayAware {
    
    long getStatisticsRegistrationDelay();
}