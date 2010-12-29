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
package org.openspaces.admin.alert;

import java.io.Serializable;
import java.util.Map;

import org.openspaces.admin.Admin;

/**
 * An interface representing an alert issued by an alert bean or an alert provider.
 * <p>
 * <p>
 * An alert has a unique alert id ({@link #getAlertUid()}) and is fired by one of the alert beans,
 * and aggregated with other alerts of the same ({@link #getGroupUid()}).
 * <p>
 * An <b>unresolved</b> alert is an alert that indicates a problematic situation that needs
 * attention. An alert bean can trigger more than one unresolved alert if the problem persists (with
 * the same alert group UID). A <b>resolved</b> alert ({@link AlertStatus#RESOLVED}) is an alert that
 * indicates that the situation was resolved, or is no longer in need of attention.
 * <p>
 * Each alert has descriptive information of the alert ({@link #getDescription()}), data and time of
 * the alert ({@link #getTimestamp()}), and configuration properties together with runtime
 * properties the alert bean exposes (see {@link #getProperties()}).
 * <p>
 * The source component for which the alert was triggered (see {@link #getComponentUid()}) can be
 * correlated to one of the components using the {@link Admin#getGridComponentByUID(String)} if this
 * component has not yet been terminated.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface Alert extends Serializable {

    /**
     * @return Alert Name - the name assigned to the alert.
     */
    public String getName();

    /**
     * @return Description - a description of the alert.
     */
    public String getDescription();

    /**
     * @return Timestamp - the date and time the alert occurred.
     */
    public long getTimestamp();

    /**
     * @return Severity - the defined severity of the alert.
     */
    public AlertSeverity getSeverity();
    
    /**
     * @return Status - the status of the alert.
     */
    public AlertStatus getStatus();

    /**
     * @return Alert UID - the unique identification for this alert.
     */
    public String getAlertUid();

    /**
     * @return Group UID - the unique identification of the group this alert belongs to.
     */
    public String getGroupUid();

    /**
     * @return Component UID - the unique identifier of the component associated with the alert.
     */
    public String getComponentUid();

    /**
     * @return  A map of String key-value property pairs including configuration properties, and any runtime
     * properties exposed by the alert bean.
     */
    public Map<String, String> getProperties();

}