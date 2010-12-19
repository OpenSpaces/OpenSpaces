package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.bean.BeanConfig;

import com.gigaspaces.security.directory.UserDetails;

public interface ElasticDeploymentTopology {

    /**
     * Sets the processing unit name that will be deployed. By default it will be based on the
     * parameter passed in the constructor.
     */
    ElasticDeploymentTopology name(String name);

    /**
     * Defines a deployment restriction where the processing unit is allowed to be deployed on.
     * The processing unit can only deploy on machines which have a grid service agent with
     * the specified zone or without any specified zones. 
     * For example, a processing unit with zone("z") 
     *  can deploy on GSA["z"] 
     *  can deploy on GSA["x,y,z"] 
     *  can deploy on GSA[] without any zone
     *  but cannot deploy on GSA["x"]
     */
    //UNIMPLEMENTED
    //ElasticDeploymentTopology zone(String zone);

   /**
    * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
    * unit configuration.
    */
    ElasticDeploymentTopology setContextProperty(String key, String value);

    /**
     * Will deploy a secured processing unit. Note, by setting user details the processing unit will be secured automatically.
     */
    ElasticDeploymentTopology secured(boolean secured);

    /**
     * Advanced: Sets the security user details for authentication and authorization of the
     * processing unit.
     */
    ElasticDeploymentTopology userDetails(UserDetails userDetails);

    /**
     * Advanced: Sets the security user details for authentication and authorization of the
     * processing unit.
     */ 
    ElasticDeploymentTopology userDetails(String userName, String password);

    /**
     * Sets the deployment isolation constraint to dedicated. 
     * Dedicated isolation ensures that only processing unit instances from the same processing units can share the same machine.
     * Dedicated is the default deployment isolation. 
     */
    //UNIMPLEMENTED
    //ElasticDeploymentTopology isolation(DedicatedIsolation isolation);

    /**
     * Sets the deployment isolation constraint to shared. 
     * Shared isolation ensures that only processing unit instances from the same tenant can share the same machine.
     */
    //UNIMPLEMENTED
    //ElasticDeploymentTopology isolation(SharedTenantIsolation isolation);

    /**
     * Sets the deployment isolation constraint to public. 
     * Public isolation ensures that only processing unit instances with public isolation can share the same machine.
     */
    //UNIMPLEMENTED
    //ElasticDeploymentTopology isolation(PublicIsolation isolation);

    /**
     * Will cause the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started using a script
     * and not a pure Java process.
     */
    ElasticDeploymentTopology useScript();

    /**
     * Will cause JVM options added using {@link #commandLineArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    ElasticDeploymentTopology overrideCommandLineArguments();

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    ElasticDeploymentTopology commandLineArgument(String vmInputArgument);

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    ElasticDeploymentTopology environmentVariable(String name, String value);
    
    /**
     * Enables the server side bean that starts and stops machines automatically.
     * For example, the bean could delegate the request to a cloud provider.  
     */
    ElasticDeploymentTopology machineProvisioning(BeanConfig config);

}