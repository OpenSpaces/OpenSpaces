package org.openspaces.interop;

import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.events.notify.NotifyEventContainerServiceDetails;
import org.openspaces.events.notify.NotifyEventContainerServiceMonitors;
import org.openspaces.events.polling.PollingEventContainerServiceDetails;
import org.openspaces.events.polling.PollingEventContainerServiceMonitors;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.service.CustomServiceDetails;
import org.openspaces.pu.service.CustomServiceMonitors;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.openspaces.remoting.RemotingServiceDetails;
import org.openspaces.remoting.RemotingServiceDetails.RemoteService;
import org.openspaces.remoting.RemotingServiceMonitors;
import org.openspaces.remoting.RemotingServiceMonitors.RemoteServiceStats;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.serialization.pbs.commands.processingunit.PUDetailsHolder;
import com.gigaspaces.serialization.pbs.commands.processingunit.ServicesDetails;
import com.gigaspaces.serialization.pbs.commands.processingunit.ServicesMonitors;
import com.gigaspaces.serialization.pbs.openspaces.ProcessingUnitProxy;
import com.j_spaces.core.Constants;
import com.j_spaces.core.IJSpace;

/**
 * Dotnet processing unit bean, used as an adapter that will delegate
 * the life cycle method invocation to the .Net processing unit implementation of
 * the .Net GigaSpaces.Core.IProcessingUnit interface 
 * 
 * @author eitany
 * @since 6.5
 */
public class DotnetProcessingUnitBean implements InitializingBean, DisposableBean, ClusterInfoAware, BeanLevelPropertiesAware,
        ServiceDetailsProvider, ServiceMonitorsProvider {
    
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
            
            this.customProperties.putAll(beanLevelProperties.getMergedBeanProperties("space"));
            //Insert security details if needed
            MarshalledObject marshalledUserDetails = (MarshalledObject)customProperties.remove(Constants.Security.USER_DETAILS);
            if (marshalledUserDetails != null)
            {
                UserDetails userDetails = (UserDetails) marshalledUserDetails.get();
                this.customProperties.put(Constants.Security.USERNAME, userDetails.getUsername());
                this.customProperties.put(Constants.Security.PASSWORD, userDetails.getPassword());
            }
        }
        //Create identifier for this bean
        UUID beanUniqueIdentifier = UUID.randomUUID();
        log.debug("Invoking Init on the .Net processing unit, pu context " + beanUniqueIdentifier);
        if (clusterInfo == null) {
            proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, beanUniqueIdentifier);
        } else {                
            proxy = new ProcessingUnitProxy(assemblyFile, implementationClassName, dependencies, deploymentPath, customProperties, clusterInfo.getBackupId(), clusterInfo.getInstanceId(), clusterInfo.getNumberOfBackups(), clusterInfo.getNumberOfInstances(), clusterInfo.getSchema(), clusterInfo.getName(), beanUniqueIdentifier);
        }
        //Remove security from properties
        this.customProperties.remove(Constants.Security.USERNAME);
        this.customProperties.remove(Constants.Security.PASSWORD);
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
            String[] ids = details.getIds();            
            String[] serviceTypes = details.getServiceTypes();
            String[] subServiceTypes = details.getSubServiceTypes();
            String[] descriptions = details.getDescriptions();
            String[] longDescriptions = details.getLongDescriptions();
            Map<String, String>[] properties = details.getProperties();            
            for(int i = 0; i < descriptions.length; ++i)
            {
                serviceDetails.add(transformDetails(ids[i], serviceTypes[i], subServiceTypes[i], descriptions[i], longDescriptions[i], properties[i]));
            }
        }
    }

    private static final String REMOTING_SERVICE_DETAILS = "remoting";
    private static final String EVENT_CONTAINER_SERVICE_DETAILS = "event-container";
    private static final String POLLING_CONTAINER_SERVICE_DETAILS = "polling";
    private static final String NOTIFY_CONTAINER_SERVICE_DETAILS = "notify";    
    
    private ServiceDetails transformDetails(String id, String serviceType, String subServiceType, String description,
            String longDescription, Map<String, String> properties) {
        if (REMOTING_SERVICE_DETAILS.equals(serviceType))           
            return buildRemotingServiceDetails(id, properties);
        if (EVENT_CONTAINER_SERVICE_DETAILS.equals(serviceType))
        {
            if (POLLING_CONTAINER_SERVICE_DETAILS.equals(subServiceType))
                return buildPollingContainerServiceDetails(id, properties);
            if (NOTIFY_CONTAINER_SERVICE_DETAILS.equals(subServiceType))
                return buildNotifyContainerServiceDetails(id, properties);
        }
        
        return new CustomServiceDetails(id, serviceType, subServiceType, description, longDescription);
    }
    
    private ServiceDetails buildNotifyContainerServiceDetails(String id, Map<String, String> props) {        
        String space = props.get("space");
        Object template = props.get("template");
        boolean performSnapshot = Boolean.parseBoolean(props.get("perform-snapshot"));
        String txManager = props.get("transaction-manager");
        boolean batchProcessing = Boolean.parseBoolean(props.get("pass-array-as-is"));
        String commTypeStr = props.get("comm-type");
        int commType = 0;
        if ("unicast".equals(commTypeStr))
            commType = 0;
        else if ("multiplex".equals(commTypeStr))
            commType = 1;
        else if ("multicast".equals(commTypeStr))
            commType = 2;        
        boolean fifo = Boolean.parseBoolean(props.get("fifo"));
        Integer batchSize = props.get("batch-size") == null? null : Integer.parseInt("batch-size");
        Integer batchTime = props.get("batch-time") == null? null : Integer.parseInt("batch-time");
        boolean autoRenew = Boolean.parseBoolean(props.get("auto-renew"));
        boolean notifyWrite = Boolean.parseBoolean(props.get("notify-write"));
        boolean notifyUpdate = Boolean.parseBoolean(props.get("notify-update"));;
        boolean notifyTake = Boolean.parseBoolean(props.get("notify-take"));;
        boolean notifyLease = Boolean.parseBoolean(props.get("notify-lease-expire"));;
        Boolean triggerNotifyTemplate = props.get("trigger-notify-template") == null? null : Boolean.parseBoolean(props.get("trigger-notify-template"));
        Boolean replicateNotifyTemplate = props.get("replicate-notify-template") == null? null : Boolean.parseBoolean(props.get("replicate-notify-template"));
        boolean performTakeOnNotify = Boolean.parseBoolean(props.get("perform-take-on-notify"));
        boolean guaranteed = Boolean.parseBoolean(props.get("guaranteed"));
        return new NotifyEventContainerServiceDetails(id, space, template, performSnapshot, txManager, commType, fifo, batchSize, 
                    batchTime, autoRenew, null, notifyWrite, notifyUpdate, notifyTake, notifyLease, false, triggerNotifyTemplate, 
                    replicateNotifyTemplate, performTakeOnNotify, batchProcessing, guaranteed);
    }

    private PollingEventContainerServiceDetails buildPollingContainerServiceDetails(String id, Map<String, String> props) {
        String space = props.get("space");
        Object template = props.get("template");
        boolean performSnapshot = Boolean.parseBoolean(props.get("perform-snapshot"));
        String txManager = props.get("transaction-manager");
        long receiveTimeout = Long.parseLong(props.get("receive-timeout"));
        String receiveOperationHandler = props.get("receive-operating-handler");
        String triggerOperationHandler = props.get("trigger-operating-handler");
        int minConcurrentConsumers = Integer.parseInt(props.get("concurrent-consumers"));
        int maxConcurrentConsumer = Integer.parseInt(props.get("max-concurrent-consumers"));
        boolean batchProcessing = Boolean.parseBoolean(props.get("pass-array-as-is"));
        return  new PollingEventContainerServiceDetails(id, space, template, performSnapshot, txManager, receiveTimeout, 
                receiveOperationHandler, triggerOperationHandler, minConcurrentConsumers, maxConcurrentConsumer, batchProcessing);
    }

    private RemotingServiceDetails buildRemotingServiceDetails(String id, Map<String, String> properties) {
        ArrayList<RemoteService> remoteServices = new ArrayList<RemoteService>();
        for(Map.Entry<String, String> entry : properties.entrySet())
        {
            String serviceId = entry.getKey();
            String className = entry.getValue();
            //Error, should not happen
            if (className == null)
                className = "unknown";
            RemoteService remoteService = new RemoteService(serviceId, className);
            remoteServices.add(remoteService);
        }
        
        return new RemotingServiceDetails(id, remoteServices.toArray(new RemoteService[remoteServices.size()]));
    }

    public ServiceMonitors[] getServicesMonitors() {
        ServicesMonitors servicesMonitors = proxy.getServicesMonitors();
        ServiceMonitors[] result = new ServiceMonitors[servicesMonitors.getIds().length];
        for(int i = 0; i < result.length; ++i)
            result[i] = transformMonitors(servicesMonitors.getInteropId()[i], servicesMonitors.getIds()[i], servicesMonitors.getServicesMonitors()[i]);
        return result;
    }

    private static final String REMOTING_SERVICE_MONITORS = "RemotingServiceMonitors";
    private static final String POLLING_CONTAINER_MONITORS = "PollingEventContainerServiceMonitors";
    private static final String NOTIFY_CONTAINER_MONITORS = "NotifyEventContainerServiceMonitors";
    
    private ServiceMonitors transformMonitors(String interopId, String id, Map<String, String> monitors) {
        if (REMOTING_SERVICE_MONITORS.equals(interopId))
        {            
            String totalProcessedStr = monitors.remove("processed");
            String totalFailedStr = monitors.remove("failed");
            int totalProc = totalProcessedStr == null? 0 : Integer.parseInt(totalProcessedStr);
            int totalFail = totalFailedStr == null? 0 : Integer.parseInt(totalFailedStr);            
            Set<String> servicesIds = new HashSet<String>();            
            for(Map.Entry<String, String> entry : monitors.entrySet())
            {
                String[] split = entry.getKey().split("#");
                String serviceId = split[0];
                servicesIds.add(serviceId);
            }
            ArrayList<RemoteServiceStats> stats = new ArrayList<RemoteServiceStats>(monitors.size());
            for(String serviceId : servicesIds)
            {                                
                String serviceProcStr = monitors.get(serviceId + "#processed");
                String serviceFailStr = monitors.get(serviceId + "#failed");
                int proc = serviceProcStr == null? 0 : Integer.parseInt(serviceProcStr);
                int fail = serviceFailStr == null? 0 : Integer.parseInt(serviceFailStr);
                stats.add(new RemoteServiceStats(serviceId, proc, fail));
            }
            
            return new RemotingServiceMonitors(id, totalProc, totalFail, stats.toArray(new RemoteServiceStats[stats.size()]));
        }
        if (POLLING_CONTAINER_MONITORS.equals(interopId))
        {            
            long procEvent = Long.parseLong(monitors.get("processedEvents"));
            long failEvent = Long.parseLong(monitors.get("failedEvents"));
            int activeConsumers = Integer.parseInt(monitors.get("consumers"));
            String status = monitors.get("status");
            
            return new PollingEventContainerServiceMonitors(id, procEvent, failEvent, status, activeConsumers);
        }
        if (NOTIFY_CONTAINER_MONITORS.equals(interopId))
        {            
            long procEvent = Long.parseLong(monitors.get("processedEvents"));
            long failEvent = Long.parseLong(monitors.get("failedEvents"));            
            String status = monitors.get("status");
            
            return new NotifyEventContainerServiceMonitors(id, procEvent, failEvent, status);
        }
        
        CustomServiceMonitors plainServiceMonitors = new CustomServiceMonitors(id);
        Map<String, Object> plainMonitors = plainServiceMonitors.getMonitors();
        for(Map.Entry<String, String> entry : monitors.entrySet())
            plainMonitors.put(entry.getKey(), entry.getValue());
        
        return plainServiceMonitors;
    }
}
