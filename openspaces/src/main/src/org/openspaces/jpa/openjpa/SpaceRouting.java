package org.openspaces.jpa.openjpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated field as the one who determines on which of the partitions
 * the object will be saved.
 * 
 * @author idan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpaceRouting {
}
