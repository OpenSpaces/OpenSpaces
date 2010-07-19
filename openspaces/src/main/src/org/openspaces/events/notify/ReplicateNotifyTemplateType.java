package org.openspaces.events.notify;

/**
 * If using a replicated space controls if the listener will be replicated between all the
 * replicated cluster members.
 *
 * @author kimchy (shay.banon)
 */
public enum ReplicateNotifyTemplateType {
    /**
     * The default value will be <code>false</code> if working against an embedded space instance,
     * and <code>true</code> otherwise.
     */
    DEFAULT,
    /**
     * Replicate notify templates between members.
     */
    TRUE,
    /**
     * Don't replicate notify templates between members.
     */
    FALSE
}
