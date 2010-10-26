// JAVA-DOC-STAMP
package org.openspaces.jpa.openjpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates which member of a POJO determines the partition the POJO will be saved in.
 * The partition is determined by calculating the member's value hash code. 
 * 
 * @author idan
 * @since 8.0
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PartitionIndicator {
}
