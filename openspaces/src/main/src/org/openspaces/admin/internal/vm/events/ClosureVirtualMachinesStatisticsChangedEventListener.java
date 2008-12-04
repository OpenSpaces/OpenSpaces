package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureVirtualMachinesStatisticsChangedEventListener extends AbstractClosureEventListener implements VirtualMachinesStatisticsChangedEventListener {

    public ClosureVirtualMachinesStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void virtualMachinesStatisticsChanged(VirtualMachinesStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}