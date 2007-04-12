package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import net.jini.core.lookup.ServiceItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.ThresholdValues;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpString;
import org.jini.rio.opstring.OpStringLoader;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.pu.container.servicegrid.SLAUtil;
import org.openspaces.pu.container.servicegrid.sla.Generic;
import org.openspaces.pu.container.servicegrid.sla.Host;
import org.openspaces.pu.container.servicegrid.sla.Policy;
import org.openspaces.pu.container.servicegrid.sla.RangeRequirement;
import org.openspaces.pu.container.servicegrid.sla.RelocationPolicy;
import org.openspaces.pu.container.servicegrid.sla.SLA;
import org.openspaces.pu.container.servicegrid.sla.ScaleUpPolicy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 */
public class Deploy {
// ------------------------------ FIELDS ------------------------------

    private static final Log logger = LogFactory.getLog(Deploy.class);

    private DeployAdmin deployAdmin;

    private String[] groups;

    public DeployAdmin getDeployAdmin() throws GSMNotFoundException {
        if (deployAdmin == null) {
            GSM gsm = null;
            // TODO add the timeout as a paremeter to deploy
            long timeout = 5000;
            ServiceItem result = ServiceFinder.find(null, GSM.class, timeout, getGroups());
            if (result != null) {
                try {
                    result = (ServiceItem) new MarshalledObject(result).get();
                    gsm = (GSM) result.service;
                } catch (Exception e) {
                    throw new GSMNotFoundException(getGroups(), timeout, e);
                }
            }
            if (gsm == null) {
                throw new GSMNotFoundException(getGroups(), timeout);
            }
            try {
                deployAdmin = (DeployAdmin) gsm.getAdmin();
            } catch (RemoteException e) {
                throw new GSMNotFoundException(getGroups(), timeout);
            }
        }

        return deployAdmin;
    }

    public void setDeployAdmin(DeployAdmin deployAdmin) {
        this.deployAdmin = deployAdmin;
    }

    public String[] getGroups() {
        if (groups == null) {
            String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
            if (groupsProperty != null) {
                StringTokenizer tokenizer = new StringTokenizer(groupsProperty);
                int count = tokenizer.countTokens();
                groups = new String[count];
                for (int i = 0; i < count; i++) {
                    groups[i] = tokenizer.nextToken();
                }
            }
        }
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public void deploy(String[] args) throws Exception {
        String puPath = args[0];
        int index = puPath.lastIndexOf('/');
        index = index == -1 ? 0 : index;
        String puName = puPath.substring(index);

        String[] groups = getGroups();
        if (logger.isInfoEnabled()) {
            logger.info("Deploying [" + puName + "] with groups " + Arrays.asList(groups));
        }
        //get codebase from service
        String codeserver = getCodebase(getDeployAdmin());
        if (logger.isDebugEnabled()) {
            logger.debug("Usign codeserver [" + codeserver + "]");
        }

        //list remote files, only works with webster
        URL root = new URL(codeserver);
        HTTPFileSystemView view = new HTTPFileSystemView(root);
        File puHome = view.createFileObject(puPath);

        //get list of all jars
        File lib = view.createFileObject(puHome, "lib");
        File[] jars = view.getFiles(lib, false);
        if (logger.isDebugEnabled()) {
            logger.debug("Using lib " + Arrays.asList(jars));
        }

        //get list of all shared
        File shared = view.createFileObject(puHome, "shared-lib");
        File[] sharedJars = view.getFiles(shared, false);
        if (logger.isDebugEnabled()) {
            logger.debug("Using shared-lib " + Arrays.asList(sharedJars));
        }

        //read pu xml
        String puString = readPUFile(root, puPath);
        if (logger.isDebugEnabled()) {
            logger.debug("Using PU xml [" + puString + "]");
        }

        //get sla from pu string
        SLA sla = SLAUtil.loadSLA(puString);
        if (logger.isDebugEnabled()) {
            logger.debug("Using SLA " + sla);
        }

        //deploy to sg
        OperationalString opString = loadDeployment(puString, codeserver, sla, jars, puPath, puName, sharedJars);
        /* Map result = */
        deployAdmin.deploy(opString);
    }

    //copied from opstringloader
    private String[] getSLAConfigArgs(String type, String max, String lowerDampener, String upperDampener) {
        String[] args;
        String handler = "org.jini.rio.qos.";
        int handlerType;
        if (type.equals("scaling")) {
            handler = handler + "ScalingPolicyHandler";
            handlerType = 1;
        } else if (type.equals("relocation")) {
            handler = handler + "RelocationPolicyHandler";
            handlerType = 2;
        } else {
            handler = handler + "SLAPolicyHandler";
            handlerType = 3;
        }

        String slaPolicyHandler =
                "org.jini.rio.qos.SLAPolicyHandler.slaPolicyHandler";
        switch (handlerType) {
            case 1:
                args = new String[5];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" +
                        "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".MaxServices=" + max;
                args[3] = handler + ".LowerThresholdDampeningFactor=" +
                        lowerDampener;
                args[4] = handler + ".UpperThresholdDampeningFactor=" +
                        upperDampener;
                break;
            case 2:
                args = new String[4];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" +
                        "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".LowerThresholdDampeningFactor=" +
                        lowerDampener;
                args[3] = handler + ".UpperThresholdDampeningFactor=" +
                        upperDampener;
                break;
            default:
                args = new String[2];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" +
                        "new " + handler + "((org.jini.rio.core.SLA)$data)";
        }
        return (args);
    }

    private static String readPUFile(URL root, String puPath) throws IOException {
        URL puURL = new URL(root, puPath + "/META-INF/spring/pu.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(puURL.openStream()));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        return buffer.toString();
    }

    private String getCodebase(DeployAdmin deployAdmin) throws MalformedURLException, RemoteException {
        URL url = ((ServiceAdmin) deployAdmin).getServiceElement().getExportURLs()[0];
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/";
    }

    private OperationalString loadDeployment(String puString, String codeserver, SLA sla, File[] jars, String puPath,
                                             String puName, File[] sharedJars) throws Exception {
        URL opstringURL = Deploy.class.getResource("/org/openspaces/pu/container/servicegrid/puservicebean.xml");
        OperationalString opString;

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader();
        opStringLoader.setDefaultGroups(getGroups());
        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(opstringURL)[0];
        ((OpString) opString).setName(puName);

        //this opstring should only have one servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];

        //put the entire pu spring xml as parameter to servicebean
        element.getServiceBeanConfig().addInitParameter("pu", puString);

        //sla
        Policy policy = sla.getPolicy();
        if (policy != null) {
            String type;
            if (policy instanceof ScaleUpPolicy) {
                type = "scaling";
            } else if (policy instanceof RelocationPolicy) {
                type = "relocation";
            } else {
                throw new IllegalArgumentException("Unknown SLA Policy:" + policy);
            }

            String max = String.valueOf(sla.getNumberOfInstances());
            //todo: make sure max is greater then num of instances
            if (policy instanceof ScaleUpPolicy) {
                max = String.valueOf(((ScaleUpPolicy) policy).getScaleUpTo());
            }
            //todo:300 is hard coded for now
            String[] configParms = getSLAConfigArgs(type, max, "3000", "3000");
            org.jini.rio.core.SLA slaElement = new org.jini.rio.core.SLA(
                    policy.getMonitor(),
                    new double[]{policy.getLow(), policy.getHigh()},
                    configParms,
                    null);
            element.getServiceLevelAgreements().addServiceSLA(slaElement);
        }

        //requirements
        List<String> hosts = new ArrayList<String>();
        if (sla.getRequirements() != null) {
            for (int i = 0; i < sla.getRequirements().size(); i++) {
                Object requirement = sla.getRequirements().get(i);
                if (requirement instanceof RangeRequirement) {
                    RangeRequirement rangeRequirement = (RangeRequirement) requirement;
                    ThresholdValues thresholdValues = new ThresholdValues(rangeRequirement.getLow(), rangeRequirement.getHigh());
                    element.getServiceLevelAgreements().addSystemThreshold(rangeRequirement.getWatch(), thresholdValues);
                } else if (requirement instanceof Host) {
                    hosts.add(((Host) requirement).getHost());
                } else if (requirement instanceof Generic) {
                    Generic generic = (Generic) requirement;
                    ServiceLevelAgreements.SystemRequirement systemRequirement = new ServiceLevelAgreements.SystemRequirement(
                            generic.getName(),
                            null,
                            generic.getAttributes()
                    );
                    element.getServiceLevelAgreements().addSystemRequirement(systemRequirement);
                }
            }
        }
        //put hosts as cluster
        if (hosts.size() > 0) {
            element.setCluster(hosts.toArray(new String[hosts.size()]));
        }

        if (sla.getMaxInstancesPerVM() > 0) {
            element.setMaxPerMachine(sla.getMaxInstancesPerVM());
        }

        //put jars
        ClassBundle classBundle = element.getComponentBundle();
        classBundle.addJAR(puPath + "/");
        for (File jar : jars) {
            String path = jar.getPath();
            classBundle.addJAR(path);
        }

        //shared-lib as sharedComponents
        String[] sharedJarPaths = new String[sharedJars.length];
        for (int i = 0; i < sharedJars.length; i++) {
            File sharedJar = sharedJars[i];
            String path = sharedJar.getPath();
            sharedJarPaths[i] = path;
        }
        Map<String, String[]> jarsMap = new HashMap<String, String[]>();
        jarsMap.put("hack", sharedJarPaths);
        classBundle.addSharedComponents(jarsMap);

        // set the each servive to have the operation string name
        element.getServiceBeanConfig().setName(element.getOperationalStringName().replace(' ', '-') + "." + element.getName());

        // pass the SLA as an init parameter so the GSC won't need to parse the XML again
        element.getServiceBeanConfig().addInitParameter("sla", new MarshalledObject(sla));

        //this is the MOST IMPORTANT part
        boolean hasBackups = sla.getNumberOfBackups() > 0;
        if (hasBackups) {
            //the extra one is the primary
            element.setPlanned(sla.getNumberOfBackups() + 1);
            String name = element.getName();
            opString.removeService(element);
            for (int i = 1; i <= sla.getNumberOfInstances(); i++) {
                ServiceElement clone = deepCopy(element);
                clone.getServiceBeanConfig().setName(name + "." + i);
                clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(i));
                opString.addService(clone);
                if (logger.isTraceEnabled()) {
                    logger.trace("Using Service Element " + element.toString());
                }
            }
        } else {
            element.setPlanned(sla.getNumberOfInstances());
            element.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(1));
            if (logger.isTraceEnabled()) {
                logger.trace("Using Service Element " + element.toString());
            }
        }

        return (opString);
    }

    private ServiceElement deepCopy(ServiceElement element) throws IOException, ClassNotFoundException {
        //write
        byte[] writtenBytes;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(element);
        writtenBytes = byteArrayOutputStream.toByteArray();

        //read
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(writtenBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object readObject = objectInputStream.readObject();
        return (ServiceElement) readObject;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }


        System.setSecurityManager(new RMISecurityManager() {
            public void checkPermission(java.security.Permission perm) {
            }

            public void checkPermission(java.security.Permission perm, Object context) {
            }
        });

        Deploy deployer = new Deploy();
        deployer.deploy(args);
    }

    private static void printUsage() {
        System.out.println("Usage: Deploy [PU Name]");
    }

}
