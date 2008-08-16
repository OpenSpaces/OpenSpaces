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

package org.openspaces.pu.container.jee.apache;

import com.gigaspaces.logger.GSLogConfigLoader;
import com.j_spaces.core.Constants;
import com.j_spaces.kernel.PlatformVersion;
import com.j_spaces.kernel.SecurityPolicyLoader;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import org.jini.rio.boot.BootUtil;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.servicegrid.JeePUServiceDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.pu.container.servicegrid.PUServiceDetails;
import org.openspaces.pu.container.support.CommandLineParser;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Experimental support for automatically refreshing apache load balancer.
 *
 *
 * <p>Note, internal API here on how to find processing units is subject to change.
 *
 * @author kimchy
 */
public class ApacheLoadBalancerAgent implements DiscoveryListener, ServiceDiscoveryListener, Runnable {

    private String[] groups;

    private String locators;

    private String apachectlLocation;

    private String configLocation;

    private String restartCommand;

    private LookupDiscoveryManager ldm;
    private ServiceDiscoveryManager sdm;
    private LookupCache cache;

    private Map<String, LoadBalancersInfo> loadBalancersInfoMap = new ConcurrentHashMap<String, LoadBalancersInfo>();

    private volatile boolean running = false;

    private Thread configThread;

    private int updateInterval = 10000;

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

    public void setGroups(String[] groups) {
        this.groups = groups;
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

    public void setLocators(String locators) {
        this.locators = locators;
    }

    public String getApachectlLocation() {
        return apachectlLocation;
    }

    public void setApachectlLocation(String apachectlLocation) {
        this.apachectlLocation = apachectlLocation;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getRestartCommand() {
        return restartCommand;
    }

    public void setRestartCommand(String restartCommand) {
        this.restartCommand = restartCommand;
    }

    public void start() throws Exception {
        System.out.println("Starting Apache Load Balancer Agent...");
        System.out.println("");
        System.out.println("groups " + Arrays.toString(getGroups()) + ", locators [" + locators + "]");

        if (restartCommand == null && apachectlLocation == null) {
            throw new IllegalStateException("Must provide either apachectl location or direct restart command");
        }
        if (apachectlLocation != null && !new File(apachectlLocation).exists()) {
            throw new IllegalArgumentException("apacheclt Location [" + apachectlLocation + "] does not exists");
        }
        if (configLocation == null) {
            throw new IllegalArgumentException("config directory location must be provided");
        }
        new File(configLocation).mkdirs();

        if (restartCommand == null) {
            if (isWindows()) {
                restartCommand = "\"" + apachectlLocation + "\" -k restart";
            } else {
                restartCommand = apachectlLocation + " graceful";
            }
        }

        System.out.println("apachectl Location [" + apachectlLocation + "]");
        System.out.println("config directory [" + configLocation + "]");
        System.out.println("update config interval [" + updateInterval + "ms]");
        System.out.println("");

        loadBalancersInfoMap.clear();
        // list all the files and init with empty load balancers
        System.out.println("Detecting exising config files...");
        new File(configLocation).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String clusterName = name.substring(0, name.length() - ".conf".length());
                System.out.println("[" + clusterName + "]: existing config detected");
                loadBalancersInfoMap.put(clusterName, new LoadBalancersInfo());
                return false;
            }
        });
        System.out.println("Done detecting exising config files");
        System.out.println("");

        ldm = new LookupDiscoveryManager(getGroups(), BootUtil.toLookupLocators(getLocators()), this);

        sdm = new ServiceDiscoveryManager(ldm, null);
        ServiceTemplate template = new ServiceTemplate(null, new Class[]{PUServiceBean.class}, null);
        cache = sdm.createLookupCache(template, null, this);

        running = true;
        configThread = new Thread(this, "Config Writer Thread");
        configThread.setDaemon(false);
        configThread.start();

        System.out.println("");
        System.out.println("Started Apache Load Balancer Agent successfully");
        System.out.println("Make sure Apache is configured with [Include " + new File(configLocation).getAbsolutePath() + "/*.conf]");
        System.out.println("");
    }

    public void stop() {
        running = false;
        System.out.println("Stopping Apached Load Balancer Agent...");
        cache.terminate();
        sdm.terminate();
        ldm.terminate();
        System.out.println("Stopped Apached Load Balancer Agent");
    }

    public void discovered(DiscoveryEvent event) {
        System.out.println("LUS Discovered " + Arrays.toString(event.getRegistrars()));
    }

    public void discarded(DiscoveryEvent event) {
        System.out.println("LUS Discarded " + Arrays.toString(event.getRegistrars()));
    }

    public synchronized void serviceAdded(ServiceDiscoveryEvent event) {
        PUServiceBean service = (PUServiceBean) event.getPostEventServiceItem().service;
        try {
            PUServiceDetails[] details = service.listServiceDetails();
            ClusterInfo clusterInfo = service.getClusterInfo();
            for (PUServiceDetails detail : details) {
                if (detail instanceof JeePUServiceDetails) {
                    JeePUServiceDetails jeeDetails = (JeePUServiceDetails) detail;
                    LoadBalancersInfo loadBalancersInfo = loadBalancersInfoMap.get(clusterInfo.getName());
                    if (loadBalancersInfo == null) {
                        loadBalancersInfo = new LoadBalancersInfo();
                        loadBalancersInfoMap.put(clusterInfo.getName(), loadBalancersInfo);
                    }
                    loadBalancersInfo.putBalancer(new LoadBalancerInfo(clusterInfo, (JeePUServiceDetails) detail));
                    loadBalancersInfo.setDirty(true);

                    System.out.println("[" + clusterInfo.getName() + "]: Adding [" + clusterInfo.getRunningNumberOffset1() + "] [" + jeeDetails.getHost() + ":" + jeeDetails.getPort() + jeeDetails.getContextPath() + "]");
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to add service");
            e.printStackTrace(System.out);
        }
    }

    public synchronized void serviceRemoved(ServiceDiscoveryEvent event) {
        PUServiceBean service = (PUServiceBean) event.getPreEventServiceItem().service;
        try {
            PUServiceDetails[] details = service.listServiceDetails();
            ClusterInfo clusterInfo = service.getClusterInfo();
            for (PUServiceDetails detail : details) {
                JeePUServiceDetails jeeDetails = (JeePUServiceDetails) detail;
                if (detail instanceof JeePUServiceDetails) {
                    LoadBalancersInfo loadBalancersInfo = loadBalancersInfoMap.get(clusterInfo.getName());
                    if (loadBalancersInfo == null) {
                        continue;
                    }
                    loadBalancersInfo.removeBalancer(new LoadBalancerInfo(clusterInfo, (JeePUServiceDetails) detail));
                    loadBalancersInfo.setDirty(true);
                    System.out.println("[" + clusterInfo.getName() + "]: Removing [" + clusterInfo.getRunningNumberOffset1() + "] [" + jeeDetails.getHost() + ":" + jeeDetails.getPort() + jeeDetails.getContextPath() + "]");
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to remove service");
            e.printStackTrace(System.out);
        }
    }

    public synchronized void serviceChanged(ServiceDiscoveryEvent event) {
        // don't care about this one
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                break;
            }
            boolean dirty = false;
            for (Map.Entry<String, LoadBalancersInfo> entry : loadBalancersInfoMap.entrySet()) {
                LoadBalancerInfo[] infos = null;
                synchronized (this) {
                    if (entry.getValue().isDirty()) {
                        infos = entry.getValue().getBlanacers();
                        entry.getValue().setDirty(false);
                    }
                }
                if (infos == null) {
                    continue;
                }
                dirty = true;
                System.out.println("[" + entry.getKey() + "]: Detected as dirty, updating config file...");
                File confFile = new File(configLocation + "/" + entry.getKey() + ".conf");
                try {
                    PrintWriter writer = new PrintWriter(new FileOutputStream(confFile));
                    writer.println("ProxyPass /" + entry.getKey() + " balancer://" + entry.getKey() + "_cluster/ stickysession=JSESSIONID nofailover=Off");
                    writer.println("");
                    writer.println("<Proxy balancer://" + entry.getKey() + "_cluster>");
                    for (LoadBalancerInfo info : infos) {
                        JeePUServiceDetails serviceDetails = info.getServiceDetails();
                        ClusterInfo clusterInfo = info.getClusterInfo();
                        writer.println("\tBalancerMember http://" + serviceDetails.getHost() + ":" + serviceDetails.getPort() + serviceDetails.getContextPath() + " route=" + clusterInfo.getName() + clusterInfo.getRunningNumberOffset1());
                    }
                    writer.println("</Proxy>");
                    writer.flush();
                    writer.close();
                    System.out.println("[" + entry.getKey() + "]: Updated config file");
                } catch (Exception e) {
                    System.out.println("Failed to write config file, will try again later");
                    e.printStackTrace(System.out);
                    entry.getValue().setDirty(true);
                }
            }
            if (dirty) {
                System.out.println("Executing [" + restartCommand + "]...");
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(restartCommand);
                    boolean exited = waitFor(process, 60000);
                    int exitValue = -999;
                    if (exited) {
                        exitValue = process.exitValue();
                    }
                    System.out.println("Executed [" + restartCommand + "], exit code [" + exitValue + "]");
                    if (exitValue != 0) {
                        String output = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
                        System.out.println(output);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to run [" + restartCommand + "]");
                    e.printStackTrace(System.out);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        try {
                            process.getErrorStream().close();
                        } catch (Exception e) {
                            // do nothing
                        }
                        try {
                            process.getInputStream().close();
                        } catch (Exception e) {
                            // do nothing
                        }
                        try {
                            process.getOutputStream().close();
                        } catch (Exception e) {
                            // do mothing
                        }
                        try {
                            process.destroy();
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                }

            }
        }
    }

    private boolean waitFor(Process process, long timeout) throws InterruptedException {
        /* interval constant */
        final int interval = 200; // 200 milliseconds
        long timeWaiting = 0;

        while (timeWaiting < timeout) {
            if (!isProcessAlive(process))
                return true;

            /* process still alive, wait next interval */
            Thread.sleep(interval);
            timeWaiting += interval;
        }

        /* process hasn't been destroyed */
        return false;
    }

    /**
     * @return <code>true</code> if supplied process is still alive, otherwise <code>false</code>
     */
    private boolean isProcessAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            return true;
        } else {
            return false;
        }
    }

    public class LoadBalancersInfo {

        private Map<Integer, LoadBalancerInfo> balancers = new ConcurrentHashMap<Integer, LoadBalancerInfo>();

        private volatile boolean dirty = true;

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public void putBalancer(LoadBalancerInfo balancerInfo) {
            balancers.put(balancerInfo.getClusterInfo().getRunningNumberOffset1(), balancerInfo);
        }

        public void removeBalancer(LoadBalancerInfo balancerInfo) {
            balancers.remove(balancerInfo.getClusterInfo().getRunningNumberOffset1());
        }

        public LoadBalancerInfo[] getBlanacers() {
            return balancers.values().toArray(new LoadBalancerInfo[0]);
        }
    }

    public class LoadBalancerInfo {

        private ClusterInfo clusterInfo;

        private JeePUServiceDetails serviceDetails;

        public LoadBalancerInfo(ClusterInfo clusterInfo, JeePUServiceDetails serviceDetails) {
            this.clusterInfo = clusterInfo;
            this.serviceDetails = serviceDetails;
        }

        public ClusterInfo getClusterInfo() {
            return clusterInfo;
        }

        public JeePUServiceDetails getServiceDetails() {
            return serviceDetails;
        }
    }

    public static void main(String[] args) throws Exception {
        GSLogConfigLoader.getLoader();
        if (System.getProperty("java.security.policy") == null) {
            SecurityPolicyLoader.loadPolicy(Constants.System.SYSTEM_GS_POLICY);
        }
        final ApacheLoadBalancerAgent agent = new ApacheLoadBalancerAgent();

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length);
        String apachectlLocation = null;
        String apcaheLocation = null;
        String configLocation = null;
        String restartCommand = null;
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("groups")) {
                agent.setGroups(param.getArguments());
            }
            if (param.getName().equalsIgnoreCase("locators")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : param.getArguments()) {
                    sb.append(arg).append(',');
                }
                agent.setLocators(sb.toString());
            }
            if (param.getName().equalsIgnoreCase("apache")) {
                apcaheLocation = param.getArguments()[0];
            }
            if (param.getName().equalsIgnoreCase("restart-command")) {
                restartCommand = param.getArguments()[0];
            }
            if (param.getName().equalsIgnoreCase("apachectl")) {
                apachectlLocation = param.getArguments()[0];
            }
            if (param.getName().equalsIgnoreCase("conf-dir")) {
                configLocation = param.getArguments()[0];
            }
            if (param.getName().equalsIgnoreCase("update-interval")) {
                agent.setUpdateInterval(Integer.parseInt(param.getArguments()[0]));
            }
        }

        if (restartCommand != null) {
            agent.setRestartCommand(restartCommand);
        }

        if (apcaheLocation == null) {
            if (isWindows()) {
                String programFiles = System.getenv("ProgramFiles");
                if (programFiles != null) {
                    String location = programFiles + "/Apache Software Foundation/Apache2.2";
                    if (new File(location + "/bin/httpd.exe").exists()) {
                        apcaheLocation = location;
                    }
                }
            } else {
                String location = "/opt/local/apache2";
                if (new File(location + "/bin/apachectl").exists()) {
                    apcaheLocation = location;
                } else {
                    location = "/opt/apache2";
                    if (new File(location + "/bin/apachectl").exists()) {
                        apcaheLocation = location;
                    }
                }
            }
        }

        if (apachectlLocation != null) {
            agent.setApachectlLocation(apachectlLocation);
        } else if (apcaheLocation != null) {
            if (isWindows()) {
                agent.setApachectlLocation(apcaheLocation + "/bin/httpd.exe");
            } else {
                agent.setApachectlLocation(apcaheLocation + "/bin/apachectl");
            }
        } else {
            throw new IllegalArgumentException("Either apache location or apachectl location must be provided");
        }

        if (configLocation != null) {
            agent.setConfigLocation(configLocation);
        } else if (apcaheLocation != null) {
            agent.setConfigLocation(apcaheLocation + "/conf/gigaspaces");
        } else {
            throw new IllegalArgumentException("Either conig director location or apache location must be provied");
        }

        try {
            agent.start();

            // Use the MAIN thread as the non daemon thread to keep it alive
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        agent.stop();
                    } finally {
                        mainThread.interrupt();
                    }
                }
            });
            while (!mainThread.isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    // do nothing, simply exit
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            printUsage();
            System.exit(1);
        }
    }

    public static void printUsage() {
        System.out.println("Usage: [-apache location] [-conf-dir location] [-update-interval value] [-restart-command command]");
        System.out.println("    -apache [location]       : The installation location of apache. Defautls to windows/unix common locations");
        System.out.println("    -conf-dir [location]     : The directory where the load balancer config files will be created. Defaults to [apache]/conf/gigaspaces");
        System.out.println("    -update-interval [value] : The interval (in milliseconds) when the load balancer conf files will be updated");
        System.out.println("    -restart-command [value] : The directy restart command for apache. Defualts to sensible values for windowns and unix systems");
        System.out.println("");
    }
}
