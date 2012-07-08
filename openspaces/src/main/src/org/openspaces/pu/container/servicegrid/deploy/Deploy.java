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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import net.jini.config.Configuration;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.jini.rio.boot.PUZipUtils;
import org.jini.rio.config.ExporterConfig;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.RequiredDependencies;
import org.jini.rio.core.ServiceBeanInstance;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.ServiceProvisionListener;
import org.jini.rio.core.ThresholdValues;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpString;
import org.jini.rio.opstring.OpStringLoader;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.container.support.RequiredDependenciesCommandLineParser;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.openspaces.pu.sla.InstanceSLA;
import org.openspaces.pu.sla.Policy;
import org.openspaces.pu.sla.RelocationPolicy;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.ScaleUpPolicy;
import org.openspaces.pu.sla.requirement.HostRequirement;
import org.openspaces.pu.sla.requirement.RangeRequirement;
import org.openspaces.pu.sla.requirement.Requirement;
import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.openspaces.pu.sla.requirement.ZoneRequirement;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.zone.ZoneHelper;
import com.gigaspaces.logger.GSLogConfigLoader;
import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;
import com.j_spaces.core.Constants;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.service.ServiceConfigLoader;
import com.j_spaces.kernel.PlatformVersion;

/**
 */
public class Deploy {

    private static final Log logger = LogFactory.getLog(Deploy.class);

    private GSM gsm = null;

    private DeployAdmin deployAdmin;

    private String[] groups;

    private LookupLocator[] locators;

    private int lookupTimeout = 5000;
    
    private long deployTimeout = Long.MAX_VALUE;

    private static boolean sout = false;

    private static boolean disableInfoLogging = false;

    public static void setDisableInfoLogging(boolean disableInfoLogging) {
        Deploy.disableInfoLogging = disableInfoLogging;
    }

    private void initGSM() {
        if (this.gsm != null) {
            return;
        }
        info("Searching for GSM in groups " + Arrays.toString(getGroups()) + " and locators [" + Arrays.toString(getLocators()) + "]");
        ServiceItem[] result = ServiceFinder.find(null, GSM.class, lookupTimeout, getGroups(), getLocators());
        if (result != null && result.length > 0) {
            gsm = (GSM) result[0].service;
        } else {
            throw new GSMNotFoundException(getGroups(), lookupTimeout);
        }
    }

    private void initDeployAdmin() {
        if (deployAdmin != null) {
            return;
        }
        try {
            deployAdmin = (DeployAdmin) gsm.getAdmin();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get deploy admin", e);
        }
    }

    public static void setSout(boolean soutVal) {
        sout = soutVal;
    }

    public void initializeDiscovery(GSM gsm) {
        this.gsm = gsm;
        try {
            deployAdmin = (DeployAdmin) gsm.getAdmin();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get deploy admin", e);
        }
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

    public LookupLocator[] getLocators() {
        if (locators == null) {
            String locatorsProperty = java.lang.System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty != null) {
                locators = BootUtil.toLookupLocators(locatorsProperty);
            }
        }
        return locators;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public void setLocators(String locators) {
        this.locators = BootUtil.toLookupLocators(locators);
    }

    public void setLookupTimeout(int lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    private Boolean secured;

    public void setSecured(boolean secured) {
        this.secured = secured;
    }
    
    private UserDetails userDetails;

    private String applicationName;

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setUserDetails(String userName, String password) {
        this.userDetails = new User(userName, password);
    }
    
    public void setDeployTimeout(long deployTimeout) {
        this.deployTimeout = deployTimeout;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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
        info("Waiting "+ (deployTimeout!=Long.MAX_VALUE?deployTimeout+" ms":"indefinitely") +" for [" + totalPlanned + "] processing unit instances to be deployed...");
        long expireDeployTimeout = (deployTimeout!=Long.MAX_VALUE ? System.currentTimeMillis() + deployTimeout : Long.MAX_VALUE);
        while (listener.getTotalEvents() < totalPlanned && System.currentTimeMillis() < expireDeployTimeout) {
            Thread.sleep(200);
        }
        if (System.currentTimeMillis() >= expireDeployTimeout) {
            info("Timed-out deploying [" + totalPlanned + "] processing unit instances");
        } else {
            info("Finished deploying [" + totalPlanned + "] processing unit instances");
        }
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
        String puName = puFile.getName().replace(' ', '_');
        // override pu name allows to change the actual pu name deployed from the on under deploy directory
        String overridePuName = puName;

        boolean deletePUFile = false;


        if (puFile.exists() && puFile.isDirectory()) {
            // this is a directory, jar it up and prepare it for upload
            File zipPUFile = new File(System.getProperty("java.io.tmpdir") + "/" + puName + ".zip");
            info("Deploying a directory [" + puFile.getAbsolutePath() + "], zipping it into [" + zipPUFile.getAbsolutePath() + "]");
            PUZipUtils.zip(puFile, zipPUFile);
            puFile = zipPUFile;
            deletePUFile = true;
        }

        if (puFile.getName().endsWith(".zip") || puFile.getName().endsWith(".jar") || puFile.getName().endsWith(".war")) {
            if (!puFile.exists()) {
                throw new IllegalArgumentException("File [" + puFile.getAbsolutePath() + "] not found and can't be deployed");
            }
            overridePuName = puFile.getName().substring(0, puFile.getName().length() - 4).replace(' ', '_');
            puPath = overridePuName;
        }

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);
        
        RequiredDependencies instanceDeploymentDependencies = new RequiredDependencies();
        RequiredDependencies instanceStartDependencies = new RequiredDependencies();
        
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
            if (param.getName().equalsIgnoreCase("deploy-timeout")) {
                setDeployTimeout(Long.valueOf(param.getArguments()[0]));
            }
            if (RequiredDependenciesCommandLineParser.isInstanceDeploymentDependencies(param)) {
                instanceDeploymentDependencies  = RequiredDependenciesCommandLineParser.convertCommandlineParameterToInstanceDeploymentDependencies(param);
            }
            if (RequiredDependenciesCommandLineParser.isInstanceStartDependencies(param)) {
                instanceStartDependencies  = RequiredDependenciesCommandLineParser.convertCommandlineParameterToInstanceStartDependencies(param);
            }
            if (param.getName().equalsIgnoreCase("application-name")) {
                setApplicationName(param.getArguments()[0]);
            }
        }

        info("Deploying [" + puName + "] with name [" + overridePuName + "] under groups " +
                Arrays.toString(getGroups()) + " and locators " + Arrays.toString(getLocators()));

        initGSM();
        initDeployAdmin();

        // check if the pu to deploy is an actual file on the file system and ends with jar
        if (puFile.exists() && (puFile.getName().endsWith(".zip") || puFile.getName().endsWith(".jar") || puFile.getName().endsWith(".war"))) {
            // we deploy a jar/zip/war file, upload it to the GSM
            uploadPU(puPath, puFile);
            if (deletePUFile) {
                puFile.delete();
            }
        }

        if (!gsm.hasPUUnderDeploy(puPath)) {
            throw new ProcessingUnitNotFoundException(puName, gsm);
        }

        String codeserver = getCodebase(deployAdmin);
        if (logger.isDebugEnabled()) {
            logger.debug("Using codeserver [" + codeserver + "]");
        }

        //list remote files, only works with webster
        URL root = new URL(codeserver);

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
        puPropsURL = new URL(root, puPath + "/pu.properties");
        try {
            InputStream is = puPropsURL.openStream();
            if (is != null) {
                beanLevelProperties.getContextProperties().load(is);
                is.close();
            }
        } catch (Exception e) {
            // ignore, no file
        }
        beanLevelProperties = BeanLevelPropertiesParser.parse(beanLevelProperties, params);


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
        
        boolean slaInPu = true;
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
                slaInPu = false;
            }
        }
        if (slaString == puString) {
            // no sla passed as a parameter, try and load from default locations
            try {
                slaString = readFile(root, puPath, "/META-INF/spring/sla.xml");
                slaInPu = false;
            } catch (IOException e) {
                // no sla string found
                try {
                    slaString = readFile(root, puPath, "/sla.xml");
                    slaInPu = false;
                } catch (IOException e1) {
                    // no sla string found
                }
            }
        }

        //get sla from pu string
        SLA sla = new SLA();
        if (StringUtils.hasText(slaString)) {
            resource = new ByteArrayResource(slaString.getBytes());
            if (slaInPu) {
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
            } else {
                // if we have specific sla file, load it as usual, so we can have deploy properties / system properties injected to it
                ResourceApplicationContext applicationContext = new ResourceApplicationContext(new Resource[]{resource}, null);
                if (beanLevelProperties != null) {
                    applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties, null));
                    applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
                }
                // start the application context
                applicationContext.refresh();
                try {
                    sla = (SLA) applicationContext.getBean("SLA");
                } catch (NoSuchBeanDefinitionException e) {
                    throw new IllegalArgumentException("Failed to find SLA from in [" + slaString + "]");
                } finally {
                    applicationContext.close();
                }
            }
        }

        ClusterInfo clusterInfo = ClusterInfoParser.parse(params);
        if (clusterInfo != null) {

            // override specific cluster info parameters on the SLA
            if (clusterInfo.getSchema() != null && clusterInfo.getSchema().length() > 0) {
                info("Overrding SLA cluster schema with [" + clusterInfo.getSchema() + "]");
                sla.setClusterSchema(clusterInfo.getSchema());
            }
            if (clusterInfo.getNumberOfInstances() != null) {
                info("Overrding SLA numberOfInstances with [" + clusterInfo.getNumberOfInstances() + "]");
                sla.setNumberOfInstances(clusterInfo.getNumberOfInstances());
                if (clusterInfo.getNumberOfBackups() == null) {
                    info("Overrding SLA numberOfBackups with [" + clusterInfo.getNumberOfBackups() + "]");
                    sla.setNumberOfBackups(0);
                } else {
                    if (sla.getClusterSchema() == null) {
                        throw new IllegalArgumentException("Number of backup instances override without a specified cluster schema");
                    }
                    info("Overrding SLA numberOfBackups with [" + clusterInfo.getNumberOfBackups() + "]");
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
            if (param.getName().equalsIgnoreCase("max-instances-per-zone")) {
                Map<String, Integer> map = new HashMap<String, Integer>();
                for (String arg : param.getArguments()) {
                    map.putAll(ZoneHelper.parse(arg));
                }
                sla.setMaxInstancesPerZone(map);
                info("Overrding SLA maxInstancesPerZone with [" + ZoneHelper.unparse(map) + "]");
            }
            if (param.getName().equalsIgnoreCase("zones")) {
                for (String arg : param.getArguments()) {
                    sla.getRequirements().add(new ZoneRequirement(arg));
                    info("Adding SLA required zone with [" + arg + "]");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using SLA " + sla);
        }

        // detive the user details if they provided using deploy time properties
        String userName = null;
        String password = null;
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equals("user")) {
                userName = param.getArguments()[0];
            } else if (param.getName().equals("password")) {
                password = param.getArguments()[0];
            } else if (param.getName().equals("secured")) {
                if (param.getArguments().length == 0) {
                    setSecured(true);
                } else {
                    setSecured(Boolean.parseBoolean(param.getArguments()[0]));
                }
            }
        }
        if (userName != null && password != null) {
            setUserDetails(userName, password);
        }

        if (userDetails == null) {
            userName = (String) beanLevelProperties.getContextProperties().remove(Constants.Security.USERNAME);
            password = (String) beanLevelProperties.getContextProperties().remove(Constants.Security.PASSWORD);
            if (userName != null && password != null) {
                setUserDetails(userName, password);
            }
        }
        // init the user detalis
        if (userDetails != null) {
            beanLevelProperties.getContextProperties().setProperty(SpaceURL.SECURED, "true");
            beanLevelProperties.getContextProperties().put(Constants.Security.USER_DETAILS, new MarshalledObject<UserDetails>(userDetails));
        } else if (secured != null && secured) {
            beanLevelProperties.getContextProperties().setProperty(SpaceURL.SECURED, "true");
        }
        
        //Get elastic properties
        Map<String, String> elasticProperties = new HashMap<String, String>();
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("elastic-properties")) {
                for (String argument : param.getArguments()) {
                    int indexOfEqual = argument.indexOf("=");                    
                    String name = argument.substring(0, indexOfEqual);
                    String value = argument.substring(indexOfEqual + 1);
                    elasticProperties.put(name, value);
                }
            }
        }
        
        //get pu type
        String puType = guessProcessingUnitType(puPath, puFile, root, puString, sla, beanLevelProperties);
        beanLevelProperties.getContextProperties().put("pu.type", puType);

        //deploy to sg
        return loadDeployment(puString, codeserver, sla, puPath, overridePuName, beanLevelProperties, elasticProperties, instanceDeploymentDependencies, instanceStartDependencies, applicationName);
    }

    private String guessProcessingUnitType(String puPath, File puFile, URL root, String puString, SLA sla, BeanLevelProperties beanLevelProperties) {

        if (containsWEB_INF(puPath, root)) {
            return ProcessingUnitType.WEB.name(); //Web APP
        } 
        if (containsEXT(puPath, root)) {
            return ProcessingUnitType.UNIVERSAL.name(); //USM
        }         
        if (sla.getClusterSchema() != null) {
            return ProcessingUnitType.STATEFUL.name(); //cluster space
        } 
        
        String puStringWithoutComments = removeCommentsFromPuString(puString);
        if (puStringWithoutComments.contains("os-core:mirror") || puStringWithoutComments.contains("schema=\"mirror\"")) {
            return ProcessingUnitType.MIRROR.name(); //mirror space
        } 
        if (puStringWithoutComments.contains("os-core:space")) {
            if (puStringWithoutComments.contains("url=\"/./")) {
                return ProcessingUnitType.STATEFUL.name(); //embedded space
            }
            if (puStringWithoutComments.contains("url=\"${")) { //Extract place holder
                int beginIndex = puStringWithoutComments.indexOf("url=\"${") + 7; //length
                if (beginIndex != -1) {
                    int endIndex = puStringWithoutComments.indexOf("}", beginIndex);
                    if (endIndex != -1) {
                        String propertyKey = puStringWithoutComments.substring(beginIndex, endIndex);
                        if (beanLevelProperties.getContextProperties().getProperty(propertyKey,"").startsWith("/./")) {
                            return ProcessingUnitType.STATEFUL.name(); //embedded space
                        }
                    }
                }
            }
        } if (puStringWithoutComments.contains("os-gateway:sink") || puStringWithoutComments.contains("os-gateway:delegator")) {
            return ProcessingUnitType.GATEWAY.name();            
        } if (puStringWithoutComments.length() == 0 && beanLevelProperties.getContextProperties().containsKey("dataGridName")) {
            return ProcessingUnitType.STATEFUL.name(); //.Net stateful
        }
        
        return ProcessingUnitType.STATELESS.name(); //default
    }

    /** search for /web-inf */
    private boolean containsWEB_INF(String puPath, URL root) {
        boolean containsWebInf = false;
        try {
            URL webInfURL = new URL(root, puPath + "/WEB-INF");
            InputStream is = webInfURL.openStream();
            if (is != null) {
                containsWebInf = true;
                is.close();
            }
        } catch (Exception e) {
            // ignore, no file
        }
        return containsWebInf;
    }
    
    /** search for /ext (assuming that only USM has this! TODO - find better distinction) */
    private boolean containsEXT(String puPath, URL root) {
        boolean containsExt = false;
        try {
            URL extURL = new URL(root, puPath + "/ext");
            InputStream is = extURL.openStream();
            if (is != null) {
                containsExt = true;
                is.close();
            }
        } catch (Exception e) {
            // ignore, no file
        }
        return containsExt;
    }
    
    /*
     * Remove all comments from pu.xml string
     * in JDK1.6, there is a stack overflow bug when using regex matching to remove the comments:
     *  puString.replaceAll("<!--(?:[^-]|-(?!->))*-->","")
     */
    private String removeCommentsFromPuString(final String puString) {
        if (puString.length() == 0) return puString;
        final String openComment = "<!--";
        final String closeComment = "-->";
        String parsedString = puString;
        String newString = "";
        for (;;) {
            int startCommentIdx = parsedString.indexOf(openComment);
            int endCommentIdx = parsedString.indexOf(closeComment, startCommentIdx);
            if (startCommentIdx == -1 || endCommentIdx == -1) {
                newString = newString.concat(parsedString);
                break;
            }
            newString = newString.concat(parsedString.substring(0, startCommentIdx));
            parsedString = parsedString.substring(endCommentIdx+closeComment.length());
        }
        return newString;
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

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private static String readFile(URL root, String puPath, String filePath) throws IOException {
        URL puURL = new URL(root, puPath + filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(puURL.openStream()));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append(LINE_SEPARATOR);
        }
        reader.close();
        return buffer.toString();
    }

    private String getCodebase(DeployAdmin deployAdmin) throws MalformedURLException, RemoteException {
        return deployAdmin.getDeployURL();
    }

    private OperationalString loadDeployment(String puString, String codeserver, SLA sla, String puPath,
            String puName, 
            BeanLevelProperties beanLevelProperties, 
            Map<String, String> elasticProperties, 
            RequiredDependencies deploymentRequiredDependencies, 
            RequiredDependencies startRequiredDependencies, 
            String applicationName) 
                    throws Exception {
        
        InputStream opstringURL = Deploy.class.getResourceAsStream("/org/openspaces/pu/container/servicegrid/puservicebean.xml");
        OperationalString opString;

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader();
        opStringLoader.setDefaultGroups(getGroups());
        opStringLoader.setDefaultLookupLocators(getLocators());
        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(opstringURL)[0];
        ((OpString) opString).setName(puName);

        //this opstring should only have one servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];

        //put the entire pu spring xml as parameter to servicebean
        element.getServiceBeanConfig().addInitParameter("pu", puString);

        if (beanLevelProperties != null) {
            element.getServiceBeanConfig().addInitParameter("pu.type", beanLevelProperties.getContextProperties().remove("pu.type"));
            element.getServiceBeanConfig().addInitParameter("beanLevelProperties", new MarshalledObject<BeanLevelProperties>(beanLevelProperties));
            element.getServiceBeanConfig().addInitParameter("jee.container", beanLevelProperties.getContextProperties().getProperty("jee.container", "jetty"));
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
        element.setMaxPerZone(sla.getMaxInstancesPerZone());
        
        element.setElasticProperties(elasticProperties);
        
        element.setInstanceDeploymentDependencies(deploymentRequiredDependencies);
        element.setInstanceStartDependencies(startRequiredDependencies);
        element.setApplicationName(applicationName);
        element.setTotalNumberOfServices(sla.getNumberOfInstances());

        // set for each service to have the operation string name
        element.getServiceBeanConfig().setName(element.getOperationalStringName().replace(' ', '-'));

        // pass the SLA as an init parameter so the GSC won't need to parse the XML again
        element.getServiceBeanConfig().addInitParameter("sla", new MarshalledObject<SLA>(sla));
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
        if (requirements == null || requirements.isEmpty()) {
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
            } else if (requirement instanceof ZoneRequirement) {
                element.addRequiredZone(((ZoneRequirement) requirement).getZone());
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

    private void uploadPU(String puPath, File puFile) throws IOException {
        byte[] buffer = new byte[4098];
        String codebase = getCodebase(deployAdmin);
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
        return getUsage(false);
    }

    public static String getUsage(boolean managed) {
        StringBuilder sb = new StringBuilder();
        if (!managed) {
            sb.append("Usage: Deploy [-sla ...] [-cluster ...] [-groups groups] [-locators hots1 hots2] [-timeout timeoutValue] [-properties ...] [-user xxx -password yyy] [-secured true/false] PU_Name");
        } else {
            sb.append("Usage: deploy [-sla ...] [-cluster ...] [-properties ...] [-user xxx -password yyy] [-secured true/false] PU_Name");
        }
        sb.append("\n    PU_Name: The name of the processing unit under the deploy directory, or packaged jar file");
        sb.append("\n    -sla [sla-location]                      : Location of an optional xml file holding the SLA element");
        sb.append("\n    -cluster [cluster properties]            : Allows to override the cluster parameters of the SLA elements");
        sb.append("\n             schema=partitioned-sync2backup  : The cluster schema to override");
        sb.append("\n             total_members=1,1               : The number of instances and number of backups to override");
        if (!managed) {
            sb.append("\n    -groups [groupName] [groupName] ...      : The lookup groups used to look up the GSM");
            sb.append("\n    -locators [host1] [host2] ...            : The lookup locators used to look up the GSM");
            sb.append("\n    -timeout [timeout value]                 : The timeout value of GSM lookup (defaults to 5000) in milliseconds");
        }
        sb.append("\n    -user xxx -password yyyy                 : Deploys a secured processing unit propagated with the supplied user and password");
        sb.append("\n    -secured true                            : Deploys a secured processing unit (implicit when using -user/-password)");
        sb.append("\n    -properties [properties-loc]             : Location of context level properties");
        sb.append("\n    -properties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        sb.append("\n    -override-name [override pu name]        : An override pu name, useful when using pu as a template");
        sb.append("\n    -max-instances-per-vm [number]           : Allows to set the SLA number of instances per VM");
        sb.append("\n    -max-instances-per-machine [number]      : Allows to set the SLA number of instances per machine");
        sb.append("\n    -max-instances-per-zone [zone/number,...]: Allows to set the SLA number of instances per zone");
        sb.append("\n    -zones [zoneName] [zoneName] ...         : Allows to set the SLA zone requirements");
        sb.append("\n    -deploy-timeout [timeout value in ms]    : Timeout for deploy operation, otherwise blocks until all successful/failed deployment events arrive (default)");
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

        private final AtomicInteger totalEvents = new AtomicInteger();

        public void succeeded(ServiceBeanInstance jsbInstance) throws RemoteException {
            info("[" + jsbInstance.getServiceBeanConfig().getName() + "] [" + jsbInstance.getServiceBeanConfig().getInstanceID() + "] deployed successfully on [" + jsbInstance.getHostAddress() + "]");
            totalEvents.incrementAndGet();
        }

        public void failed(ServiceElement sElem, boolean resubmitted) throws RemoteException {
            info("[" + sElem.getName() + "] [" + sElem.getServiceBeanConfig().getInstanceID() + "] failed to deploy, resubmitted [" + resubmitted + "]");
            totalEvents.incrementAndGet();
        }

        public int getTotalEvents() {
            return totalEvents.intValue();
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
}
