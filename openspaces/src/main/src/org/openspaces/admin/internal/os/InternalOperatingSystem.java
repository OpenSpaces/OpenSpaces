package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystem;

/**
 * @author kimchy
 */
public interface InternalOperatingSystem extends OperatingSystem {

    void addOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider);

    void removeOperatingSystemInfoProvider(InternalOperatingSystemInfoProvider provider);

    boolean hasOperatingSystemInfoProviders();
}
