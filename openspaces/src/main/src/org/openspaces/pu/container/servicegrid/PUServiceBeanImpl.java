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
import com.gigaspaces.start.Locator;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.client.DCacheSpaceImpl;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.cache.ISpaceLocalCache;
import com.j_spaces.core.client.view.LocalSpaceView;
import net.jini.core.lookup.ServiceID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.SLA;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.watch.Calculable;
import org.jini.rio.watch.GaugeWatch;
import org.jini.rio.watch.Watch;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.jetty.JettyJeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.BeanLevelPropertiesUtils;
import org.openspaces.pu.sla.monitor.ApplicationContextMonitor;
import org.openspaces.pu.sla.monitor.Monitor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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

    private volatile ApplicationContextProcessingUnitContainer container;

    private int clusterGroup;

    private Integer instanceId;

    private Integer backupId;

    private volatile List<WatchTask> watchTasks = new ArrayList<WatchTask>();

    private volatile ScheduledExecutorService executorService;

    private volatile ClassLoader contextClassLoader;

    private volatile MemberAliveIndicator[] memberAliveIndicators;

    // all the embedded spaces
    private volatile IJSpace[] embeddedSpaces;

    // embedded spaces, localcahce/view spaces, proxies
    private volatile IJSpace[] allSpaces;

    private volatile File deployPath;

    private ClusterInfo clusterInfo;

    public PUServiceBeanImpl() {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void initialize(ServiceBeanContext context) throws Exception {
        org.openspaces.pu.sla.SLA sla = getSLA(context);
        if (sla.getMonitors() != null) {
            for (Monitor monitor : sla.getMonitors()) {
                String watchName = monitor.getName();
                Watch watch = new GaugeWatch(watchName, context.getConfiguration());
                watch.getWatchDataSource().setSize(monitor.getHistorySize());
                watchRegistry.register(watch);
                watchTasks.add(new WatchTask(monitor, watch));
            }
        }
        super.initialize(context);
    }

    @Override
    public void destroy() {
        for (WatchTask watchTask : watchTasks) {
            watchRegistry.deregister(watchTask.getWatch());
        }
        watchTasks.clear();
        super.destroy();
    }

    @Override
    public void doAdvertise() throws IOException {
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
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            // TODO create explicit exception here
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }

        super.doAdvertise();
    }

    @Override
    public void unadvertise() {
        super.unadvertise();

        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            stopPU();
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    private void startPU(String springXml) throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage("Starting PU with [" + springXml + "]"));
        }

        String puName = (String) context.getInitParameter("puName");
        String puPath = (String) context.getInitParameter("puPath");
        String codeserver = (String) context.getInitParameter("codeserver");

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
        File workLocation = new File(System.getProperty("com.gs.work", System.getProperty(Locator.GS_HOME) + "/work"));
        workLocation.mkdirs();

        beanLevelProperties.getContextProperties().setProperty("com.gs.work", workLocation.getAbsolutePath());

        //create PU Container
        ApplicationContextProcessingUnitContainerProvider factory;
        // identify if this is a web app
        InputStream webXml = contextClassLoader.getResourceAsStream("WEB-INF/web.xml");
        if (webXml != null) {
            webXml.close();
            JeeProcessingUnitContainerProvider jeeFactory = (JeeProcessingUnitContainerProvider) createContainerProvider(beanLevelProperties, JettyJeeProcessingUnitContainerProvider.class.getName());
            String deployName = puName + "_" + clusterInfo.getRunningNumberOffset1();

            String deployedProcessingUnitsLocation = workLocation.getAbsolutePath() + "/deployed-processing-units";

            deployPath = new File(deployedProcessingUnitsLocation + "/" + deployName);
            FileSystemUtils.deleteRecursively(deployPath);
            deployPath.mkdirs();

            beanLevelProperties.getContextProperties().setProperty("jee.deployPath", deployPath.getAbsolutePath());

            jeeFactory.setDeployPath(deployPath);
            downloadAndExtractPU(puPath, codeserver, deployPath, new File(deployedProcessingUnitsLocation));

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

            factory = jeeFactory;
        } else {
            factory = (ApplicationContextProcessingUnitContainerProvider) createContainerProvider(beanLevelProperties, IntegratedProcessingUnitContainerProvider.class.getName());
        }

        if (StringUtils.hasText(springXml)) {
            Resource resource = new ByteArrayResource(springXml.getBytes());
            factory.addConfigLocation(resource);
        }
        factory.setClusterInfo(clusterInfo);
        factory.setBeanLevelProperties(beanLevelProperties);

        container = (ApplicationContextProcessingUnitContainer) factory.createContainer();

        Map map = container.getApplicationContext().getBeansOfType(MemberAliveIndicator.class);
        ArrayList<MemberAliveIndicator> maiList = new ArrayList<MemberAliveIndicator>();
        for (Iterator it = map.values().iterator(); it.hasNext();) {
            MemberAliveIndicator memberAliveIndicator = (MemberAliveIndicator) it.next();
            if (memberAliveIndicator.isMemberAliveEnabled()) {
                maiList.add(memberAliveIndicator);
            }
        }
        memberAliveIndicators = maiList.toArray(new MemberAliveIndicator[maiList.size()]);

        map = container.getApplicationContext().getBeansOfType(IJSpace.class);
        ArrayList<IJSpace> embeddedSpacesList = new ArrayList<IJSpace>();
        ArrayList<IJSpace> allSpacesList = new ArrayList<IJSpace>();
        for (Iterator it = map.values().iterator(); it.hasNext();) {
            IJSpace space = (IJSpace) it.next();
            if (space instanceof ISpaceLocalCache || SpaceUtils.isRemoteProtocol(space)) {
                allSpacesList.add(space);
            } else {
                embeddedSpacesList.add(space);
                allSpacesList.add(space);
            }
        }
        embeddedSpaces = embeddedSpacesList.toArray(new IJSpace[embeddedSpacesList.size()]);
        allSpaces = allSpacesList.toArray(new IJSpace[allSpacesList.size()]);

        // inject the application context to all the monitors and schedule them
        // currently use the number of threads in relation to the number of monitors
        int numberOfThreads = watchTasks.size() / 5;
        if (numberOfThreads == 0) {
            numberOfThreads = 1;
        }
        executorService = Executors.newScheduledThreadPool(numberOfThreads);
        for (WatchTask watchTask : watchTasks) {
            if (watchTask.getMonitor() instanceof ApplicationContextMonitor) {
                ((ApplicationContextMonitor) watchTask.getMonitor()).setApplicationContext(container.getApplicationContext());
            }
            executorService.scheduleAtFixedRate(watchTask, watchTask.getMonitor().getPeriod(), watchTask.getMonitor().getPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    private org.openspaces.pu.sla.SLA getSLA(ServiceBeanContext context) throws IOException, ClassNotFoundException {
        MarshalledObject slaMarshObj = (MarshalledObject) context.getInitParameter("sla");
        return (org.openspaces.pu.sla.SLA) slaMarshObj.get();
    }

    private void stopPU() {
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
                embeddedSpaces = null;
                allSpaces = null;
                memberAliveIndicators = null;
            }
        }

        // clean the deploy path directory
        if (deployPath != null) {
            boolean deleted = false;
            for (int i = 0; i < 2; i++) {
                deleted = FileSystemUtils.deleteRecursively(deployPath);
                if (deleted) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            if (!deleted) {
                logger.debug("Failed to delete deployed war from [" + deployPath + "]");
            }
        }
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
        SpaceURL[] spaceURLs = new SpaceURL[embeddedSpaces.length];
        for (int i = 0; i < embeddedSpaces.length; i++) {
            spaceURLs[i] = embeddedSpaces[i].getFinderURL();
        }
        return spaceURLs;
    }

    public IJSpace[] listSpaces() throws RemoteException {
        return embeddedSpaces;
    }

    public ClusterInfo getClusterInfo() throws RemoteException {
        return this.clusterInfo;
    }

    /**
     * @return A list of {@link SpaceMode space modes}; An empty array if there are no spaces.
     */
    public SpaceMode[] listSpacesModes() throws RemoteException {
        final int length = embeddedSpaces == null ? 0 : embeddedSpaces.length;
        SpaceMode[] spacesModes = new SpaceMode[length];
        for (int i = 0; i < length; ++i) {
            spacesModes[i] = ((IInternalRemoteJSpaceAdmin) embeddedSpaces[i].getAdmin()).getSpaceMode();
        }
        return spacesModes;
    }

    public PUServiceDetails[] listServiceDetails() throws RemoteException {
        ArrayList<PUServiceDetails> serviceDetails = new ArrayList<PUServiceDetails>();
        for (IJSpace space : allSpaces) {
            SpaceURL spaceURL = space.getFinderURL();
            ServiceID serviceID = new ServiceID(space.getReferentUuid().getMostSignificantBits(), space.getReferentUuid().getLeastSignificantBits());
            String type = "embedded";
            if (space instanceof LocalSpaceView) {
                type = "localview";
            } else if (space instanceof DCacheSpaceImpl) {
                type = "localcache";
            } else if (SpaceUtils.isRemoteProtocol(space)) {
                type = "remote";
            }
            serviceDetails.add(new SpacePUServiceDetails(spaceURL.getSpaceName(), spaceURL.getContainerName(), serviceID,
                    ((IInternalRemoteJSpaceAdmin) space.getAdmin()).getSpaceMode(), type));
        }
        if (container instanceof JeeProcessingUnitContainer) {
            serviceDetails.add(((JeeProcessingUnitContainer) container).getServiceDetails());
        }
        return serviceDetails.toArray(new PUServiceDetails[serviceDetails.size()]);
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

    private ProcessingUnitContainerProvider createContainerProvider(BeanLevelProperties beanLevelProperties, String defaultType) {
        String containerProviderType = defaultType;
        if (beanLevelProperties != null) {
            containerProviderType = beanLevelProperties.getContextProperties().getProperty(ProcessingUnitContainerProvider.CONTAINER_CLASS_PROP, defaultType);
        }
        try {
            return (ProcessingUnitContainerProvider) ClassUtils.forName(containerProviderType).newInstance();
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to create a new instance of container [" + containerProviderType + "]", e);
        }
    }

    private void downloadAndExtractPU(String puPath, String codeserver, File path, File tempPath) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(codeserver + puPath).openConnection();
        conn.setRequestProperty("Package", "true");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            throw new RuntimeException("Failed to extract file to: " + path.getAbsolutePath() + ", response code [" + responseCode + "]");
        }

        File tempFile = File.createTempFile("packaged-webpu", "war", tempPath);
        InputStream in = new BufferedInputStream(conn.getInputStream());
        FileCopyUtils.copy(in, new FileOutputStream(tempFile));
        conn.disconnect();

        // extract the file
        byte data[] = new byte[1024];
        ZipFile zipFile = new ZipFile(tempFile);
        Enumeration e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            if (entry.isDirectory()) {
                File dir = new File(path.getAbsolutePath() + "/" + entry.getName());
                for (int i = 0; i < 5; i++) {
                    dir.mkdirs();
                }
            } else {
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                int count;
                File file = new File(path.getAbsolutePath() + "/" + entry.getName());
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream dest = new BufferedOutputStream(fos, 1024);
                while ((count = is.read(data, 0, 1024)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        zipFile.close();
        tempFile.delete();
    }
}
