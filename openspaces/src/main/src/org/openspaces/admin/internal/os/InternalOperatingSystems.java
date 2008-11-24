package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystems;

/**
 * @author kimchy
 */
public interface InternalOperatingSystems extends OperatingSystems {

    void addOperatingSystem(OperatingSystem operatingSystem);

    void removeOperatingSystem(String uid);
}
