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
package org.openspaces.grid.gsa;

import com.gigaspaces.start.SystemBoot;
import com.j_spaces.kernel.GSThread;

/**
 * Provides a configurer to start a GS-Agent.
 * @since 8.0
 * @author Uri
 */
public class GridServiceAgentConfigurer {
    
    private int gsc = 0;
    private int gsm = 0;
    private int gsmLus = 0;
    private int lus = 0;
    
    private int globalGsc = 0;
    private int globalGsm = 0;
    private int globalGsmLus = 0;
    private int globalLus = 0;
    
    public GridServiceAgentConfigurer locallyManagedGridServiceContainers(int n) {
        this.gsc = n;
        return this;
    }
    
    public GridServiceAgentConfigurer locallyManagedGridServiceManagers(int n) {
        this.gsm = n;
        return this;
    }

    public GridServiceAgentConfigurer locallyManagedGridServiceManagersWithLookupService(int n) {
        this.gsmLus = n;
        return this;
    }

    public GridServiceAgentConfigurer locallyManagedLookupServices(int n) {
        this.lus = n;
        return this;
    }
    
    public GridServiceAgentConfigurer globallyManagedGridServiceContainers(int n) {
        this.globalGsc = n;
        return this;
    }
    
    public GridServiceAgentConfigurer globallyManagedGridServiceManagers(int n) {
        this.globalGsm = n;
        return this;
    }
    
    public GridServiceAgentConfigurer globallyManagedGridServiceManagersWithLookupService(int n) {
        this.globalGsmLus = n;
        return this;
    }
    
    public GridServiceAgentConfigurer globallyManageLookupService(int n) {
        this.globalLus = n;
        return this;
    }
    
    public void create() {
        GSThread starter = new GSThread("GSAgentStarter"){ 
            @Override
            public void run() {
                SystemBoot.main(new String[]{"com.gigaspaces.start.services=\"GSA\"",
                        "gsa.gsc", String.valueOf(gsc),
                        "gsa.gsm", String.valueOf(gsm),
                        "gsa.gsm_lus", String.valueOf(gsmLus),
                        "gsa.lus", String.valueOf(lus),
                        "gsa.global.gsc", String.valueOf(globalGsc),
                        "gsa.global.gsm", String.valueOf(globalGsm),
                        "gsa.global.gsm_lus", String.valueOf(globalGsmLus),
                        "gsa.global.lus", String.valueOf(globalLus),
                        });
            }
        };
        starter.setDaemon(true);
        starter.start();
    }
}
