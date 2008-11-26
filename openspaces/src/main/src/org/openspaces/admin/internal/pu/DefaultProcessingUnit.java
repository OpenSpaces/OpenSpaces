package org.openspaces.admin.internal.pu;

import com.gigaspaces.grid.gsm.PUDetails;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.DeploymentStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnit implements InternalProcessingUnit {

    private final String name;

    private final int numberOfInstances;

    private final int numberOfBackups;

    private DeploymentStatus deploymentStatus;

    private volatile GridServiceManager managingGridServiceManager;

    private final Map<String, GridServiceManager> backupGridServiceManagers = new ConcurrentHashMap<String, GridServiceManager>();

    public DefaultProcessingUnit(PUDetails details) {
        this.name = details.getName();
        this.numberOfInstances = details.getNumberOfInstances();
        this.numberOfBackups = details.getNumberOfBackups();
        setStatus(details.getStatus());
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfInstances() {
        return this.numberOfInstances;
    }

    public int getNumberOfBackups() {
        return this.numberOfBackups;
    }

    public DeploymentStatus getStatus() {
        return this.deploymentStatus;
    }

    public GridServiceManager getManagingGridServiceManager() {
        return this.managingGridServiceManager;
    }

    public GridServiceManager[] getBackupGridServiceManagers() {
        return this.backupGridServiceManagers.values().toArray(new GridServiceManager[0]);
    }

    public boolean isManaged() {
        return managingGridServiceManager != null;
    }

    public GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID) {
        return backupGridServiceManagers.get(gridServiceManagerUID);
    }

    public void setManagingGridServiceManager(GridServiceManager gridServiceManager) {
        this.managingGridServiceManager = gridServiceManager;
    }

    public void addBackupGridServiceManager(GridServiceManager backupGridServiceManager) {
        this.backupGridServiceManagers.put(backupGridServiceManager.getUID(), backupGridServiceManager);
    }

    public void removeBackupGridServiceManager(String gsmUID) {
        backupGridServiceManagers.remove(gsmUID);
    }

    public boolean setStatus(int statusCode) {
        DeploymentStatus tempStatus;
        switch (statusCode) {
            case 0:
                tempStatus = DeploymentStatus.UNDEPLOYED;
                break;
            case 1:
                tempStatus = DeploymentStatus.SCHEDULED;
                break;
            case 2:
                tempStatus = DeploymentStatus.DEPLOYED;
                break;
            case 3:
                tempStatus = DeploymentStatus.BROKEN;
                break;
            case 4:
                tempStatus = DeploymentStatus.COMPROMISED;
                break;
            case 5:
                tempStatus = DeploymentStatus.INTACT;
                break;
            default:
                throw new IllegalStateException("No status match");
        }
        if (tempStatus != deploymentStatus) {
            deploymentStatus = tempStatus;
            return true;
        }
        return false;
    }
}
