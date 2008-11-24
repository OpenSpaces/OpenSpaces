package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystems extends Iterable<OperatingSystem> {

    OperatingSystem[] getOperatingSystems();

    OperatingSystem getByUID(String uid);

    int size();
}
