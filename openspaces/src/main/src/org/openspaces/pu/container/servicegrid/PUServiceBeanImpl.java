/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.servicegrid;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMHelper;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoHelper;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSHelper;
import com.gigaspaces.operatingsystem.OSStatistics;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.kernel.Environment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.jini.rio.boot.CommonClassLoader;
import org.jini.rio.boot.ServiceClassLoader;
import org.jini.rio.boot.SharedServiceData;
import org.jini.rio.core.JSBInstantiationException;
import org.jini.rio.core.SLA;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.watch.Calculable;
import org.jini.rio.watch.GaugeWatch;
import org.jini.rio.watch.Watch;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.interop.DotnetProcessingUnitContainerProvider;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ClassLoaderAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.BeanLevelPropertiesUtils;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.WebsterFile;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.openspaces.pu.sla.monitor.ApplicationContextMonitor;
import org.openspaces.pu.sla.monitor.Monitor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author kimchy
 */
public class PUServiceBeanImpl extends ServiceBeanAdapter implements PUServiceBean {

    private static final Log logger = LogFactory.getLog(PUServiceBeanImpl.class);

    private volatile ProcessingUnitContainer container;

    private int clusterGroup;

    private Integer instanceId;

    private Integer backupId;

    private volatile List<WatchTask> watchTasks = new ArrayList<WatchTask>();

    private volatile ScheduledExecutorService executorService;

    private volatile MemberAliveIndicator[] memberAliveIndicators;

    private volatile File deployPath;

    private ClusterInfo clusterInfo;

    private volatile PUDetails puDetails;

    private List<Callable> serviceMonitors = new ArrayList<Callable>();

    public PUServiceBeanImpl() {
        super();
    }


    @Override
    protected Object doStart(ServiceBeanContext context) throws Exception {
        this.context = context;
        org.openspaces.pu.sla.SLA sla = getSLA(context);
        if (sla.getMonitors() != null) {
            for (Monitor monitor : sla.getMonitors()) {
                String watchName = monitor.getName();
                Watch watch = new GaugeWatch(watchName, context.getConfiguration());
                watch.getWatchDataSource().setSize(monitor.getHistorySize());
                context.getWatchRegistry().register(watch);
                watchTasks.add(new WatchTask(monitor, watch));
            }
        }

        String springXML = (String) context.getInitParameter("pu");
        clusterGroup = Integer.parseInt((String) context.getInitParameter("clusterGroup"));
        String sInstanceId = (String) context.getInitParameter("instanceId");
        if (sInstanceId != null) {
            instanceId = Integer.valueOf(sInstanceId);
        }
        String sBackupId = (String) context.getInitParameter("backupId");
        if (sBackupId != null) {
            backupId = Integer.valueOf(sBackupId);
        }

        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            startPU(springXML);
        } catch (Exception e) {
            logger.error(logMessage("Failed to start PU with xml [" + springXML + "]"), e);
            try {
                destroy();
            } catch (Exception e1) {
                logger.debug(logMessage("Failed to destroy PU after failed start, ignoring"), e1);
            }
            throw new JSBInstantiationException("Failed to start processing unit [" + getServiceBeanContext().getServiceElement().getName() + "]", e, false);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }

        try {
            // we set the context class loader so we export with it
            ClassLoader webAppClassLoader = SharedServiceData.removeWebAppClassLoader(clusterInfo.getName() + clusterInfo.getRunningNumber());
            if (webAppClassLoader != null) {
                contextClassLoader = webAppClassLoader;
            }
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return super.doStart(context);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    @Override
    public void stop(boolean force) {
        // make sure to clean the web app class loader
        if (clusterInfo != null) {
            SharedServiceData.removeWebAppClassLoader(clusterInfo.getName() + clusterInfo.getRunningNumber());
        }

        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            stopPU();
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);

            for (WatchTask watchTask : watchTasks) {
                watchRegistry.deregister(watchTask.getWatch());
            }
            watchTasks.clear();
        }

        super.stop(force);
    }

    private void startPU(String springXml) throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage("Starting PU with [" + springXml + "]"));
        }

        String puName = (String) context.getInitParameter("puName");
        String puPath = (String) context.getInitParameter("puPath");
        String codeserver = context.getExportCodebase();

        org.openspaces.pu.sla.SLA sla = getSLA(getServiceBeanContext());

        Integer instanceId = this.instanceId;
        Integer backupId = this.backupId;

        // Derive instanceId and backupId if not explicitly set
        if (instanceId == null) {
            boolean hasBackups = sla.getNumberOfBackups() > 0;
            if (hasBackups) {
                instanceId = clusterGroup;
                //the first instance is primary so no backupid
                if (context.getServiceBeanConfig().getInstanceID().intValue() > 1) {
                    backupId = (context.getServiceBeanConfig().getInstanceID().intValue() - 1);
                }
            } else {
                instanceId = context.getServiceBeanConfig().getInstanceID().intValue();
            }
        }

        //set cluster info
        clusterInfo = new ClusterInfo();
        String clusterSchema = sla.getClusterSchema();
        if (clusterSchema != null) {
            clusterInfo.setSchema(clusterSchema);
            int slaMax = getSLAMax(context);
            int numberOfInstances = Math.max(slaMax, sla.getNumberOfInstances());
            clusterInfo.setNumberOfInstances(numberOfInstances);
        } else {
            clusterInfo.setNumberOfInstances(sla.getNumberOfInstances());
        }
        clusterInfo.setNumberOfBackups(sla.getNumberOfBackups());
        clusterInfo.setInstanceId(instanceId);
        clusterInfo.setBackupId(backupId);
        clusterInfo.setName(puName);

        ClusterInfoParser.guessSchema(clusterInfo);

        logger.info(logMessage("ClusterInfo [" + clusterInfo + "]"));

        MarshalledObject beanLevelPropertiesMarshObj =
                (MarshalledObject) getServiceBeanContext().getInitParameter("beanLevelProperties");
        BeanLevelProperties beanLevelProperties = null;
        if (beanLevelPropertiesMarshObj != null) {
            beanLevelProperties = (BeanLevelProperties) beanLevelPropertiesMarshObj.get();
            logger.info(logMessage("BeanLevelProperties " + beanLevelProperties));
        } else {
            beanLevelProperties = new BeanLevelProperties();
        }
        beanLevelProperties.getContextProperties().putAll(ClusterInfoPropertyPlaceholderConfigurer.createProperties(clusterInfo));

        // set a generic work location that can be used by container providers
        File workLocation = new File(System.getProperty("com.gs.work", Environment.getHomeDirectory() + "/work"));
        workLocation.mkdirs();

        beanLevelProperties.getContextProperties().setProperty("com.gs.work", workLocation.getAbsolutePath());

        boolean downloadPU = false;
        //create PU Container
        ProcessingUnitContainerProvider factory;
        // identify if this is a web app
        InputStream webXml = null;
        try {
            webXml = new URL(codeserver + puPath + "/WEB-INF/web.xml").openStream();
        } catch (IOException e) {
            // does not exists
        }
        // identify if this is a .NET one
        InputStream puConfig = null;
        try {
            puConfig = new URL(codeserver + puPath + "/pu.config").openStream();
        } catch (IOException e) {
            // does not exists
        }
        // identify if this is a .NET interop one
        InputStream puInteropConfig = null;
        try {
            puInteropConfig = new URL(codeserver + puPath + "/pu.interop.config").openStream();
        } catch (IOException e) {
            // does not exists
        }
        String processingUnitContainerProviderClass;
        if (webXml != null) {
            webXml.close();
            downloadPU = true;
            String jeeContainer = beanLevelProperties.getContextProperties().getProperty("jee.container", "jetty");
            // setup class loaders correcly
            try {
                ((ServiceClassLoader) contextClassLoader).setParentClassLoader(SharedServiceData.getJeeClassLoader(jeeContainer));
            } catch (Exception e) {
                throw new CannotCreateContainerException("Failed to configure JEE class loader", e);
            }

            String className = StringUtils.capitalize(jeeContainer) + "JeeProcessingUnitContainerProvider";
            processingUnitContainerProviderClass = "org.openspaces.pu.container.jee." + jeeContainer + "." + className;
        } else if (puConfig != null) {
            puConfig.close();
            downloadPU = true;
            processingUnitContainerProviderClass = DotnetProcessingUnitContainerProvider.class.getName();
        } else if (puInteropConfig != null) {
            puInteropConfig.close();
            downloadPU = true;
            processingUnitContainerProviderClass = IntegratedProcessingUnitContainerProvider.class.getName();
        } else {
            processingUnitContainerProviderClass = IntegratedProcessingUnitContainerProvider.class.getName();
            if (beanLevelProperties.getContextProperties().getProperty("pu.download", "true").equalsIgnoreCase("true")) {
                downloadPU = true;
            }
        }

        if (beanLevelProperties != null) {
            processingUnitContainerProviderClass = beanLevelProperties.getContextProperties().getProperty(ProcessingUnitContainerProvider.CONTAINER_CLASS_PROP, processingUnitContainerProviderClass);
        }

        if (downloadPU) {
            String deployName = puName + "_" + clusterInfo.getRunningNumberOffset1();

            String deployedProcessingUnitsLocation = workLocation.getAbsolutePath() + "/processing-units";

            deployPath = new File(deployedProcessingUnitsLocation + "/" + deployName);
            FileSystemUtils.deleteRecursively(deployPath);
            deployPath.mkdirs();

            // backward compatible
            beanLevelProperties.getContextProperties().setProperty("jee.deployPath", deployPath.getAbsolutePath());
            beanLevelProperties.getContextProperties().setProperty("dotnet.deployPath", deployPath.getAbsolutePath());

            beanLevelProperties.getContextProperties().setProperty(DeployableProcessingUnitContainerProvider.CONTEXT_PROPERTY_DEPLOY_PATH, deployPath.getAbsolutePath());

            downloadAndExtractPU(puName, puPath, codeserver, deployPath, new File(deployedProcessingUnitsLocation));

            // go over listed files that needs to be resovled with properties
            for (Map.Entry entry : beanLevelProperties.getContextProperties().entrySet()) {
                String key = (String) entry.getKey();
                if (key.startsWith("com.gs.resolvePlaceholder")) {
                    String path = (String) entry.getValue();
                    File input = new File(deployPath, path);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resolving placeholder for file [" + input.getAbsolutePath() + "]");
                    }
                    BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, input);
                }
            }
        }

        CommonClassLoader commonClassLoader = CommonClassLoader.getInstance();
        // handles class loader libraries
        if (downloadPU) {
            List<URL> libUrls = new ArrayList<URL>();
            File libDir = new File(deployPath, "lib");
            if (libDir.exists()) {
                File[] libFiles = libDir.listFiles();
                for (File libFile : libFiles) {
                    libUrls.add(libFile.toURI().toURL());
                }
            }
            ((ServiceClassLoader) contextClassLoader).setSlashPath(deployPath.toURI().toURL());
            ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
            }
            // add to common class loader
            List<URL> sharedlibUrls = new ArrayList<URL>();
            File sharedlibDir = new File(deployPath, "shared-lib");
            if (sharedlibDir.exists()) {
                File[] sharedlibFiles = sharedlibDir.listFiles();
                for (File sharedlibFile : sharedlibFiles) {
                    sharedlibUrls.add(sharedlibFile.toURI().toURL());
                }
            }
            sharedlibDir = new File(deployPath, "WEB-INF/shared-lib");
            if (sharedlibDir.exists()) {
                File[] sharedlibFiles = sharedlibDir.listFiles();
                for (File sharedlibFile : sharedlibFiles) {
                    sharedlibUrls.add(sharedlibFile.toURI().toURL());
                }
            }
            commonClassLoader.addComponent(puName, sharedlibUrls.toArray(new URL[sharedlibUrls.size()]));
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage("Common Class Loader " + sharedlibUrls));
            }
            try {
                prepareWebApplication(deployPath, clusterInfo, beanLevelProperties);
            } catch (Exception e) {
                throw new CannotCreateContainerException("Failed to bootstrap web applciation", e);
            }

        } else {
            // add to service class loader
            List<URL> libUrls = new ArrayList<URL>();
            WebsterFile libDir = new WebsterFile(new URL(codeserver + puPath + "/lib"));
            File[] libFiles = libDir.listFiles();
            for (int i = 0; i < libFiles.length; i++) {
                libUrls.add(new URL(codeserver + puPath + "/lib/" + libFiles[i].getName()));
            }
            ((ServiceClassLoader) contextClassLoader).setSlashPath(new URL(codeserver + puPath + "/"));
            ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
            }
            // add to common class loader
            WebsterFile sharedlibDir = new WebsterFile(new URL(codeserver + puPath + "/shared-lib"));
            File[] sharedlibFiles = sharedlibDir.listFiles();
            List<URL> sharedlibUrls = new ArrayList<URL>();
            for (File sharedlibFile : sharedlibFiles) {
                sharedlibUrls.add(new URL(codeserver + puPath + "/shared-lib/" + sharedlibFile.getName()));
            }
            sharedlibDir = new WebsterFile(new URL(codeserver + puPath + "/WEB-INF/shared-lib"));
            sharedlibFiles = sharedlibDir.listFiles();
            for (File sharedlibFile : sharedlibFiles) {
                sharedlibUrls.add(new URL(codeserver + puPath + "/WEB-INF/shared-lib/" + sharedlibFile.getName()));
            }
            ((CommonClassLoader) (contextClassLoader.getParent())).addComponent(puName, sharedlibUrls.toArray(new URL[sharedlibUrls.size()]));
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage("Common Class Loader " + sharedlibUrls));
            }
        }

        // handle mule os if there is one class loader
        try {
            ClassUtils.forName("org.mule.api.MuleContext");
            ((ServiceClassLoader) contextClassLoader).addURLs(BootUtil.toURLs(new String[]
                    {
                            Environment.getHomeDirectory() + "/lib/optional/openspaces/mule-os.jar"
                    }));
        } catch (Exception e) {
            // no mule
        }

        factory = createContainerProvider(processingUnitContainerProviderClass);
        if (factory instanceof DeployableProcessingUnitContainerProvider) {
            ((DeployableProcessingUnitContainerProvider) factory).setDeployPath(deployPath);
        }
        if (factory instanceof ClassLoaderAwareProcessingUnitContainerProvider) {
            ((ClassLoaderAwareProcessingUnitContainerProvider) factory).setClassLoader(contextClassLoader);
        }

        if (factory instanceof ApplicationContextProcessingUnitContainerProvider) {
            if (StringUtils.hasText(springXml)) {
                Resource resource = new ByteArrayResource(springXml.getBytes());
                ((ApplicationContextProcessingUnitContainerProvider) factory).addConfigLocation(resource);
            }
        }
        if (factory instanceof ClusterInfoAware) {
            ((ClusterInfoAware) factory).setClusterInfo(clusterInfo);
        }
        if (factory instanceof BeanLevelPropertiesAware) {
            ((BeanLevelPropertiesAware) factory).setBeanLevelProperties(beanLevelProperties);
        }

        container = factory.createContainer();

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ApplicationContext applicationContext = ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            Map map = applicationContext.getBeansOfType(MemberAliveIndicator.class);
            ArrayList<MemberAliveIndicator> maiList = new ArrayList<MemberAliveIndicator>();
            for (Iterator it = map.values().iterator(); it.hasNext();) {
                MemberAliveIndicator memberAliveIndicator = (MemberAliveIndicator) it.next();
                if (memberAliveIndicator.isMemberAliveEnabled()) {
                    maiList.add(memberAliveIndicator);
                }
            }
            memberAliveIndicators = maiList.toArray(new MemberAliveIndicator[maiList.size()]);

            // inject the application context to all the monitors and schedule them
            // currently use the number of threads in relation to the number of monitors
            int numberOfThreads = watchTasks.size() / 5;
            if (numberOfThreads == 0) {
                numberOfThreads = 1;
            }
            executorService = Executors.newScheduledThreadPool(numberOfThreads);
            for (WatchTask watchTask : watchTasks) {
                if (watchTask.getMonitor() instanceof ApplicationContextMonitor) {
                    ((ApplicationContextMonitor) watchTask.getMonitor()).setApplicationContext(applicationContext);
                }
                executorService.scheduleAtFixedRate(watchTask, watchTask.getMonitor().getPeriod(), watchTask.getMonitor().getPeriod(), TimeUnit.MILLISECONDS);
            }
        }

        ArrayList<Object> serviceDetails = new ArrayList<Object>();

        if (container instanceof ServiceDetailsProvider) {
            ServiceDetails[] details = ((ServiceDetailsProvider) container).getServicesDetails();
            if (details != null) {
                for (ServiceDetails detail : details) {
                    serviceDetails.add(detail);
                }
            }
        }

        if (container instanceof ServiceMonitorsProvider) {
            serviceMonitors.add(new Callable() {
                public Object call() throws Exception {
                    return ((ServiceMonitorsProvider) container).getServicesMonitors();
                }
            });
        }

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            Map map = applicationContext.getBeansOfType(ServiceDetailsProvider.class);
            for (Iterator it = map.values().iterator(); it.hasNext();) {
                ServiceDetails[] details = ((ServiceDetailsProvider) it.next()).getServicesDetails();
                if (details != null) {
                    for (ServiceDetails detail : details) {
                        serviceDetails.add(detail);
                    }
                }
            }

            final Map monitorsMap = applicationContext.getBeansOfType(ServiceMonitorsProvider.class);
            serviceMonitors.add(new Callable() {
                public Object call() throws Exception {
                    ArrayList<ServiceMonitors> retMonitors = new ArrayList<ServiceMonitors>();
                    for (Iterator it = monitorsMap.values().iterator(); it.hasNext();) {
                        ServiceMonitors[] monitors = ((ServiceMonitorsProvider) it.next()).getServicesMonitors();
                        if (monitors != null) {
                            for (ServiceMonitors mon : monitors) {
                                retMonitors.add(mon);
                            }
                        }
                    }
                    return retMonitors.toArray(new Object[retMonitors.size()]);
                }
            });
        }

        List<Callable> serviceDetailsProvider = SharedServiceData.removeServiceDetails(clusterInfo.getName() + clusterInfo.getRunningNumber());
        if (serviceDetailsProvider != null) {
            for (Callable serProvider : serviceDetailsProvider) {
                try {
                    Object[] details = (Object[]) serProvider.call();
                    Collections.addAll(serviceDetails, details);
                } catch (Exception e) {
                    logger.error("Failed to add service details from custom provider", e);
                }
            }
        }

        List<Callable> sharedMonitors = SharedServiceData.removeServiceMonitors(clusterInfo.getName() + clusterInfo.getRunningNumber());
        if (sharedMonitors != null) {
            serviceMonitors.addAll(sharedMonitors);
        }

        this.puDetails = new PUDetails(context.getParentServiceID(), clusterInfo, beanLevelProperties, serviceDetails.toArray(new Object[serviceDetails.size()]));
    }

    private org.openspaces.pu.sla.SLA getSLA(ServiceBeanContext context) throws IOException, ClassNotFoundException {
        MarshalledObject slaMarshObj = (MarshalledObject) context.getInitParameter("sla");
        return (org.openspaces.pu.sla.SLA) slaMarshObj.get();
    }

    private void stopPU() {
        serviceMonitors.clear();
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        if (container != null) {
            try {
                container.close();
            } catch (Exception e) {
                logger.warn(logMessage("Failed to close"), e);
            } finally {
                container = null;
                memberAliveIndicators = null;
                puDetails = null;
            }
        }

        // clean the deploy path directory
//        if (deployPath != null) {
//            boolean deleted = false;
//            for (int i = 0; i < 2; i++) {
//                deleted = FileSystemUtils.deleteRecursively(deployPath);
//                if (deleted) {
//                    break;
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    // do nothing
//                }
//            }
//            if (!deleted) {
//                logger.debug("Failed to delete deployed processing unit from [" + deployPath + "]");
//            }
//        }
    }

    public boolean isMemberAliveEnabled() {
        return (memberAliveIndicators != null && memberAliveIndicators.length > 0);
    }

    public boolean isAlive() {
        if (memberAliveIndicators == null || memberAliveIndicators.length == 0) {
            return true;
        }
        boolean alive = false;
        for (MemberAliveIndicator memberAliveIndicator : memberAliveIndicators) {
            try {
                alive = memberAliveIndicator.isAlive();
                if (!alive) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return alive;
    }

    public SpaceURL[] listSpacesURLs() throws RemoteException {
        List<SpaceURL> spaceUrls = new ArrayList<SpaceURL>();
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Field spaceDetails = serviceDetails.getClass().getDeclaredField("space");
                        spaceDetails.setAccessible(true);
                        IJSpace space = (IJSpace) spaceDetails.get(serviceDetails);
                        spaceUrls.add(space.getFinderURL());
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get space url", e);
                }
            }
        }
        return spaceUrls.toArray(new SpaceURL[spaceUrls.size()]);
    }

    public SpaceMode[] listSpacesModes() throws RemoteException {
        List<SpaceMode> spacesModes = new ArrayList<SpaceMode>();
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Field spaceDetails = serviceDetails.getClass().getDeclaredField("space");
                        spaceDetails.setAccessible(true);
                        IJSpace space = (IJSpace) spaceDetails.get(serviceDetails);
                        spacesModes.add(((IInternalRemoteJSpaceAdmin) space.getAdmin()).getSpaceMode());
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get space mode", e);
                }
            }
        }
        return spacesModes.toArray(new SpaceMode[spacesModes.size()]);
    }

    public PUMonitors getPUMonitors() throws RemoteException {
        ArrayList<Object> monitors = new ArrayList<Object>();
        for (Callable call : serviceMonitors) {
            try {
                Collections.addAll(monitors, (Object[]) call.call());
            } catch (Exception e) {
                logger.error(logMessage("Failed to get monitor information, ignoring it"), e);
            }
        }
        return new PUMonitors(monitors.toArray(new Object[monitors.size()]));
    }

    public PUDetails getPUDetails() throws RemoteException {
        return this.puDetails;
    }

    public ClusterInfo getClusterInfo() throws RemoteException {
        return this.clusterInfo;
    }

    public Object[] listServiceDetails() throws RemoteException {
        if (puDetails == null) {
            return new ServiceDetails[0];
        }
        return puDetails.getDetails();
    }

    private int getMaxServiceCount(String[] args) {
        int count = -1;
        for (String arg : args) {
            if (arg.indexOf("ScalingPolicyHandler.MaxServices") != -1) {
                StringTokenizer tok = new StringTokenizer(arg, " =");
                /* first token is "ScalingPolicyHandler.MaxServices" */
                tok.nextToken();
                String value = tok.nextToken();
                try {
                    count = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return (count);
    }

    private int getSLAMax(ServiceBeanContext context) {
        int max = -1;
        ServiceLevelAgreements slas =
                context.getServiceElement().getServiceLevelAgreements();
        SLA[] spaceSLAs = slas.getServiceSLAs();
        for (SLA spaceSLA : spaceSLAs) {
            int count =
                    getMaxServiceCount(spaceSLA.getConfigArgs());
            if (count != -1) {
                max = count;
                break;
            }
        }
        return max;
    }

    private String logMessage(String message) {
        return "[" + getServiceBeanContext().getServiceElement().getName() + "] " + message;
    }

    private class WatchTask implements Runnable {

        private final Monitor monitor;

        private final Watch watch;

        public WatchTask(Monitor monitor, Watch watch) {
            this.monitor = monitor;
            this.watch = watch;
        }

        public Monitor getMonitor() {
            return monitor;
        }

        public Watch getWatch() {
            return watch;
        }

        public void run() {
            watch.addWatchRecord(new Calculable(watch.getId(), monitor.getValue(), System.currentTimeMillis()));
        }
    }

    private ProcessingUnitContainerProvider createContainerProvider(String containerProviderType) {
        try {
            return (ProcessingUnitContainerProvider) ClassUtils.forName(containerProviderType, contextClassLoader).newInstance();
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to create a new instance of container [" + containerProviderType + "]", e);
        }
    }

    private void downloadAndExtractPU(String puName, String puPath, String codeserver, File path, File tempPath) {
        URL url = null;
        try {
            url = new URL(codeserver + puPath);
        } catch (MalformedURLException e) {
            throw new CannotCreateContainerException("Failed to construct URL to download procdessing unit, url [" + (codeserver + puPath) + "]", e);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to connect to [" + url.toString() + "] in order to download processing unit [" + puName + "]", e);
        }
        conn.setRequestProperty("Package", "true");
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to read response code from [" + url.toString() + "] in order to download processing unit [" + puName + "]", e);
        }
        if (responseCode != 200 && responseCode != 201) {
            try {
                if (responseCode == 404) {
                    throw new CannotCreateContainerException("Processing Unit [" + puName + "] not found on server [" + url.toString() + "]");
                }
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                } catch (Exception e) {
                    // ignore this exception, failed to read input
                }
                throw new CannotCreateContainerException("Failed to connect/download (failure on the web server side) from  [" + url.toString() + "], response code [" + responseCode + "], response [" + sb.toString() + "]");
            } finally {
                conn.disconnect();
            }
        }

        if (puName.length() < 3) {
            puName = "zzz" + puName;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(puName, "jar", tempPath);
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to create temporary file for downloading processing unit [" + puName + "] at [" + tempPath.getAbsolutePath() + "]", e);
        }
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            FileCopyUtils.copy(in, new FileOutputStream(tempFile));
            conn.disconnect();
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to read processing unit [" + puName + "] from [" + url.toString() + "] into [" + tempFile.getAbsolutePath() + "]", e);
        }

        // extract the file
        try {
            final int bufferSize = 4098;
            byte data[] = new byte[bufferSize];
            ZipFile zipFile = new ZipFile(tempFile);
            Enumeration e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    File dir = new File(path.getAbsolutePath() + "/" + entry.getName().replace('\\', '/'));
                    for (int i = 0; i < 5; i++) {
                        dir.mkdirs();
                    }
                } else {
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int count;
                    File file = new File(path.getAbsolutePath() + "/" + entry.getName().replace('\\', '/'));
                    if (file.getParentFile() != null) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
                    while ((count = is.read(data, 0, bufferSize)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            zipFile.close();
            tempFile.delete();
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to extract processing unit [" + puName + "] downloaded temp zip file from [" + tempFile.getAbsolutePath() + "] into [" + path.getAbsolutePath() + "]", e);
        }
    }

    private void prepareWebApplication(File deployPath, ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties) throws Exception {
        if (!(new File(deployPath, "WEB-INF/web.xml").exists())) {
            return;
        }
        BootstrapWebApplicationContextListener.prepareForBoot(deployPath, clusterInfo, beanLevelProperties);

        // if we download, we can delete JSpaces and jini jars from the WEB-INF/lib (they are already in the
        // common class loader).
        File webInfLib = new File(deployPath, "WEB-INF/lib");
        webInfLib.mkdirs();
        if (webInfLib.exists()) {
            File[] wenInfJars = webInfLib.listFiles();
            ArrayList<String> deleted = new ArrayList<String>();
            for (File webInfJarFile : wenInfJars) {
                boolean delete = false;
                if (webInfJarFile.getName().startsWith("gs-runtime")) {
                    delete = true;
                }
                if (delete) {
                    deleted.add(webInfJarFile.getName());
                    webInfJarFile.delete();
                }
            }
            if (!deleted.isEmpty()) {
                logger.debug(logMessage("Deleted the following jars from the web application: " + deleted));
            }
            try {
                FileCopyUtils.copy(new File(Environment.getHomeDirectory() + "/lib/gigaspaces/gs-openspaces.jar"), new File(deployPath, "WEB-INF/lib/gs-openspaces.jar"));
                logger.debug(logMessage("Added openspaces jar to web application"));
            } catch (IOException e) {
                // don't copy it
            }
            try {
                FileCopyUtils.copy(new File(Environment.getHomeDirectory() + "/lib/gigaspaces/spring.jar"), new File(deployPath, "WEB-INF/lib/spring.jar"));
                FileSystemUtils.copyRecursively(new File(Environment.getHomeDirectory() + "/lib/opt/spring"), new File(deployPath, "WEB-INF/lib"));
                logger.debug(logMessage("Added spring jars to web application"));
            } catch (IOException e) {
                // don't copy it
            }
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return NIOInfoHelper.getConfiguration();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return NIOInfoHelper.getNIOStatistics();
    }

    public OSDetails getOSConfiguration() throws RemoteException {
        return OSHelper.getConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return OSHelper.getStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return JVMHelper.getConfiguration();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return JVMHelper.getStatistics();
    }
    
    public void runGc() throws RemoteException {
        System.gc();
    }
}
