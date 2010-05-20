package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.core.GigaSpace;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.getGridServiceManagers().waitFor(1);
        admin.getGridServiceContainers().waitFor(2);

//        ProcessingUnit unit = admin.getGridServiceManagers().deploy(new SpaceDeployment("test").partitioned(1, 1));
//        unit.waitFor(unit.getTotalNumberOfInstances());
//        GigaSpace gigaSpace = unit.getSpace().getGigaSpace();

        ProcessingUnit unit = admin.getGridServiceManagers().deploy(new MemcachedDeployment("/./test").partitioned(1, 1));
        unit.waitFor(unit.getTotalNumberOfInstances());

        for (ProcessingUnitInstance instance : unit) {
            System.out.println(instance.getClusterInfo().getUniqueName() + ": Memcached started on port [" + instance.getMemcachedDetails().getPort() + "]");
        }

        while (true) {
            Thread.sleep(3000);
            System.out.println("---------------------------------");
            for (ProcessingUnitInstance instance : unit) {
                System.out.println(instance.getClusterInfo().getUniqueName() + ": Gets [" + instance.getStatistics().getMemcached().getGetCmds() + "]");
            }
        }

//        admin.close();
    }
}
