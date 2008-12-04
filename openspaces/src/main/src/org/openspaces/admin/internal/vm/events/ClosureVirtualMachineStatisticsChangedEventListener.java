package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureVirtualMachineStatisticsChangedEventListener extends AbstractClosureEventListener implements VirtualMachineStatisticsChangedEventListener {

    public ClosureVirtualMachineStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}