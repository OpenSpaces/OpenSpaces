package org.openspaces.events;

/**
 * A template provider allowing for custom implementations to provide a template used for matching
 * on events to receive.
 * 
 * <p>
 * Can be used when a {@link org.openspaces.events.SpaceDataEventListener SpaceDataEventListener} is
 * provided that also controls the template associated with it. This of course depends on the
 * container and containers should take it into account (such as
 * {@link org.openspaces.events.polling.SimplePollingEventListenerContainer} and
 * {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer}.
 * 
 * @author kimchy
 */
public interface EventTemplateProvider {

    /**
     * Returns the template that will be used for matching on events.
     */
    Object getTemplate();
}
