package org.openspaces.admin.pu;

import org.openspaces.admin.Admin;

/**
 * @author kimchy
 */
public interface ProcessingUnits extends Iterable<ProcessingUnit> {

    Admin getAdmin();

    ProcessingUnit[] getProcessingUnits();

    ProcessingUnit getProcessingUnit(String name);

    void addEventListener(ProcessingUnitEventListener eventListener);

    void removeEventListener(ProcessingUnitEventListener eventListener);
}
