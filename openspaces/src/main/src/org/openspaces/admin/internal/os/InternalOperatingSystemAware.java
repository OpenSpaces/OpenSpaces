package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemAware;

/**
 * @author kimchy
 */
public interface InternalOperatingSystemAware extends OperatingSystemAware {

    void setOperatingSystem(OperatingSystem operatingSystem);
}
