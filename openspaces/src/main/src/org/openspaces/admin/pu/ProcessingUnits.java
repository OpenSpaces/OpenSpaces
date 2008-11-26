package org.openspaces.admin.pu;

/**
 * @author kimchy
 */
public interface ProcessingUnits extends Iterable<ProcessingUnit> {

    ProcessingUnit[] getProcessingUnits();

    ProcessingUnit getProcessingUnit(String name);
}
