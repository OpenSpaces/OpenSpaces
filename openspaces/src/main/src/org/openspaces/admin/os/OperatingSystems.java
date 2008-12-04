package org.openspaces.admin.os;

import java.util.Map;

/**
 * @author kimchy
 */
public interface OperatingSystems extends Iterable<OperatingSystem> {

    OperatingSystem[] getOperatingSystems();

    OperatingSystem getByUID(String uid);

    Map<String, OperatingSystem> getUids();

    int size();

    OperatingSystemsStatistics getStatistics();
}
