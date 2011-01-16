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

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;

/**
 * An event listener for all types of alerts. Register/Unregister for alert events, using the
 * {@link AlertTriggeredEventManager} API accessible via {@link Admin#getAlertManager()}.
 * <p>
 * Alerts may be then filtered by their wrapper class (see implementations of {@link Alert}) or any other
 * criteria exposed by the getters in {@link Alert}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertTriggeredEventListener {
    
    /**
     * @param alert An event of an alert issued by one of the admin Alert triggers.
     */
	public void alertTriggered(Alert alert);
}
