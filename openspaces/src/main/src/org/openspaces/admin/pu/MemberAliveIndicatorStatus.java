package org.openspaces.admin.pu;

/**
 * Member alive indicator status is communicated by the Grid Service Manager when monitoring managed processing unit instances.
 * The indicator status transitions are:
 * <ul>
 * <li><tt>ALIVE</tt> --> <tt>SUSPECTING_FAILURE</tt> : instance has failed to reply on monitoring attempt</li>
 * <li><tt>SUSPECTING_FAILURE</tt> --> <tt>ALIVE</tt> : instance has yet to reply on monitoring <b>retry</b> attempt</li>
 * <li><tt>SUSPECTING_FAILURE</tt> --> <tt>DETECTED_FAILURE</tt>: instance has failed to respond for all monitoring <b>retry</b> attempts</li>
 * </ul>
 * 
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener
 * 
 * @since 8.0.6
 * @author moran
 */
public enum MemberAliveIndicatorStatus {

    /** processing unit instance has replied to monitoring attempts */
    ALIVE,
    /** processing unit instance has yet to respond to monitoring retry attempts */
    SUSPECTING_FAILURE,
    /** processing unit instance has failed to respond for all monitoring retry attempts */
    DETECTED_FAILURE;   
}
