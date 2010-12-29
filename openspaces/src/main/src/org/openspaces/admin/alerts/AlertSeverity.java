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
 * <p>
 * The levels in descending order are:
 * <ul>
 * <li>SEVERE (highest value)</li>
 * <li>WARNING</li>
 * <li>INFO (lowest value)</li>
 * </ul>
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertSeverity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @serial  The non-localized name of the severity.
     */
    private final String name;

    /**
     * @serial  The integer value of the severity.
     */
    private final int value;

    /**
     * <tt>SEVERE</tt> is an alert severity indicating a serious failure/situation.
     * <p>
     * SEVERE alerts should describe events that are of considerable importance and which will
     * prevent normal program execution. They should be reasonably intelligible to end users and to
     * system administrators.
     * <p>
     * This severity is initialized to <CODE>1000</CODE>.
     */
    public static final AlertSeverity SEVERE = new AlertSeverity("SEVERE", 1000);

    /**
     * <tt>WARNING</tt> is an alert severity indicating a potential problem.
     * <p>
     * WARNING alerts should describe events that will be of interest to end users or system
     * managers, or which indicate potential problems.
     * <p>
     * This severity is initialized to <CODE>900</CODE>.
     */
    public static final AlertSeverity WARNING = new AlertSeverity("WARNING", 900);

    /**
     * <tt>INFO</tt> is an alert severity indicating an informational alert.
     * <p>
     * INFO severity will typically be used when 'resolving' an alert, for which it's previous
     * severity is either unknown or unimportant.
     * <p>
     * This severity is initialized to <CODE>800</CODE>.
     */
    public static final AlertSeverity INFO = new AlertSeverity("INFO", 800);
    
    /**
     * Create a named alert severity with a given integer value.
     * <p>
     * Note that this constructor is "protected" to allow subclassing. In general it is sufficient
     * to use one of the constant Level objects such as {@link #WARNING} or {@link #SEVERE}.
     * However, if there is a need to add a new alert severity, this call may be subclassed and new
     * constants can be defined.
     * 
     * @param name
     *            the name of the alert severity, for example "SEVERE".
     * @param value
     *            an integer value for the severity.
     */
    protected AlertSeverity(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Return the string name of the alert severity.
     *
     * @return name of this alert severity.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the integer value for this alert severity. This integer value can be used for efficient
     * ordering comparisons between alert severity objects.
     * 
     * @return the integer value for this alert severity.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #SEVERE} alert severity.
     */
    public boolean isSevere() {
        return this == SEVERE;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #WARNING} alert severity.
     */
    public boolean isWarning() {
       return this == WARNING; 
    }
    
    /**
     * @return <code>true</code> if this is an {@link #INFO} alert severity.
     */
    public boolean isInfo() {
        return this == INFO;
    }
    
    /**
     * @return the non-localized name of the alert severity, for example "SEVERE".
     */
    @Override
    public final String toString() {
        return name;
    }
}

