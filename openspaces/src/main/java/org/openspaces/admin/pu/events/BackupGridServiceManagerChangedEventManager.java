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

package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link BackupGridServiceManagerChangedEventListener}s
 * in order to listen for {@link BackupGridServiceManagerChangedEvent}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#getBackupGridServiceManagerChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getBackupGridServiceManagerChanged()
 */
public interface BackupGridServiceManagerChangedEventManager {

    /**
     * Adds an event listener. Note, the add callback will be called for currently discovered backup managers as well.
     */
    void add(BackupGridServiceManagerChangedEventListener eventListener);

    /**
     * Adds an event listener. Allows to control if the event will be called for existing backup grid
     * service managers as well.
     * @since 8.0.4
     */
    void add(BackupGridServiceManagerChangedEventListener eventListener, boolean includeExisting);
    
    /**
     * Removes an event listener.
     */
    void remove(BackupGridServiceManagerChangedEventListener eventListener);
}