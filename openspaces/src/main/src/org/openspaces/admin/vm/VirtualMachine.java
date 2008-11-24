package org.openspaces.admin.vm;

import com.gigaspaces.jvm.JVMConfiguration;
import com.gigaspaces.jvm.JVMStatistics;

/**
 * @author kimchy
 */
public interface VirtualMachine {

    String getUID();

    JVMConfiguration getConfiguration();

    JVMStatistics getStatistics();
}
