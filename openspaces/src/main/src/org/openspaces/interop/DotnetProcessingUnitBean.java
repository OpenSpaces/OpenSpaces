package org.openspaces.interop;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.ServiceClassLoader;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import com.gigaspaces.serialization.pbs.openspaces.ProcessingUnitProxy; 

/**
 * Dotnet processing unit bean, used as an adapter that will delegate
 * the life cycle method invocation to the .Net processing unit implementation of
 * the .Net GigaSpaces.Core.IProcessingUnit interface 
 * 
 * @author eitany
 * @since 6.5
 */
public class DotnetProcessingUnitBean implements InitializingBean, DisposableBean, ClusterInfoAware {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private ProcessingUnitProxy proxy;
    
    private String assemblyFile;
    
    private String implementationClassName;
    
    private String[] dependencies;
    
    private String deploymentDirectory;
    
    private ClusterInfo clusterInfo;
    
    private Properties customProperties;

    /**
     * Injects the .Net processing unit implementation's assembly file
     * 
     * @param assemblyFile
     */
    public void setAssemblyFile(String assemblyFile) {
        this.assemblyFile = assemblyFile;
    }

    /**
     * Injects the .Net processing unit implementation class name
     * 
     * @param implementationName
     */
    public void setImplementationClassName(String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    /**
     * Injects the .Net processing unit implementation's dependencies
     * 
     * @param dependencies
     */
    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
    
    /**
     * @param deploymentDirectory the deploymentDirectory to set
     */
    public void setDeploymentDirectory(String deploymentDirectory) {
        this.deploymentDirectory = deploymentDirectory;
    }    
    
    /**
     * Injects the .Net processing unit properties that will be passed
     * to the init method
     * 
     * @param customProperties
     */
    public void setCustomProperties(Properties customProperties)
    {
        this.customProperties = customProperties;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }
	/**
	 * {@inheritDoc}
	 */
    public void afterPropertiesSet() throws Exception {  
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader instanceof ServiceClassLoader) {
                Thread.currentThread().setContextClassLoader(classLoader.getParent());
            }            
            log.info("Invoking Init on the .Net processing unit");
            if (clusterInfo == null) {
                log.info("Invoking Init on the .Net processing unit");
                proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentDirectory, customProperties);
            } else {
                
                proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentDirectory, customProperties, clusterInfo.getBackupId(), clusterInfo.getInstanceId(), clusterInfo.getNumberOfBackups(), clusterInfo.getNumberOfInstances(), clusterInfo.getSchema(), clusterInfo.getName());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
	/**
	 * {@inheritDoc}
	 */
    public void destroy() throws Exception {
        log.info("Invoking Dispose on the .Net processing unit");
        proxy.close();
        proxy = null;
    }
	

}
