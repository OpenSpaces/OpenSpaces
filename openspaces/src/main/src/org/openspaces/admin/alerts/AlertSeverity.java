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
package org.openspaces.admin.alerts;

/**
 * The alert severity levels.
 * 
 * The levels in descending order are:
 * <ul>
 * <li>CRITICAL (highest value)
 * <li>WARNING
 * <li>OK
 * <li>NA (lowest value)
 * </ul>
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public enum AlertSeverity {
    /** Alert indicating a critical state */
    CRITICAL, 
    /** Alert warning about a critical state */
    WARNING,
    /** Alert indicating a normal state */
    OK,
    /** Alert indicating that a state is not available; e.g. monitored component is unreachable */
    NA;
}
