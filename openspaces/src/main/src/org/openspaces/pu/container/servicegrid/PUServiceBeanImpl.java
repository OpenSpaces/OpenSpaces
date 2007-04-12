/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.pu.container.servicegrid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.SLA;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.StringTokenizer;

/**
 *
 */
public class PUServiceBeanImpl extends ServiceBeanAdapter implements PUServiceBean {

    private static final Log logger = LogFactory.getLog(PUServiceBeanImpl.class);

    private IntegratedProcessingUnitContainer integratedContainer;

    private int clusterGroup;

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

        MarshalledObject slaMarshObj = (MarshalledObject) getServiceBeanContext().getInitParameter("sla");
        org.openspaces.pu.container.servicegrid.sla.SLA sla =
                (org.openspaces.pu.container.servicegrid.sla.SLA) slaMarshObj.get();

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
    }

    private void stopPU() {
        if (integratedContainer != null) {
            try {
                integratedContainer.close();
            } catch (Exception e) {
                logger.warn(logMessage("Failed to close"), e);
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
}
