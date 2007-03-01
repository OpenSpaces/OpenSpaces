/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.pu.container.servicegrid;

import org.jini.rio.core.SLA;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Jan 15, 2007
 * Time: 1:35:24 AM
 */
public class PUServiceBeanImpl extends ServiceBeanAdapter implements PUServiceBean {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = Logger.getLogger(PUServiceBeanImpl.class.getName());
    private static final String SPACE_PROPERTY_PREFIX = "gs.space.url.arg.";

    private IntegratedProcessingUnitContainer integratedContainer;
    private int clusterGroup;

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface ServiceBeanAdapterMBean ---------------------

    public void advertise() throws IOException {
        String springXML = (String) context.getInitParameter("pu");
        clusterGroup = Integer.parseInt((String) context.getInitParameter("clusterGroup"));
        try {
            startPU(springXML);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException(e.getMessage());
        }

        super.advertise();
    }

    public void unadvertise() {
        super.unadvertise();

        stopPU();
    }

// -------------------------- OTHER METHODS --------------------------

    private int getMaxServiceCount(String[] args) {
        int count = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].indexOf("ScalingPolicyHandler.MaxServices") != -1) {
                StringTokenizer tok = new StringTokenizer(args[i], " =");
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

    private int getTotalMembers(ServiceBeanContext context) {
        ServiceLevelAgreements slas =
                context.getServiceElement().getServiceLevelAgreements();
        SLA[] spaceSLAs = slas.getServiceSLAs();
        int memberCount = context.getServiceElement().getPlanned();
        for (int i = 0; i < spaceSLAs.length; i++) {
            int count =
                    getMaxServiceCount(spaceSLAs[i].getConfigArgs());
            if (count != -1) {
                memberCount = count;
                break;
            }
        }
        return memberCount;
    }

    private void startPU(String springXml) throws MalformedURLException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "PUServiceBeanImpl.startPU:" + springXml);
        }

        org.openspaces.pu.container.servicegrid.sla.SLA sla = SLAUtil.loadSLA(springXml);

        //this is the MOST IMPORTANT part
        Integer instanceId = null;
        Integer backupId = null;
        boolean hasBackups = sla.getNumberOfBackups() > 0;
        if (hasBackups) {
            instanceId = new Integer(clusterGroup);
            //the first instance is primary so no backupid
            if (context.getServiceBeanConfig().getInstanceID().intValue() > 1) {
                backupId = new Integer((context.getServiceBeanConfig().getInstanceID().intValue() - 1));
            }
        } else {
            instanceId = new Integer((context.getServiceBeanConfig().getInstanceID().intValue()));
        }

        LOGGER.log(Level.INFO, "id=" + instanceId + ", bid=" + backupId);

        //set cluster info
        ClusterInfo clusterInfo = new ClusterInfo();
        String clusterSchema = sla.getClusterSchema();
        if (clusterSchema != null) {
            clusterInfo.setSchema(clusterSchema);
            int memberCount = getTotalMembers(context);
            clusterInfo.setNumberOfInstances(new Integer(memberCount));
        }
        clusterInfo.setNumberOfBackups(new Integer(sla.getNumberOfBackups()));
        clusterInfo.setInstanceId(instanceId);
        clusterInfo.setBackupId(backupId);

        //create PU Container
        Resource resource = new ByteArrayResource(springXml.getBytes());
        IntegratedProcessingUnitContainerProvider factory = new IntegratedProcessingUnitContainerProvider();
        factory.addConfigLocation(resource);
        factory.setClusterInfo(clusterInfo);
        integratedContainer = (IntegratedProcessingUnitContainer) factory.createContainer();
    }

    private void stopPU() {
        if (integratedContainer != null) {
            try {
                integratedContainer.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }
        }
    }
}
