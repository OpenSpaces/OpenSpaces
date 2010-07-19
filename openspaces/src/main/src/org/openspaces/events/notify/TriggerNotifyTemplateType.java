package org.openspaces.events.notify;

/**
 * If using a replicated space controls if the listener that are replicated to cluster members
 * will raise notifications.
 *
 * @author kimchy (shay.banon)
 */
public enum TriggerNotifyTemplateType {
    DEFAULT,
    TRUE,
    FALSE
}