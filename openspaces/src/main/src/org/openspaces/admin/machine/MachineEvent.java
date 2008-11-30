package org.openspaces.admin.machine;

/**
 * @author kimchy
 */
public class MachineEvent {

    private final Machine machine;

    public MachineEvent(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }
}
