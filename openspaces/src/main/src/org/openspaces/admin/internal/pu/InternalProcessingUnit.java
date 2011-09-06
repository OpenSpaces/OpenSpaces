package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.application.InternalApplicationAware;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.space.Space;

/**
 * @author kimchy
 */
public interface InternalProcessingUnit extends ProcessingUnit, InternalProcessingUnitInstancesAware , InternalApplicationAware {

    void setNumberOfInstances(int numberOfInstances);

    void setNumberOfBackups(int numberOfBackups);

    void setManagingGridServiceManager(GridServiceManager gridServiceManager);
    
    void addManagingGridServiceManager(GridServiceManager gridServiceManager);

    void addBackupGridServiceManager(GridServiceManager backupGridServiceManager);

    void removeBackupGridServiceManager(String gsmUID);

    boolean setStatus(int statusCode);

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addEmbeddedSpace(Space space);
    
    Map<String, String> getElasticProperties();

    String getApplicationName();
    
    String getClusterSchema();

    /**
     * Retrieves a context property containing the order of dependencies within an application.
     * @return The property value, in a CSV format.
     * @since 8.0.4 
     */
    String getApplicationDependencies();
    
    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ScaleStrategyConfigEventListener}s.
     *
     * @since 8.0.3
     */
    //TODO:ScaleStrategyConfigChangedEventManager getScaleStrategyConfigChanged(ScaleStrategyConfigChangedEventListener eventListener);
    
    /**
     * Returns the current scale strategy config
     *
     * @since 8.0.3
     */
    ScaleStrategyConfig getScaleStrategyConfig();

    /**
     * Decrements a planned instance if the number of planned instances is less then the actual instances to maintain.
     * @return <code>true</code> if a planned instance was successfully decremented; <code>false</code> planned instances hasn't changed.
     * @since 8.0.4
     */
    boolean decrementPlannedInstances();
}
