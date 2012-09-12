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
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsa.GridServiceManagerOptions;
import org.openspaces.admin.gsa.GridServiceOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public class TestAgent {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        System.out.println("Waiting for at least one agent");
        GridServiceAgent gridServiceAgent = admin.getGridServiceAgents().waitForAtLeastOne();

        System.out.println("Starting 2 GSM and 2 GSC");

        GridServiceManager gsm1 = gridServiceAgent.startGridServiceAndWait(new GridServiceManagerOptions().useScript());
        GridServiceManager gsm2 = gridServiceAgent.startGridServiceAndWait(new GridServiceManagerOptions());

        GridServiceContainer gsc1 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument("-Xmx512m"));
        GridServiceContainer gsc2 = gridServiceAgent.startGridServiceAndWait(new GridServiceContainerOptions().useScript());

        gridServiceAgent.startGridService(new GridServiceOptions("hsqldb").argument("-port").argument("1987"));

        System.out.println("Deploying a space");
        ProcessingUnit processingUnit = gsm1.deploy(new SpaceDeployment("test").numberOfInstances(2).numberOfBackups(1));
        Space space = processingUnit.waitForSpace();
        System.out.println("Waiting for all instances to be up");
        space.waitFor(4);

        System.out.println("Undeploying the space");
        processingUnit.undeploy();
        System.out.println("Stopping GSM and GSC");

        gsc1.kill();
        gsc2.kill();
        gsm1.kill();
        gsm2.kill();

        admin.close();
    }
}
