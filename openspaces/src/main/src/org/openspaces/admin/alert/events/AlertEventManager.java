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
package org.openspaces.admin.alert.events;

/**
 * An event manager allowing to remove and add {@link AlertEventListener}s.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertEventManager {

    /**
     * Add the event listener. Note, the add callback will be called for currently triggered alerts
     * (both resolved and unresolved alerts).
     */
	void add(AlertEventListener listener);

    /**
     * Add the event listener. Allows to control if the event will be called for existing issued
     * alerts as well or only for future alert events.
     */
	void add(AlertEventListener listener, boolean includeExisting);
	
	/**
	 * Removes the event listener.
	 */
	void remove(AlertEventListener listener);
}
