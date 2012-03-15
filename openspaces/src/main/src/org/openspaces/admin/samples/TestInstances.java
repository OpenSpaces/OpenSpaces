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
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;

/**
 * @author kimchy
 */
public class TestInstances {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        System.out.println("Start 1 GSM and 2 GSCs");
        admin.getGridServiceManagers().waitFor(1);
        System.out.println("Found at least 1 GSM");
        admin.getGridServiceContainers().waitFor(2);
        System.out.println("Found at least 2 GSC");
        ProcessingUnit procesingUnit = admin.getGridServiceManagers().deploy(new ProcessingUnitDeployment("test")
                .numberOfInstances(2));
        System.out.println("Deployed test processing unit, waiting...");
        procesingUnit.waitFor(2);
        System.out.println("Deployed space");

        System.out.println("Incrementing instance ...");
        procesingUnit.incrementInstance();
        System.out.println("Waiting for instance to increment...");
        procesingUnit.waitFor(3);
        System.out.println("Instance incremented");
        Thread.sleep(2000);
        System.out.println("Decrementing instance ...");
        procesingUnit.decrementInstance();
        Thread.sleep(10000);
        System.out.println("Waiting for instance to decrement...");
        while (!(procesingUnit.getInstances().length == 2)) {
            Thread.sleep(1000);
        }
        System.out.println("Instance decremented");
        System.out.println("Undeploying");
        procesingUnit.undeploy();
        System.out.println("Closing admin");
        admin.close();
    }
}
