package org.openspaces.admin.pu;

import org.openspaces.admin.Admin;

import java.util.Map;

/**
 * @author kimchy
 */
public interface ProcessingUnits extends Iterable<ProcessingUnit> {

    Admin getAdmin();

    int getSize();

    boolean isEmpty();

    ProcessingUnit[] getProcessingUnits();

    ProcessingUnit getProcessingUnit(String name);

    Map<String, ProcessingUnit> getNames();

    void addEventListener(ProcessingUnitEventListener eventListener);

    void removeEventListener(ProcessingUnitEventListener eventListener);
}
