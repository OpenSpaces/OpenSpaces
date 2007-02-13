package org.openspaces.pu.container.servicegrid.sla;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 13, 2007
 * Time: 12:50:39 AM
 */
public class SLA {
// ------------------------------ FIELDS ------------------------------

    int totalMembers;

    int numbeOfBackups;

    Policy policy;

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getNumbeOfBackups() {
        return numbeOfBackups;
    }

    public void setNumbeOfBackups(int numbeOfBackups) {
        this.numbeOfBackups = numbeOfBackups;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }
}
