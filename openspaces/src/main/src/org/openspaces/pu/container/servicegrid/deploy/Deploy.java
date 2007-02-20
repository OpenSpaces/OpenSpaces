package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpString;
import org.jini.rio.opstring.OpStringLoader;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.container.servicegrid.SLAUtil;
import org.openspaces.pu.container.servicegrid.sla.SLA;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Jan 28, 2007
 * Time: 11:24:32 AM
 */
public class Deploy {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = Logger.getLogger(Deploy.class.getName());

// -------------------------- STATIC METHODS --------------------------

    static OperationalString loadDeployment(String puString, String codeserver, String jiniGroup, SLA sla, File[] jars, String puPath, String puName) throws Exception {
        URL opstringURL = Deploy.class.getResource("/org/openspaces/pu/container/servicegrid/puservicebean.xml");
        System.out.println("opstringURL = " + opstringURL);
        OperationalString opString = null;

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader();
        opStringLoader.setDefaultGroups(new String[]{jiniGroup});
        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(opstringURL)[0];
        ((OpString) opString).setName(puName);
        //this opstring should only have one servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];
        //put the entire pu spring xml as parameter to servicebean
        element.getServiceBeanConfig().addInitParameter("pu", puString);
        element.setPlanned(sla.getNumberOfInstances() + sla.getNumberOfBackups());
        //put jars
        ClassBundle classBundle = element.getComponentBundle();
        classBundle.addJAR(puPath + "/" + "classes/");
        for (int i = 0; i < jars.length; i++) {
            File jar = jars[i];
            String path = jar.getPath();
            classBundle.addJAR(path);
            System.out.println("added jar:" + path);
        }
        return (opString);
    }

    private static String readPUFile(URL root, String puPath) throws IOException {
        URL puURL = new URL(root, puPath + "/classes/META-INF/pu/pu.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(puURL.openStream()));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        String puString = buffer.toString();
        return puString;
    }

    private static String getCodebase(DeployAdmin deployAdmin) throws MalformedURLException, RemoteException {
        URL url = ((ServiceAdmin) deployAdmin).getServiceElement().getExportURLs()[0];
        String codeserver = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/";
        return codeserver;
    }

    private static void deployLocal(SLA sla, String puString) {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setNumberOfInstances(new Integer(sla.getNumberOfInstances()));
        clusterInfo.setNumberOfBackups(new Integer(sla.getNumberOfBackups()));
        clusterInfo.setSchema(sla.getClusterSchema());

        clusterInfo.setInstanceId(new Integer(1));
        //create PU Container
        Resource resource = new ByteArrayResource(puString.getBytes());
        IntegratedProcessingUnitContainerProvider factory = new IntegratedProcessingUnitContainerProvider();
        factory.addConfigLocation(resource);
        factory.setClusterInfo(clusterInfo);
        IntegratedProcessingUnitContainer integratedContainer = (IntegratedProcessingUnitContainer) factory.createContainer();
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) throws Exception {
        //these must be manually changed to reflect your environment, for now
//        String puFilename = "/Users/ming/Gigaspaces/cvs/openspaces/config/pu/pu.xml";
        String jiniGroup = System.getProperty("com.gs.jini_lus.groups");
        System.out.println("jiniGroup = " + jiniGroup);

        System.setSecurityManager(new RMISecurityManager() {
            public void checkPermission(java.security.Permission perm) {
            }

            public void checkPermission(java.security.Permission perm, Object context) {
            }
        });

        //find gsm and codebase
        GSM gsm = ProvisionerFinder.find(null, 5000, new String[]{jiniGroup});
        DeployAdmin deployAdmin = (DeployAdmin) gsm.getAdmin();
        String codeserver = getCodebase(deployAdmin);
        System.out.println("codeserver = " + codeserver);

        //get pu xml
        String puPath = "helloworld";
        int index = puPath.lastIndexOf('/');
        index = index == -1 ? 0 : index;
        String puName = puPath.substring(index);
        System.out.println("puName = " + puName);

        URL root = new URL(codeserver);
        HTTPFileSystemView view = new HTTPFileSystemView(root);
        File puHome = view.createFileObject(puPath);
        File lib = view.createFileObject(puHome, "lib");
        File[] jars = view.getFiles(lib, false);
        System.out.println("jars = " + Arrays.asList(jars));

        //read pu xml
        String puString = readPUFile(root, puPath);
        System.out.println("puString = " + puString);
        SLA sla = SLAUtil.loadSLA(puString);
        System.out.println("sla = " + sla);

        //deploy to sg
        OperationalString opString = loadDeployment(puString, codeserver, jiniGroup, sla, jars, puPath, puName);
        Map result = deployAdmin.deploy(opString);
//        System.out.println("result = " + result);

//        deployLocal(sla, puString);
    }
}
