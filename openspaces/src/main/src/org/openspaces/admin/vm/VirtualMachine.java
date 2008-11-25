package org.openspaces.admin.vm;

import com.gigaspaces.jvm.VirtualMachineConfiguration;
import com.gigaspaces.jvm.VirtualMachineStatistics;

/**
 * @author kimchy
 */
public interface VirtualMachine {

    String getUID();

    VirtualMachineConfiguration getConfiguration();

    VirtualMachineStatistics getStatistics();
}
