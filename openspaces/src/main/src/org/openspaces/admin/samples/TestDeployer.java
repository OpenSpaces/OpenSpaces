/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

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
