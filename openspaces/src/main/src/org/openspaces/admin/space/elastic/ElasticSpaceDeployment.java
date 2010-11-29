package org.openspaces.admin.space.elastic;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.MemorySla;
import org.openspaces.admin.pu.elastic.SLA;
import org.openspaces.grid.esm.ElasticScaleHandler;

/**
 * An elastic data-grid deployment descriptor for deploying a plain data-grid.
 * <p>
 * The data-grid context properties can be modified by {@link #addContextProperty(String, String)}.
 * The default is a Highly-available data-grid, consisting of 1-10 gigabytes of memory spanned
 * across a partitioned cluster of 10,1. Each Grid Service Container JVM is set to -Xms512m and
 * -Xmx512m.
 * </p>
 * 
 * @author moran
 * @author itaif
 */
public class ElasticSpaceDeployment {
   
    private final ElasticProcessingUnitDeployment deployment;
    
    private final String spaceName;
    

    /**
     * Constructs an Elastic space with the specified name.
     * @param spaceName The space name.
     */    
    public ElasticSpaceDeployment(String spaceName) {
        this.spaceName = spaceName;
        this.deployment = new ElasticProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }
    
        
    /**
     * Returns the Space name of the deployment.
     */
    public String getSpaceName() {
        return spaceName;
    }

    /**
     * Sets the number of instances that will be deployed as part of this processing unit instance. This setting will override the default
     * calculated value for the number of partitions derived from the {@link #capacity(String, String)} parameters.
     */
    public ElasticSpaceDeployment numberOfInstances(int numberOfInstances) {
       deployment.numberOfInstances(numberOfInstances);
       return this;
    }

    /**
     * Sets the number of backups per instance of the space deployment.
     * Setting 0 backup is same as calling highlyAvailable(false)
     * Setting 1 backup is the default and is the same as calling highlyAvailable(true)
     */
    public ElasticSpaceDeployment numberOfBackups(int numberOfBackups) {
        deployment.numberOfBackups(numberOfBackups);
        return this;
    }
    
    /**
     * Set the availability of the data grid. A highly available data grid is one that has at least
     * one backup copy per primary, for failing over to.
     * 
     * @param enabled
     *            <code>true</code> if data grid is highly available; false if the data is
     *            recoverable from some storage. Default is <code>true</code>.
     */
    public ElasticSpaceDeployment highlyAvailable(boolean enabled) {
        deployment.highlyAvailable(enabled);
        return this;
    }
    
    /**
     * Adds a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public ElasticSpaceDeployment setContextProperty(String key, String value) {
       deployment.setContextProperty(key,value);
       return this;
    }
    
    /**
     * Restrict this data-grid deployment to machines not shared by other deployments.
     */
    public ElasticSpaceDeployment dedicatedDeploymentIsolation() {
        deployment.dedicatedDeploymentIsolation();
        return this;
    }

    /**
     * Allow this data-grid deployment to co-exist and share the same machine resources with other deployments.
     */
    public ElasticSpaceDeployment publicDeploymentIsolation() {
        deployment.publicDeploymentIsolation();
        return this;
    }

    /**
     * Allow this data-grid deployment to co-exist and share the same machine resources with other
     * deployments of this tenant - only.
     * 
     * @param tenant A name representing the tenant.
     */
    public ElasticSpaceDeployment sharedDeploymentIsolation(String tenant) {
        deployment.sharedDeploymentIsolation(tenant);
        return this;
    }
    
    /**
     * Set the memory capacity growth of this data grid. The parameter specifies size in bytes (b),
     * kilobytes (k), megabytes (m), gigabytes (g). These two control respectively -Xms and -Xmx
     * of the Grid Service Container JVM. Default it "1g"-"10g".
     * 
     * @param minMemory
     *            minimum memory initially held for this data grid; Default "1g".
     * @param maxMemory
     *            maximum memory to allocate for this data grid; Default "10g".
     */
    public ElasticSpaceDeployment capacity(String minMemory, String maxMemory) {
        deployment.capacity(minMemory,maxMemory);
        return this;
    }



    /**
     * Sets the initial Java heap size (the JVM -Xms argument) of a container hosting a processing unit.
     * For performance optimization you should have the initial heap size the same as the maximum size.
     * @param size The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticSpaceDeployment initialJavaHeapSize(String size) {
        deployment.initialJavaHeapSize(size);
        return this;
    }

    /**
     * Sets the maximum Java heap size (the JVM -Xmx argument) of a container hosting a processing
     * unit. In many cases the heap size is determined based on the operating system: for a 32-bit
     * OS, a 2 GB maximum heap size is recommended, and for a 64-bit OS, a 6-10 MB maximum heap size
     * is recommended.
     * <p>
     * If set below the {@link #initialJavaHeapSize(String)} then the -Xms setting will correspond
     * to the -Xmx setting.
     * 
     * @param size
     *            The heap size in kilobytes (k), megabytes (m), gigabytes (g); Default "512m".
     */
    public ElasticSpaceDeployment maximumJavaHeapSize(String size) {
        deployment.maximumJavaHeapSize(size);
        return this;
    }

    /**
     * Adds a single JVM level argument to the container hosting a processing unit. Note that the
     * {@link #initialJavaHeapSize(String)} and {@link #maximumJavaHeapSize(String)} already define
     * the -Xms and -Xmx vm arguments.
     */
    public ElasticSpaceDeployment vmInputArgument(String vmInputArgument) {
        deployment.vmInputArgument(vmInputArgument);
        return this;
    }
    

    /**
     * Adds an SLA to monitor and auto-scale if necessary.
     * @see MemorySla
     * 
     * @param sla an SLA descriptor
     */
    public ElasticSpaceDeployment sla(SLA sla) {
        deployment.sla(sla);
        return this;
    }

    /**
     * Set an {@link ElasticScaleHandler} implementation configuration. The scale-handler (and its
     * state) is per-deployment. The scale-handler API will be called upon when need of a GSC (either
     * on an available machine, or a new machine)
     * 
     * @param classname The elastic scale handler class name
     * @param properties The elastic scale handler properties.
     */
    public ElasticSpaceDeployment elasticScaleHandler(String classname, Map<String, String> properties) {
        deployment.elasticScaleHandler(classname,properties);
        return this;
    }
    
    /**
     * Transforms the space deployment to a processing unit deployment (it is a processing unit after all,
     * that simply starts an embedded space).
     */
    public ProcessingUnitDeployment toProcessingUnitDeployment() {
        return deployment.toProcessingUnitDeployment();
    }

}
