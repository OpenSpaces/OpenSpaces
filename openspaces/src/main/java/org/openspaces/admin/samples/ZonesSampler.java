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
import org.openspaces.admin.zone.Zone;

/**
 * @author kimchy
 */
public class ZonesSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        while(true) {
            for (Zone zone : admin.getZones()) {
                System.out.println("Zone [" + zone.getName() + "]");
                System.out.println("    -> Spaces [" + zone.getSpaceInstances().length + "], PUs [" + zone.getProcessingUnitInstances().length + "], GSA [" + zone.getGridServiceAgents().getSize() + "], GSMs [" + zone.getGridServiceManagers().getSize() + "], GSCs [" + zone.getGridServiceContainers().getSize() + "], Vms [" + zone.getVirtualMachines().getSize() + "], Transports [" + zone.getTransports().getSize() + "], Machines [" + zone.getMachines().getSize() + "]");
            }
            Thread.sleep(1000);
        }
    }
}
