package org.openspaces.grid.gsa;

import com.gigaspaces.start.SystemBoot;

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
        new Thread(new Runnable() {
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
        },"GSAgentStarter").start();
    }
}
