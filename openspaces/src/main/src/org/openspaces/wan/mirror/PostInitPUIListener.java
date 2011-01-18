package org.openspaces.wan.mirror;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;

final class PostInitPUIListener implements ProcessingUnitInstanceAddedEventListener {
    
    private final Admin admin;
    
    private boolean finished = false;

    private int gscPort;

    private String myNICAddress;

    private String puName;

    private int discoveryPort;
    PostInitPUIListener(Admin admin, int gscPort, int discoveryPort, String myNICAddress, String puName) {
        this.gscPort = gscPort;
        this.myNICAddress = myNICAddress;
        this.admin = admin;
        this.puName = puName;
        this.discoveryPort = discoveryPort;
    }

    public void processingUnitInstanceAdded(final ProcessingUnitInstance processingUnitInstance) {
        if(finished) {
            // This edge case seems to happen rarely - need to sort out why
            WanDataSource.logger.warning("PUI Added notification still active even though " +
            		"Mirror instance has been moved and admin closed!");
            return;
        }
        WanDataSource.logger.info("PUI added to PU: " + processingUnitInstance
            .getProcessingUnit().getName());
        if (this.puName.equals(processingUnitInstance
            .getProcessingUnit().getName())) {
            new GSCForkHandler(admin, this.gscPort, this.discoveryPort, processingUnitInstance,
                    this.myNICAddress).moveMirrorToAlternativeGSC();
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(this);
            
            admin.close();
            finished = true;
        }

    }
}