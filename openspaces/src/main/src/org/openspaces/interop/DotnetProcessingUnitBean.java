package org.openspaces.interop;

import com.gigaspaces.serialization.pbs.commands.processingunit.PUDetailsHolder;
import com.gigaspaces.serialization.pbs.commands.processingunit.ServicesDetails;
import com.gigaspaces.serialization.pbs.openspaces.ProcessingUnitProxy;
import com.j_spaces.core.IJSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.ServiceClassLoader;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.SpaceProvider;
import org.openspaces.pu.container.servicegrid.PUServiceDetails;
import org.openspaces.pu.container.servicegrid.ServiceDetailsProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

/**
 * Dotnet processing unit bean, used as an adapter that will delegate
 * the life cycle method invocation to the .Net processing unit implementation of
 * the .Net GigaSpaces.Core.IProcessingUnit interface 
 * 
 * @author eitany
 * @since 6.5
 */
public class DotnetProcessingUnitBean implements InitializingBean, DisposableBean, ClusterInfoAware, BeanLevelPropertiesAware,
        SpaceProvider, ServiceDetailsProvider {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private ProcessingUnitProxy proxy;
    
    private String assemblyFile;
    
    private String implementationClassName;
    
    private String[] dependencies;
    
    private String deploymentPath;
    
    private ClusterInfo clusterInfo;
    
    private Properties customProperties;

    private BeanLevelProperties beanLevelProperties;

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
        this.deploymentPath = deploymentDirectory;
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
        //Try to get deployment path if not set, relevant for interop pu scenario.
        if (this.deploymentPath == null && beanLevelProperties != null)
        {
            this.deploymentPath = beanLevelProperties.getContextProperties().getProperty(DeployableProcessingUnitContainerProvider.CONTEXT_PROPERTY_DEPLOY_PATH);            
        }
        if (deploymentPath != null)
            log.info("Deployment path taken from " + DeployableProcessingUnitContainerProvider.CONTEXT_PROPERTY_DEPLOY_PATH + " property (" + this.deploymentPath + ")");
        
        //Merge beanLevelProperties with custom properties
        if (this.beanLevelProperties != null)
        {
            if (this.customProperties == null)
                this.customProperties = new Properties();
            
            this.customProperties.putAll(beanLevelProperties.getContextProperties());
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader instanceof ServiceClassLoader) {
                Thread.currentThread().setContextClassLoader(classLoader.getParent());
            }
            //Create identifier for this bean
            UUID beanUniqueIdentifier = UUID.randomUUID();
            log.info("Invoking Init on the .Net processing unit");
            if (clusterInfo == null) {
                log.info("Invoking Init on the .Net processing unit");
                proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, beanUniqueIdentifier);
            } else {                
                proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, clusterInfo.getBackupId(), clusterInfo.getInstanceId(), clusterInfo.getNumberOfBackups(), clusterInfo.getNumberOfInstances(), clusterInfo.getSchema(), clusterInfo.getName(), beanUniqueIdentifier);
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
    /**
     * {@inheritDoc}
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }
    /**
     * {@inheritDoc}
     */
    public IJSpace[] getSpaces() {
        return proxy.getContextProxies();
    }

    public PUServiceDetails[] getServicesDetails() {
        PUDetailsHolder puDetails = proxy.getPUDetailsHolder();
        ArrayList<PUServiceDetails> dotnetServiceDetails = new ArrayList<PUServiceDetails>();
        dotnetServiceDetails.add(new DotnetPUContainerServiceDetails("interop", puDetails.getDotnetPUContainerQualifiedName()));
        BuildServiceDetails(puDetails, dotnetServiceDetails);
        return dotnetServiceDetails.toArray(new PUServiceDetails[dotnetServiceDetails.size()]);
    }

    private void BuildServiceDetails(PUDetailsHolder puDetails, ArrayList<PUServiceDetails> serviceDetails) {
        ServicesDetails details = puDetails.getServicesDetails();
        if (details != null)
        {
            String[] types = details.getTypes();
            String[] serviceTypes = details.getServiceTypes();
            String[] descriptions = details.getDescriptions();
            for(int i = 0; i < descriptions.length; ++i)
            {
                serviceDetails.add(new DotnetServiceDetails(types[i], serviceTypes[i], descriptions[i]));
            }
        }
    }
}
