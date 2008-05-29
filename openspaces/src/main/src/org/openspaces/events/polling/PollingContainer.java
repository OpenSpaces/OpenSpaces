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

package org.openspaces.events.polling;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an event listener as polled event listener. It will be wrapped automtically with
 * {@link org.openspaces.events.polling.SimplePollingEventListenerContainer}.
 *
 * <p>Template can be provided using {@link org.openspaces.events.EventTemplate} marked on
 * a general method that returns the template.
 *
 * <p>The event listener method should be marked with {@link org.openspaces.events.adapter.SpaceDataEvent}.
 *
 * @see org.openspaces.events.TransactionalEventContainer
 * @see org.openspaces.events.polling.ReceiveHandler
 * @see org.openspaces.events.polling.TriggerHandler
 * @author kimchy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface PollingContainer {

    /**
     * The name of the bean that that is the {@link org.openspaces.core.GigaSpace} this container will
     * used.
     *
     * <p>Note, this is optional. If there is a field of type {@link org.openspaces.core.GigaSpace} it
     * will be used. If there is none, and there is only one {@link org.openspaces.core.GigaSpace}
     * defined in the application context, it will be used.
     */
    String gigaSpace() default "";

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setConcurrentConsumers(int)
     */
    int concurrentConsumers() default 1;

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setMaxConcurrentConsumers(int)
     */
    int maxConcurrentConsumers() default 1;

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setReceiveTimeout(long)
     */
    long receiveTimeout() default AbstractPollingEventListenerContainer.DEFAULT_RECEIVE_TIMEOUT;

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setPerformSnapshot(boolean)
     */
    boolean performSnapshot() default true;

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setPassArrayAsIs(boolean)
     */
    boolean passArrayAsIs() default false;

    /**
     * @see org.openspaces.events.polling.SimplePollingEventListenerContainer#setRecoveryInterval(long)
     */
    long recoveryInterval() default SimplePollingEventListenerContainer.DEFAULT_RECOVERY_INTERVAL;
}
