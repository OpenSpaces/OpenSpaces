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

package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.logger.GSLogConfigLoader;
import com.j_spaces.core.service.ServiceConfigLoader;
import com.j_spaces.kernel.PlatformVersion;
import net.jini.config.Configuration;
import net.jini.core.lookup.ServiceItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.jini.rio.config.ExporterConfig;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceBeanInstance;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.ServiceProvisionListener;
import org.jini.rio.core.ThresholdValues;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpString;
import org.jini.rio.opstring.OpStringLoader;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.sla.InstanceSLA;
import org.openspaces.pu.sla.Policy;
import org.openspaces.pu.sla.RelocationPolicy;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.ScaleUpPolicy;
import org.openspaces.pu.sla.requirement.HostRequirement;
import org.openspaces.pu.sla.requirement.RangeRequirement;
import org.openspaces.pu.sla.requirement.Requirement;
import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 */
public class Deploy {

    private static final Log logger = LogFactory.getLog(Deploy.class);

    private DeployAdmin deployAdmin;

    private GSM[] gsms = null;

    private String[] groups;

    private String locators;

    private int lookupTimeout = 5000;

    private static boolean sout = false;

    private static boolean disableInfoLogging = false;

    public static void setDisableInfoLogging(boolean disableInfoLogging) {
        Deploy.disableInfoLogging = disableInfoLogging;
    }

    public GSM[] findGSMs() {
        if (this.gsms != null) {
            return gsms;
        }
        GSM[] gsms;
        info("Searching for GSMs in groups " + Arrays.asList(getGroups()) + " and locators [" + getLocators() + "]");
        ServiceItem[] result = ServiceFinder.find(null, GSM.class, lookupTimeout, getGroups(), getLocators());
        if (result != null && result.length > 0) {
            gsms = new GSM[result.length];
            for (int i = 0; i < result.length; i++) {
                gsms[i] = (GSM) result[i].service;
            }
        } else {
            gsms = new GSM[0];
        }
        return gsms;
    }

    public void initDeployAdmin(GSM[] gsms) throws GSMNotFoundException {
        GSM gsm = null;
        if (deployAdmin == null) {
            if (gsms == null) {
                gsms = findGSMs();
            }
            if (gsms.length > 0) {
                try {
                    gsm = gsms[0];
                } catch (Exception e) {
                    throw new GSMNotFoundException(getGroups(), lookupTimeout, e);
                }
            }
            if (gsm == null) {
                throw new GSMNotFoundException(getGroups(), lookupTimeout);
            }
            try {
                deployAdmin = (DeployAdmin) gsm.getAdmin();
            } catch (RemoteException e) {
                throw new GSMNotFoundException(getGroups(), lookupTimeout);
            }
        }
    }

    public static void setSout(boolean soutVal) {
        sout = soutVal;
    }

    public void setGSMs(GSM[] gsms) {
        this.gsms = gsms;
    }

    public void setDeployAdmin(DeployAdmin deployAdmin) {
        this.deployAdmin = deployAdmin;
    }

    public String[] getGroups() {
        if (groups == null) {
            String groupsProperty = java.lang.System.getProperty("com.gs.jini_lus.groups");
            if (groupsProperty != null) {
                StringTokenizer tokenizer = new StringTokenizer(groupsProperty);
                int count = tokenizer.countTokens();
                groups = new String[count];
                for (int i = 0; i < count; i++) {
                    groups[i] = tokenizer.nextToken();
                }
            } else {
                groups = new String[]{"gigaspaces-" + PlatformVersion.getVersionNumber()};
            }
        }
        return groups;
    }

    public String getLocators() {
        if (locators == null) {
            String locatorsProperty = java.lang.System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty != null) {
                locators = locatorsProperty;
            }
        }
        return locators;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public void setLocators(String locators) {
        this.locators = locators;
    }

    public void setLookupTimeout(int lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    public void deployAndWait(String[] args) throws Exception {
        OperationalString opString = buildOperationalString(args);
        if (deployAdmin.hasDeployed(opString.getName())) {
            info("Processing Unit already deployed, exiting");
            return;
        }
        int totalPlanned = sumUpServices(opString);
        DeployListener listener = new DeployListener();
        Configuration config = ServiceConfigLoader.getConfiguration();
        deployAdmin.deploy(opString, (ServiceProvisionListener) ExporterConfig.getExporter(config, "com.gigaspaces.transport", "defaultExporter").export(listener));
        info("Waiting for [" + totalPlanned + "] processing unit instances to be deployed...");
        while (true) {
            if (totalPlanned == listener.getTotalStarted()) {
                break;
            }
            Thread.sleep(200);
        }
        info("Finished deploying [" + totalPlanned + "] processing unit instances");
    }

    public void deploy(String[] args) throws Exception {
        deploy(args, null);
    }

    public void deploy(String[] args, ServiceProvisionListener listener) throws Exception {
        OperationalString opString = buildOperationalString(args);
        /* Map result = */
        if (listener != null) {
            deployAdmin.deploy(opString, listener);
        } else {
            deployAdmin.deploy(opString);
        }
    }

    public OperationalString buildOperationalString(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("The pu name must be defined");
        }
        String puPath = args[args.length - 1];
        File puFile = new File(puPath);
        String puName = puFile.getName();
        // override pu name allows to change the actual pu name deployed from the on under deploy directory
        String overridePuName = puName;

        boolean deletePUFile = false;


        if (puFile.exists() && puFile.isDirectory()) {
            // this is a directory, jar it up and prepare it for upload
            File jarPUFile = new File(System.getProperty("java.io.tmpdir") + "/" + puName + ".jar");
            info("Deploying a directory [" + puFile.getAbsolutePath() + "], jaring it into [" + jarPUFile.getAbsolutePath() + "]");
            createJarFile(jarPUFile, puFile, null, null);
            puFile = jarPUFile;
            deletePUFile = true;
        }

        if (puFile.exists() && (puFile.getName().endsWith(".jar") || puFile.getName().endsWith(".war"))) {
            overridePuName = puFile.getName().substring(0, puFile.getName().length() - 4);
            puPath = overridePuName;
        }

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);

        // check if we have a groups parameter and timeout parameter
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("groups")) {
                setGroups(param.getArguments());
            }
            if (param.getName().equalsIgnoreCase("locators")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : param.getArguments()) {
                    sb.append(arg).append(',');
                }
                setLocators(sb.toString());
            }
            if (param.getName().equalsIgnoreCase("timeout")) {
                setLookupTimeout(Integer.valueOf(param.getArguments()[0]));
            }
            if (param.getName().equalsIgnoreCase("override-name")) {
                overridePuName = param.getArguments()[0];
            }
        }

        info("Deploying [" + puName + "] with name [" + overridePuName + "] under groups " + Arrays.asList(getGroups()) + " and locators [" + getLocators() + "]");

        // check if the pu to deploy is an actual file on the file system and ends with jar
        if (puFile.exists() && (puFile.getName().endsWith(".jar") || puFile.getName().endsWith(".war"))) {
            // we deploy a jar file, upload it to all the GSMs
            gsms = findGSMs();
            uploadPU(puPath, puFile, gsms);
            if (deletePUFile) {
                puFile.delete();
            }
        }

        //get codebase from service
        initDeployAdmin(gsms);
        String codeserver = getCodebase(deployAdmin);
        if (logger.isDebugEnabled()) {
            logger.debug("Using codeserver [" + codeserver + "]");
        }

        //list remote files, only works with webster
        URL root = new URL(codeserver);

        //read pu xml
        String puString = "";
        try {
            puString = readFile(root, puPath, "/META-INF/spring/pu.xml");
        } catch (IOException e) {
            logger.debug("Failed to find puPath " + puPath, e);
            // ignore, it might be ok for war files
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using PU xml [" + puString + "]");
        }

        Resource resource;

        // check to see if sla was passed as a parameter
        String slaString = puString;
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("sla")) {
                String slaLocation = param.getArguments()[0];
                info("Loading SLA from [" + slaLocation + "]");
                resource = new DefaultResourceLoader() {
                    // override the default load from the classpath to load from the file system
                    @Override
                    protected Resource getResourceByPath(String path) {
                        return new FileSystemResource(path);
                    }
                }.getResource(slaLocation);
                InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                slaString = FileCopyUtils.copyToString(reader);
                reader.close();
            }
        }
        if (slaString == puString) {
            // no sla passed as a parameter, try and load from default locations
            try {
                slaString = readFile(root, puPath, "/META-INF/spring/sla.xml");
            } catch (IOException e) {
                // no sla string found
                try {
                    slaString = readFile(root, puPath, "/sla.xml");
                } catch (IOException e1) {
                    // no sla string found
                }
            }
        }

        //get sla from pu string
        SLA sla = new SLA();
        if (StringUtils.hasText(slaString)) {
            resource = new ByteArrayResource(slaString.getBytes());
            XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
            // TODO: Need to find how to do it
//            Map<String, PropertyResourceConfigurer> map = xmlBeanFactory.getBeansOfType(PropertyResourceConfigurer.class);
//            for (PropertyResourceConfigurer cfg : map.values()) {
//                cfg.postProcessBeanFactory(xmlBeanFactory);
//            }
            try {
                sla = (SLA) xmlBeanFactory.getBean("SLA");
            } catch (NoSuchBeanDefinitionException e) {
                info("SLA Not Found in PU.  Using Default SLA.");
                sla = new SLA();
            }
        }

        ClusterInfo clusterInfo = ClusterInfoParser.parse(params);
        if (clusterInfo != null) {
            // override specific cluster info parameters on the SLA
            if (clusterInfo.getSchema() != null) {
                info("Overrding SLA cluster schema with [" + clusterInfo.getSchema() + "]");
                sla.setClusterSchema(clusterInfo.getSchema());
            }
            if (clusterInfo.getNumberOfInstances() != null) {
                info("Overrding SLA numberOfInstances with [" + clusterInfo.getNumberOfInstances() + "]");
                sla.setNumberOfInstances(clusterInfo.getNumberOfInstances());
                info("Overrding SLA numberOfBackups with [" + clusterInfo.getNumberOfBackups() + "]");
                if (clusterInfo.getNumberOfBackups() == null) {
                    sla.setNumberOfBackups(0);
                } else {
                    sla.setNumberOfBackups(clusterInfo.getNumberOfBackups());
                }
            }
        }

        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("max-instances-per-vm")) {
                String maxInstancePerVm = param.getArguments()[0];
                sla.setMaxInstancesPerVM(Integer.valueOf(maxInstancePerVm));
                info("Overrding SLA maxInstancesPerVM with [" + maxInstancePerVm + "]");
            }
            if (param.getName().equalsIgnoreCase("max-instances-per-machine")) {
                String maxInstancePerMachine = param.getArguments()[0];
                sla.setMaxInstancesPerMachine(Integer.valueOf(maxInstancePerMachine));
                info("Overrding SLA maxInstancesPerMachine with [" + maxInstancePerMachine + "]");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using SLA " + sla);
        }


        BeanLevelProperties beanLevelProperties = new BeanLevelProperties();

        URL puPropsURL = new URL(root, puPath + "/META-INF/spring/pu.properties");
        try {
            InputStream is = puPropsURL.openStream();
            if (is != null) {
                beanLevelProperties.getContextProperties().load(is);
                is.close();
            }
        } catch (Exception e) {
            // ignore, no file
        }

        //deploy to sg
        return loadDeployment(puString, codeserver, sla, puPath, overridePuName, BeanLevelPropertiesParser.parse(beanLevelProperties, params));
    }

    //copied from opstringloader
    private String[] getSLAConfigArgs(String type, String max, long lowerDampener, long upperDampener) {
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
                args[1] = slaPolicyHandler + "=" + "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".MaxServices=" + max;
                args[3] = handler + ".LowerThresholdDampeningFactor=" + lowerDampener;
                args[4] = handler + ".UpperThresholdDampeningFactor=" + upperDampener;
                break;
            case 2:
                args = new String[4];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" + "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".LowerThresholdDampeningFactor=" + lowerDampener;
                args[3] = handler + ".UpperThresholdDampeningFactor=" + upperDampener;
                break;
            default:
                args = new String[2];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" +
                        "new " + handler + "((org.jini.rio.core.SLA)$data)";
        }
        return (args);
    }

    private static String readFile(URL root, String puPath, String filePath) throws IOException {
        URL puURL = new URL(root, puPath + filePath);
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

    private OperationalString loadDeployment(String puString, String codeserver, SLA sla, String puPath,
                                             String puName, BeanLevelProperties beanLevelProperties) throws Exception {
        URL opstringURL = Deploy.class.getResource("/org/openspaces/pu/container/servicegrid/puservicebean.xml");
        OperationalString opString;

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader();
        opStringLoader.setDefaultGroups(getGroups());
        opStringLoader.setDefaultLookupLocators(BootUtil.toLookupLocators(getLocators()));
        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(opstringURL)[0];
        ((OpString) opString).setName(puName);

        //this opstring should only have one servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];

        //put the entire pu spring xml as parameter to servicebean
        element.getServiceBeanConfig().addInitParameter("pu", puString);

        if (beanLevelProperties != null) {
            element.getServiceBeanConfig().addInitParameter("beanLevelProperties", new MarshalledObject(beanLevelProperties));
        }

        //sla
        int numberOfInstances = sla.getNumberOfInstances();
        int numberOfBackups = sla.getNumberOfBackups();
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
                max = String.valueOf(((ScaleUpPolicy) policy).getMaxInstances());
                numberOfInstances = ((ScaleUpPolicy) policy).getMaxInstances();
            }
            String[] configParms = getSLAConfigArgs(type, max, policy.getLowerDampener(), policy.getUpperDampener());
            org.jini.rio.core.SLA slaElement = new org.jini.rio.core.SLA(
                    policy.getMonitor(),
                    new double[]{policy.getLow(), policy.getHigh()},
                    configParms,
                    null);
            element.getServiceLevelAgreements().addServiceSLA(slaElement);
        }

        //requirements
        applyRequirements(element, sla.getRequirements());

        element.getFaultDetectionHandlerBundle().addMethod("setConfiguration", new Object[]{new String[]{
                "-",
                "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler.invocationDelay = " + sla.getMemberAliveIndicator().getInvocationDelay(),
                "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler.retryCount = " + sla.getMemberAliveIndicator().getRetryCount(),
                "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler.retryTimeout = " + sla.getMemberAliveIndicator().getRetryTimeout()

        }});

        if (sla.getMaxInstancesPerVM() > 0) {
            element.setMaxPerMachine(sla.getMaxInstancesPerVM());
        }

        if (sla.getMaxInstancesPerMachine() > 0) {
            element.setMaxPerPhysicalMachine(sla.getMaxInstancesPerMachine());
        }

        element.setTotalNumberOfServices(sla.getNumberOfInstances());

        // set for each service to have the operation string name
        element.getServiceBeanConfig().setName(element.getOperationalStringName().replace(' ', '-') + "." + element.getName());

        // pass the SLA as an init parameter so the GSC won't need to parse the XML again
        element.getServiceBeanConfig().addInitParameter("sla", new MarshalledObject(sla));
        element.getServiceBeanConfig().addInitParameter("numberOfInstances", numberOfInstances);
        element.getServiceBeanConfig().addInitParameter("numberOfBackups", numberOfBackups);
        // add pu names, path and code server so it can be used on the service bean side
        element.getServiceBeanConfig().addInitParameter("puName", puName);
        element.getServiceBeanConfig().addInitParameter("puPath", puPath);

        //this is the MOST IMPORTANT part
        if (sla.getInstanceSLAs() != null && sla.getInstanceSLAs().size() > 0) {
            element.setPlanned(1);
            String name = element.getName();
            opString.removeService(element);
            for (int instanceId = 1; instanceId <= sla.getNumberOfInstances(); instanceId++) {
                ServiceElement clone = deepCopy(element);
                clone.getServiceBeanConfig().setName(name + "." + instanceId);
                clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(instanceId));
                clone.getServiceBeanConfig().addInitParameter("instanceId", String.valueOf(instanceId));
                InstanceSLA instanceSLA = findInstanceSLA(instanceId, null, sla.getInstanceSLAs());
                if (instanceSLA != null) {
                    applyRequirements(clone, instanceSLA.getRequirements());
                }
                opString.addService(clone);
                if (logger.isTraceEnabled()) {
                    logger.trace("Using Service Element " + element.toString());
                }
                for (int backupId = 1; backupId <= sla.getNumberOfBackups(); backupId++) {
                    clone = deepCopy(element);
                    clone.getServiceBeanConfig().setName(name + "." + instanceId + "_" + backupId);
                    clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(instanceId));
                    clone.getServiceBeanConfig().addInitParameter("instanceId", String.valueOf(instanceId));
                    clone.getServiceBeanConfig().addInitParameter("backupId", String.valueOf(backupId));
                    instanceSLA = findInstanceSLA(instanceId, backupId, sla.getInstanceSLAs());
                    if (instanceSLA != null) {
                        applyRequirements(clone, instanceSLA.getRequirements());
                    }
                    opString.addService(clone);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Using Service Element " + element.toString());
                    }
                }
            }
        } else {
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
        }

        return (opString);
    }

    private InstanceSLA findInstanceSLA(Integer instanceId, Integer backupId, List<InstanceSLA> instanceSLAs) {
        for (InstanceSLA instanceSLA : instanceSLAs) {
            if (instanceId.equals(instanceSLA.getInstanceId())) {
                if (backupId != null) {
                    if (backupId.equals(instanceSLA.getBackupId())) {
                        return instanceSLA;
                    }
                } else {
                    return instanceSLA;
                }
            }
        }
        return null;
    }

    private void applyRequirements(ServiceElement element, List<Requirement> requirements) {
        if (requirements == null) {
            return;
        }
        List<String> hosts = new ArrayList<String>();
        for (Requirement requirement : requirements) {
            if (requirement instanceof RangeRequirement) {
                RangeRequirement range = (RangeRequirement) requirement;
                ThresholdValues thresholdValues = new ThresholdValues(range.getLow(), range.getHigh());
                element.getServiceLevelAgreements().addSystemThreshold(range.getWatch(), thresholdValues);
            } else if (requirement instanceof HostRequirement) {
                hosts.add(((HostRequirement) requirement).getIp());
            } else if (requirement instanceof SystemRequirement) {
                SystemRequirement systemAttributes = (SystemRequirement) requirement;
                ServiceLevelAgreements.SystemRequirement systemRequirement = new ServiceLevelAgreements.SystemRequirement(
                        systemAttributes.getName(),
                        null,
                        systemAttributes.getAttributes()
                );
                element.getServiceLevelAgreements().addSystemRequirement(systemRequirement);
            }
        }
        //put hosts as cluster
        if (hosts.size() > 0) {
            element.setCluster(hosts.toArray(new String[hosts.size()]));
        }
    }

    private void uploadPU(String puPath, File puFile, GSM[] gsms) throws IOException {
        byte[] buffer = new byte[4098];
        for (GSM gsm : gsms) {
            String codebase = getCodebase((DeployAdmin) gsm.getAdmin());
            info("Uploading [" + puPath + "] to [" + codebase + "]");
            HttpURLConnection conn = (HttpURLConnection) new URL(codebase + puFile.getName()).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setAllowUserInteraction(false);
            conn.setUseCaches(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Length", "" + puFile.length());
            conn.setRequestProperty("Extract", "true");
            conn.connect();
            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            InputStream in = new BufferedInputStream(new FileInputStream(puFile));
            int byteCount = 0;
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            out.close();
            in.close();

            int responseCode = conn.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            conn.disconnect();
            if (responseCode != 200 && responseCode != 201) {
                throw new RuntimeException("Failed to upload file, response code [" + responseCode + "], response: " + sb.toString());
            }
        }
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
            System.out.println(getUsage());
            return;
        }

        // init GigaSpace logger
        GSLogConfigLoader.getLoader();

        Deploy deployer = new Deploy();
        deployer.deployAndWait(args);
    }

    public static String getUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: Deploy [-sla ...] [-cluster ...] [-groups groups] [-timeout timeoutValue] [-properties ...] PU_Name");
        sb.append("\n    PU_Name: The name of the processing unit under the deploy directory");
        sb.append("\n    -sla [sla-location]                      : Location of an optional xml file holding the SLA element");
        sb.append("\n    -cluster [cluster properties]            : Allows to override the cluster parameters of the SLA elements");
        sb.append("\n             schema=partitioned              : The cluster schema to override");
        sb.append("\n             total_members=1,1               : The number of instances and number of backups to override");
        sb.append("\n    -groups [groupName] [groupName] ...      : The lookup groups used to look up the GSM");
        sb.append("\n    -locators [host1] [host2] ...            : The lookup locators used to look up the GSM");
        sb.append("\n    -timeout [timeout value]                 : The timeout value of GSM lookup (defaults to 5000) in milliseconds");
        sb.append("\n    -properties [properties-loc]             : Location of context level properties");
        sb.append("\n    -properties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        sb.append("\n    -override-name [override pu name]        : An override pu name, useful when using pu as a template");
        sb.append("\n    -max-instances-per-vm [number]           : Allows to set the SLA number of instances per VM");
        sb.append("\n    -max-instances-per-machine [number]      : Allows to set the SLA number of instances per machine");
        sb.append("\n");
        sb.append("\n");
        sb.append("\nSome Examples:");
        sb.append("\n1. Deploy data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor");
        sb.append("\n2. Deploy -sla file://config/sla.xml data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor using an SLA element read from sla.xml");
        sb.append("\n3. Deploy -properties file://config/context.properties -properties space1 file://config/space1.properties data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor using context level properties called context.proeprties and bean level properties called space1.properties applied to bean named space1");
        sb.append("\n4. Deploy -properties embed://prop1=value1 -properties space1 embed://prop2=value2;prop3=value3 data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
        return sb.toString();
    }


    private static void info(String message) {
        if (disableInfoLogging) {
            return;
        }
        if (sout) {
            System.out.println(message);
        }
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    // utility to create a jar file

    private static class DeployListener implements ServiceProvisionListener {

        private AtomicInteger totalStarted = new AtomicInteger();

        public void succeeded(ServiceBeanInstance jsbInstance) throws RemoteException {
            info("[" + jsbInstance.getServiceBeanConfig().getName() + "] [" + jsbInstance.getServiceBeanConfig().getInstanceID() + "] deployed successfully on [" + jsbInstance.getHostAddress() + "]");
            totalStarted.incrementAndGet();
        }

        public void failed(ServiceElement sElem, boolean resubmitted) throws RemoteException {
            info("[" + sElem.getName() + "] [" + sElem.getServiceBeanConfig().getInstanceID() + "] failed to deploy, resubmitted [" + resubmitted + "]");
            totalStarted.incrementAndGet();
        }

        public int getTotalStarted() {
            return totalStarted.intValue();
        }
    }

    private static int sumUpServices(OperationalString deployment) {
        int summation = 0;
        ServiceElement[] elems = deployment.getServices();
        for (int i = 0; i < elems.length; i++) {
            ServiceElement element = elems[i];
            summation += element.getPlanned();
        }
        OperationalString[] nested = deployment.getNestedOperationalStrings();
        for (int i = 0; i < nested.length; i++) {
            summation += sumUpServices(nested[i]);
        }
        return (summation);
    }

    public static boolean createJarFile(File jarFile, File directory,
                                        String mainClass, FileFilter filter) {
        if (null == directory)
            throw new IllegalArgumentException("null directory");
        JarOutputStream out = null;
        try {
            OutputStream os = new FileOutputStream(jarFile);
            out = new JarOutputStream(os);
            ZipAccumulator reader = new ZipAccumulator(directory, out, filter);
            descendFileTree(directory, reader);
            out.closeEntry();
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err); // todo
        } finally {
            if (null != out) {
                try {
                    out.close();
                }
                catch (IOException e) {
                } // todo ignored
            }
        }

        return false;
    }

    static class ZipAccumulator implements FileFilter {
        final File parentDir;
        final ZipOutputStream out;
        final FileFilter filter;

        public ZipAccumulator(File parentDir, ZipOutputStream out,
                              FileFilter filter) {
            this.parentDir = parentDir;
            this.out = out;
            this.filter = filter;
        }

        public boolean accept(File f) {
            if ((null != filter) && (!filter.accept(f))) {
                return false;
            }
            try {
                addFileToZip(f, parentDir, out);
                return true;
            } catch (IOException e) {
                e.printStackTrace(System.err); // todo
            }
            return false;
        }
    }

    public static void descendFileTree(File file, FileFilter filter) {
        descendFileTree(file, filter, false);
    }

    public static boolean descendFileTree(File file, FileFilter fileFilter,
                                          boolean userRecursion) {
        if (null == file) {
            throw new IllegalArgumentException("parm File");
        }
        if (null == fileFilter) {
            throw new IllegalArgumentException("parm FileFilter");
        }

        if (!file.isDirectory()) {
            return fileFilter.accept(file);
        } else if (file.canRead()) {
            // go through files first
            File[] files = file.listFiles(ValidFileFilter.FILE_EXISTS);
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (!fileFilter.accept(files[i])) {
                        return false;
                    }
                }
            }
            // now recurse to handle directories
            File[] dirs = file.listFiles(ValidFileFilter.DIR_EXISTS);
            if (null != dirs) {
                for (int i = 0; i < dirs.length; i++) {
                    if (userRecursion) {
                        if (!fileFilter.accept(dirs[i])) {
                            return false;
                        }
                    } else {
                        if (!descendFileTree(dirs[i], fileFilter, userRecursion)) {
                            return false;
                        }
                    }
                }
            }
        } // readable directory (ignore unreadable ones)
        return true;
    } // descendFiles

    protected static void addFileToZip(File in, File parent,
                                       ZipOutputStream out)
            throws IOException {
        String path = in.getCanonicalPath();
        String parentPath = parent.getCanonicalPath();
        if (path.equals(parentPath)) {
            return;
        }
        if (!path.startsWith(parentPath)) {
            throw new Error("not parent: " + parentPath + " of " + path);
        } else {
            path = path.substring(1 + parentPath.length());
            path = path.replace('\'', '/'); // todo: use filesep
        }
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(in.lastModified());
        // todo: default behavior is DEFLATED

        out.putNextEntry(entry);

        InputStream input = null;
        try {
            input = new FileInputStream(in);
            byte[] buf = new byte[1024];
            int count;
            while (0 < (count = input.read(buf, 0, buf.length))) {
                out.write(buf, 0, count);
            }
        } finally {
            if (null != input) input.close();
        }
    }

    static class ValidFileFilter implements FileFilter {
        //----------------------------- singleton variants
        public static final FileFilter EXIST = new ValidFileFilter();
        public static final FileFilter FILE_EXISTS = new FilesOnlyFilter();
        public static final FileFilter DIR_EXISTS = new DirsOnlyFilter();
        public static final FileFilter CLASS_FILE = new ClassOnlyFilter();
        public static final FileFilter JAVA_FILE = new JavaOnlyFilter();

        //----------------------------- members
        protected final FileFilter delegate;

        protected ValidFileFilter() {
            this(null);
        }

        protected ValidFileFilter(FileFilter delegate) {
            this.delegate = delegate;
        }

        /**
         * Implement <code>FileFilter.accept(File)</code> by checking
         * taht input is not null, exists, and is accepted by any delegate.
         */
        public boolean accept(File f) {
            return ((null != f) && (f.exists())
                    && ((null == delegate) || delegate.accept(f)));
        }

        //----------------------------- inner subclasses
        static class FilesOnlyFilter extends ValidFileFilter {
            public boolean accept(File f) {
                return (super.accept(f) && (!f.isDirectory()));
            }
        }

        static class DirsOnlyFilter extends ValidFileFilter {
            public final boolean accept(File f) {
                return (super.accept(f) && (f.isDirectory()));
            }
        }

        // todo: StringsFileFilter, accepts String[] variants for each
        static class StringFileFilter extends ValidFileFilter {
            public static final boolean IGNORE_CASE = true;
            protected final String prefix;
            protected final String substring;
            protected final String suffix;
            protected final boolean ignoreCase;
            /**
             * true if one of the String specifiers is not null
             */
            protected final boolean haveSpecifier;

            public StringFileFilter(String prefix, String substring,
                                    String suffix, boolean ignoreCase) {
                this.ignoreCase = ignoreCase;
                this.prefix = preprocess(prefix);
                this.substring = preprocess(substring);
                this.suffix = preprocess(suffix);
                haveSpecifier = ((null != prefix) || (null != substring)
                        || (null != suffix));
            }

            private final String preprocess(String input) {
                if ((null != input) && ignoreCase) {
                    input = input.toLowerCase();
                }
                return input;
            }

            public boolean accept(File f) {
                if (!(super.accept(f))) {
                    return false;
                } else if (haveSpecifier) {
                    String path = preprocess(f.getPath());
                    if ((null == path) || (0 == path.length())) {
                        return false;
                    }
                    if ((null != prefix) && (!(path.startsWith(prefix)))) {
                        return false;
                    }
                    if ((null != substring) && (-1 == path.indexOf(substring))) {
                        return false;
                    }
                    if ((null != suffix) && (!(path.endsWith(suffix)))) {
                        return false;
                    }
                }
                return true;
            }
        } // class StringFileFilter

        static class ClassOnlyFilter extends StringFileFilter {
            ClassOnlyFilter() {
                super(null, null, ".class", IGNORE_CASE);
            }
        }

        static class JavaOnlyFilter extends StringFileFilter {
            JavaOnlyFilter() {
                super(null, null, ".java", IGNORE_CASE);
            }
        }
    } // class ValidFileFilter
}
