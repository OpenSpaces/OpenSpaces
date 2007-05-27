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
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.sla.monitor.ApplicationContextMonitor;
import org.openspaces.pu.sla.monitor.Monitor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PUServiceBeanImpl extends ServiceBeanAdapter implements PUServiceBean {

    private static final Log logger = LogFactory.getLog(PUServiceBeanImpl.class);

    private IntegratedProcessingUnitContainer integratedContainer;

    private int clusterGroup;

    private List<WatchTask> watchTasks = new ArrayList<WatchTask>();

    private ScheduledExecutorService executorService;

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

    public void destroy() {
        for (WatchTask watchTask : watchTasks) {
            watchRegistry.deregister(watchTask.getWatch());
        }
        watchTasks.clear();
        super.destroy();
    }

    public void advertise() throws IOException {
        String springXML = (String) context.getInitParameter("pu");
        clusterGroup = Integer.parseInt((String) context.getInitParameter("clusterGroup"));
        try {
            startPU(springXML);
        } catch (Exception e) {
            logger.info(logMessage("Failed to start PU with xml [" + springXML + "]"), e);
            // TODO create explciit exception here
            throw new RuntimeException(e.getMessage());
        }

        super.advertise();
    }

    public void unadvertise() {
        super.unadvertise();

        stopPU();
    }

    private void startPU(String springXml) throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage("Starting PU with [" + springXml + "]"));
        }

        org.openspaces.pu.sla.SLA sla = getSLA(getServiceBeanContext());

        //this is the MOST IMPORTANT part
        Integer instanceId;
        Integer backupId = null;
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

        //set cluster info
        ClusterInfo clusterInfo = new ClusterInfo();
        String clusterSchema = sla.getClusterSchema();
        if (clusterSchema != null) {
            clusterInfo.setSchema(clusterSchema);
            int slaMax = getSLAMax(context);
            int numberOfInstances = Math.max(slaMax, sla.getNumberOfInstances());
            clusterInfo.setNumberOfInstances(numberOfInstances);
        }
        clusterInfo.setNumberOfBackups(sla.getNumberOfBackups());
        clusterInfo.setInstanceId(instanceId);
        clusterInfo.setBackupId(backupId);

        logger.info(logMessage("ClusterInfo [" + clusterInfo + "]"));

        //create PU Container
        Resource resource = new ByteArrayResource(springXml.getBytes());
        IntegratedProcessingUnitContainerProvider factory = new IntegratedProcessingUnitContainerProvider();
        factory.addConfigLocation(resource);
        factory.setClusterInfo(clusterInfo);

        MarshalledObject beanLevelPropertiesMarshObj =
                (MarshalledObject) getServiceBeanContext().getInitParameter("beanLevelProperties");
        if (beanLevelPropertiesMarshObj != null) {
            BeanLevelProperties beanLevelProperties = (BeanLevelProperties) beanLevelPropertiesMarshObj.get();
            factory.setBeanLevelProperties(beanLevelProperties);
            logger.info(logMessage("BeanLevelProperties " + beanLevelProperties));
        }

        integratedContainer = (IntegratedProcessingUnitContainer) factory.createContainer();

        // inject the application context to all the monitors and schedule them
        // currently use the number of threads in relation to the number of monitors
        int numberOfThreads = watchTasks.size() / 5;
        if (numberOfThreads == 0) {
            numberOfThreads = 1;
        }
        executorService = Executors.newScheduledThreadPool(numberOfThreads);
        for (WatchTask watchTask : watchTasks) {
            if (watchTask.getMonitor() instanceof ApplicationContextMonitor) {
                ((ApplicationContextMonitor) watchTask.getMonitor()).setApplicationContext(integratedContainer.getApplicationContext());
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
        if (integratedContainer != null) {
            try {
                integratedContainer.close();
            } catch (Exception e) {
                logger.warn(logMessage("Failed to close"), e);
            } finally {
                integratedContainer = null;
            }
        }
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

        private Monitor monitor;

        private Watch watch;

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
}
