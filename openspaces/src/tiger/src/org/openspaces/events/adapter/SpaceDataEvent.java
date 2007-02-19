package org.openspaces.events.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A space data event annotation allowing to mark methods as delegates to be executed when
 * an event occurs.
 *
 * <p>Note, methods can have no parameters. They can also have one or more paramerers ordered based
 * on {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,Object)}.
 *
 * @author kimchy
 * @see org.openspaces.events.adapter.AnnotationEventListenerAdapter
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpaceDataEvent {
}
