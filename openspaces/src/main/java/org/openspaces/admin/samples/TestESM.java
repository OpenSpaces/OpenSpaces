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
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.ElasticSpaceDeployment;

public class TestESM {
 
        public static void main(String[] args) throws Exception {
            
            Admin admin = new AdminFactory().addGroup("moran-gigaspaces-7.1.0-XAPPremium-rc").createAdmin();
            System.out.println("Waiting for at least one agent");
            admin.getGridServiceAgents().waitForAtLeastOne();
            GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne();
            System.out.println("found gsa,gsm");
            ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().waitForAtLeastOne();

            System.out.println("found esm, deploying");
            
            ProcessingUnit pu = gsm.
            deploy(new ElasticSpaceDeployment("mygrid")
            .maxMemoryCapacity("2000m")
            .commandLineArgument("-Xmx250m")            
            );
            
            System.out.println("PU deployed: " + pu.getName() + " " + pu.getNumberOfInstances()+","+pu.getNumberOfBackups());
            System.exit(0);
        }
}
