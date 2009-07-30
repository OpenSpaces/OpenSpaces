package org.openspaces.interop;

import com.gigaspaces.serialization.pbs.commands.processingunit.PUDetailsHolder;
import com.gigaspaces.serialization.pbs.commands.processingunit.ServicesDetails;
import com.gigaspaces.serialization.pbs.openspaces.ProcessingUnitProxy;
import com.j_spaces.core.IJSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
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
        ServiceDetailsProvider {
    
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
     */
    public void setAssemblyFile(String assemblyFile) {
        this.assemblyFile = assemblyFile;
    }

    /**
     * Injects the .Net processing unit implementation class name
     */
    public void setImplementationClassName(String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    /**
     * Injects the .Net processing unit implementation's dependencies
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
        //Create identifier for this bean
        UUID beanUniqueIdentifier = UUID.randomUUID();
        log.debug("Invoking Init on the .Net processing unit");
        if (clusterInfo == null) {
            proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, beanUniqueIdentifier);
        } else {                
            proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, clusterInfo.getBackupId(), clusterInfo.getInstanceId(), clusterInfo.getNumberOfBackups(), clusterInfo.getNumberOfInstances(), clusterInfo.getSchema(), clusterInfo.getName(), beanUniqueIdentifier);
        }
    }
	/**
	 * {@inheritDoc}
	 */
    public void destroy() throws Exception {
        log.debug("Invoking Dispose on the .Net processing unit");
        proxy.close();
        proxy = null;
    }
    
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public ServiceDetails[] getServicesDetails() {
        PUDetailsHolder puDetails = proxy.getPUDetailsHolder();
        ArrayList<ServiceDetails> dotnetServiceDetails = new ArrayList<ServiceDetails>();
        dotnetServiceDetails.add(new DotnetContainerServiceDetails(puDetails.getDotnetPUContainerShortName(), "interop", puDetails.getDotnetPUContainerShortName(), puDetails.getDotnetPUContainerQualifiedName()));
        for (IJSpace space : proxy.getContextProxies()) {
            dotnetServiceDetails.add(new SpaceServiceDetails(space));
        }
        buildServiceDetails(puDetails, dotnetServiceDetails);
        return dotnetServiceDetails.toArray(new ServiceDetails[dotnetServiceDetails.size()]);
    }

    private void buildServiceDetails(PUDetailsHolder puDetails, ArrayList<ServiceDetails> serviceDetails) {
        ServicesDetails details = puDetails.getServicesDetails();
        if (details != null)
        {
            String[] types = details.getTypes();
            String[] serviceTypes = details.getServiceTypes();
            String[] descriptions = details.getDescriptions();
            String[] longDescriptions = details.getLongDescriptions();
            for(int i = 0; i < descriptions.length; ++i)
            {
                // TODO pass ids from dotnet
                serviceDetails.add(new DotnetServiceDetails(descriptions[i], types[i], serviceTypes[i], descriptions[i], longDescriptions[i]));
            }
        }
    }
}
