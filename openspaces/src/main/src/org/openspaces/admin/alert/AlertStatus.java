package org.openspaces.admin.alert;

/**
 * The alert status levels.
 * <p>
 * The levels in descending order are:
 * <ul>
 * <li>ESCALATED (highest value)</li>
 * <li>RAISED</li>
 * <li>SUPPRESSED</li>
 * <li>RESOLVED</li>
 * <li>NA (lowest value)</li>
 * </ul>
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertStatus implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @serial  The non-localized name of the status.
     */
    private final String name;

    /**
     * @serial  The integer value of the status.
     */
    private final int value;
    
    /**
     * <tt>ESCALATED</tt> is an alert status indicating that a raised alert has been escalated.
     * <p>
     * ESCALATED alert status is intended to inform of a situation that is in need of attention,
     * and has gone worse.
     * <p>
     * This status is initialized to <CODE>1000</CODE>.
     */
    public static final AlertStatus ESCALATED = new AlertStatus("ESCALATED", 1000);

    /**
     * <tt>RAISED</tt> is an alert status indicating an alert has been raised.
     * <p>
     * RAISED alerts are intended to inform of a situation that is in need of attention.
     * <p>
     * This status is initialized to <CODE>900</CODE>.
     */
    public static final AlertStatus RAISED = new AlertStatus("RAISED", 900);

    /**
     * <tt>SUPPRESSED</tt> is an alert status indicating that a raised alert has been suppressed.
     * <p>
     * SUPPRESSED alert status is intended to inform of a situation that has been attended, but will
     * not be resolved.
     * <p>
     * This status is initialized to <CODE>800</CODE>.
     */
    public static final AlertStatus SUPPRESSED = new AlertStatus("SUPPRESSED", 800);

    /**
     * <tt>RESOLVED</tt> is an alert status indicating that a raised alert has been resolved.
     * <p>
     * RESOLVED alert status is intended to inform of a situation that has been attended and has
     * been resolved.
     * <p>
     * This status is initialized to <CODE>700</CODE>.
     */
    public static final AlertStatus RESOLVED = new AlertStatus("RESOLVED", 700);

    /**
     * <tt>NA</tt> is an alert status indicating that a component for which an alert has been raised
     * is no longer available.
     * <p>
     * NA (NOT AVAILABLE) alert status is intended to inform of a situation that is in need of
     * attention, but it's status can't be currently determined. This alert status can be considered
     * as a 'resolution' depending on the system involved.
     * <p>
     * This status is initialized to <CODE>600</CODE>.
     */
    public static final AlertStatus NA = new AlertStatus("NA", 600);

    /**
     * Create a named alert status with a given integer value.
     * <p>
     * Note that this constructor is "protected" to allow subclassing. In general it is sufficient
     * to use one of the constant Level objects such as {@link #RESOLVED} or {@link #RAISED}.
     * However, if there is a need to add a new alert status, this call may be subclassed and new
     * constants can be defined.
     * 
     * @param name
     *            the name of the alert status, for example "RAISED".
     * @param value
     *            an integer value for the status.
     */
    protected AlertStatus(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Return the string name of the alert status.
     *
     * @return name of this alert status.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the integer value for this alert status. This integer value can be used for efficient
     * ordering comparisons between alert status objects.
     * 
     * @return the integer value for this alert status.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * @return <code>true</code> if this is an {@link #ESCALATED} alert status.
     */
    public boolean isEscalated() {
        return this == ESCALATED;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #RAISED} alert status.
     */
    public boolean isRaised() {
        return this == RAISED;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #SUPPRESSED} alert status.
     */
    public boolean isSuppressed() {
        return this == SUPPRESSED;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #RESOLVED} alert status.
     */
    public boolean isResolved() {
        return this == RESOLVED;
    }
    
    /**
     * @return <code>true</code> if this is a {@link #NA} alert status.
     */
    public boolean isNotAvailable() {
        return this == NA;
    }
    
    /**
     * Returns an AlertStatus by it's value (see {@link #getName()}).
     * @param name representing this status
     * @return a severity.
     * @throws IllegalArgumentException if an unknown status was requested.
     */
    public static AlertStatus parse(String name) {
        if (name == ESCALATED.name) {
            return ESCALATED;
        } else if (name == RAISED.name) {
            return RAISED;
        } else if (name == SUPPRESSED.name) {
            return SUPPRESSED;
        } else if (name == RESOLVED.name) {
            return RESOLVED;
        } else if (name == NA.name) {
            return NA;
        }else {
            throw new IllegalArgumentException("Could not match an AlertStatus with a name of " + name);
        }
    }
    
    /**
     * Returns an AlertStatus by it's value (see {@link #getValue()}).
     * @param value representing this status
     * @return a severity.
     * @throws IllegalArgumentException if an unknown status was requested.
     */
    public static AlertStatus parse(int value) {
        if (value == ESCALATED.value) {
            return ESCALATED;
        } else if (value == RAISED.value) {
            return RAISED;
        } else if (value == SUPPRESSED.value) {
            return SUPPRESSED;
        } else if (value == RESOLVED.value) {
            return RESOLVED;
        } else if (value == NA.value) {
            return NA;
        }else {
            throw new IllegalArgumentException("Could not match an AlertStatus with a value of " + value);
        }
    }
    
    /**
     * Compare two objects for value equality.
     * @return true if and only if the two objects have the same alert status value.
     */
    @Override
    public boolean equals(Object obj) {
        try {
            AlertStatus s = (AlertStatus) obj;
            return (s.value == this.value);
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * Generate a hashcode.
     * @return a hashcode based on the alert status value
     */
    @Override
    public int hashCode() {
        return this.value;
    }
    
    /**
     * @return the non-localized name of the alert status, for example "RESOLVED".
     */
    @Override
    public final String toString() {
        return name;
    }
}
