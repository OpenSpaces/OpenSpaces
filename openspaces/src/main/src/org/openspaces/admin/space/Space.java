package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface Space extends Iterable<SpaceInstance> {

    String getUID();

    String getName();

    SpaceInstance[] getInstnaces();

    int size();
}

