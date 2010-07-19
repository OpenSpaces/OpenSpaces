/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.events.notify;

import com.j_spaces.core.client.INotifyDelegatorFilter;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an event listener as polled event listener. It will be wrapped automtically with
 * {@link SimpleNotifyEventListenerContainer}.
 *
 * <p>Template can be provided using {@link org.openspaces.events.EventTemplate} marked on
 * a general method that returns the template.
 *
 * <p>The event listener method should be marked with {@link org.openspaces.events.adapter.SpaceDataEvent}.
 *
 * @author kimchy
 * @see org.openspaces.events.TransactionalEvent
 * @see org.openspaces.events.notify.NotifyBatch
 * @see org.openspaces.events.notify.NotifyLease
 * @see org.openspaces.events.notify.NotifyType
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Notify {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any
     */
    String value() default "";

    /**
     * The name of the bean that that is the {@link org.openspaces.core.GigaSpace} this container will
     * used.
     *
     * <p>Note, this is optional. If there is only one {@link org.openspaces.core.GigaSpace}
     * defined in the application context, it will be used.
     */
    String gigaSpace() default "";

    /**
     * @see SimpleNotifyEventListenerContainer#setPerformSnapshot(boolean)
     */
    boolean performSnapshot() default true;

    /**
     * @see SimpleNotifyEventListenerContainer#setPerformTakeOnNotify(boolean)
     */
    boolean performTakeOnNotify() default false;

    /**
     * @see SimpleNotifyEventListenerContainer#setIgnoreEventOnNullTake(boolean)
     */
    boolean ignoreEventOnNullTake() default false;

    /**
     * @see SimpleNotifyEventListenerContainer#setGuaranteed(Boolean)
     */
    boolean guaranteed() default false;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setComType(int)
     */
    NotifyComType commType() default NotifyComType.UNICAST;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setFifo(boolean)
     */
    boolean fifo() default false;

    /**
     * When batching is turned on, should the batch of events be passed as an <code>Object[]</code> to
     * the listener. Default to <code>false</code> which means it will be passed one event at a time.
     *
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setPassArrayAsIs(boolean) 
     */
    boolean passArrayAsIs() default false;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setNotifyFilter(com.j_spaces.core.client.INotifyDelegatorFilter)
     */
    Class<INotifyDelegatorFilter> notifyFilter() default INotifyDelegatorFilter.class;

    /**
     * Set whether this container will start once instantiated.
     *
     * <p>Default is <code>true</code>. Set to <code>false</code> in order for this container to
     * be started using {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer#start()}.
     */
    boolean autoStart() default true;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setReplicateNotifyTemplate(boolean)
     */
    ReplicateNotifyTemplateType replicateNotifyTemplate() default ReplicateNotifyTemplateType.DEFAULT;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setTriggerNotifyTemplate(boolean)
     */
    TriggerNotifyTemplateType triggerNotifyTemplate() default TriggerNotifyTemplateType.DEFAULT;
}