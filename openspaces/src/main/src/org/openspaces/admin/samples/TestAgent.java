package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

/**
 * @author kimchy
 */
public class TestAgent {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        Thread.sleep(5000);
        admin.getGridServiceAgents().getAgents()[0].startGridServiceManager();
    }
}
