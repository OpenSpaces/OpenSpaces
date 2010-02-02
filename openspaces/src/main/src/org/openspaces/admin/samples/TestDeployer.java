package org.openspaces.admin.samples;

import com.gigaspaces.log.*;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.core.GigaSpace;

import static com.gigaspaces.log.LogEntryMatchers.*;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.getGridServiceManagers().waitFor(1);
        admin.getGridServiceContainers().waitFor(2);

        ProcessingUnit unit = admin.getGridServiceManagers().deploy(new SpaceDeployment("test").partitioned(1, 1));
        unit.waitFor(unit.getTotalNumberOfInstances());
        GigaSpace gigaSpace = unit.getSpace().getGigaSpace();

        admin.close();
    }
}
