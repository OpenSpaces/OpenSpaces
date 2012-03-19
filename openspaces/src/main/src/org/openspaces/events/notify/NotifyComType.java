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

/**
 * The communication type used to send notifications.
 *
 * @author kimchy
 */
public enum NotifyComType {

    /**
     * Controls how notification are propagated from the space to the listener. Unicast propagation
     * uses TCP unicast communication which is usually best for small amount of registered clients.
     * This is the default communication type.
     */
    UNICAST(AbstractNotifyEventListenerContainer.COM_TYPE_UNICAST),

    /**
     * Controls how notification are propagated from the space to the listener. Same as unicast ({@link #UNICAST})
     * in terms of communication protocol but uses a single client side multiplexer which handles
     * all the dispatching to the different notification listeners.
     */
    MULTIPLEX(AbstractNotifyEventListenerContainer.COM_TYPE_MULTIPLEX),

    /**
     * Multicast notifications are no longer supported. This enum value will be removed in future versions.
     * @deprecated Since 9.0.0
     */
    @Deprecated
    MULTICAST(AbstractNotifyEventListenerContainer.COM_TYPE_MULTICAST);


    private final int value;


    NotifyComType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
