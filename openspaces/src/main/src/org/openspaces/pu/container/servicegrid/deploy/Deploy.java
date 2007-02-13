package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpStringLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RMISecurityManager;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Jan 28, 2007
 * Time: 11:24:32 AM
 */
public class Deploy {
// -------------------------- STATIC METHODS --------------------------

    public static String getExportCodebase(Object service) {
        URLClassLoader cl = (URLClassLoader) service.getClass().getClassLoader();
        URL[] urls = cl.getURLs();
        String exportCodebase = urls[0].toExternalForm();
        if (exportCodebase.indexOf(".jar") != -1) {
            int index = exportCodebase.lastIndexOf('/');
            if (index != -1)
                exportCodebase = exportCodebase.substring(0, index + 1);
        } else {
            System.out.println("Cannot determine export codebase");
        }
        /*
         * TODO: If the exportCodebase starts with httpmd, replace
         * httpmd with http. Need to figure out a mechanism to use the
         * httpmd in a better way
         */
        if (exportCodebase.startsWith("httpmd")) {
            exportCodebase = "http" + exportCodebase.substring(6);
        }
        return (exportCodebase);
    }

    static OperationalString loadDeployment(File deployFile, String puString, String codeserver) throws Exception {
        OperationalString opString = null;
        if (!deployFile.exists()) {
            throw new IllegalArgumentException(deployFile + " Not Found");
        }

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader(/*GS.class.getClassLoader()*/);
        opStringLoader.setDefaultGroups(new String[]{"test"});
//        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(deployFile)[0];

        //put the entire pu spring xml as parameter to servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];
        element.getServiceBeanConfig().addInitParameter("pu", puString);
        return (opString);
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) throws Exception {
        //these must be manually changed to reflect your environment, for now
        String puFilename = "/Users/ming/Gigaspaces/cvs/openspaces/config/pu/pu.xml";
        String puservicebeanFilename = "/Users/ming/Gigaspaces/cvs/openspaces/config/pu/springservicebean.xml";
        String jiniGroup = "ming";

        System.setSecurityManager(new RMISecurityManager() {
            public void checkPermission(java.security.Permission perm) {
            }

            public void checkPermission(java.security.Permission perm, Object context) {
            }
        });

        //read pu xml
//        GenericApplicationContext applicationContext = new GenericApplicationContext();
//        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
//
        BufferedReader reader = new BufferedReader(
                new FileReader(
                        new File(puFilename)
                )
        );

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
//        FileSystemResource fileSystemResource = new FileSystemResource(puFile);
//        xmlReader.loadBeanDefinitions(fileSystemResource);
//
//        applicationContext.getBean("gs");
        //generate opstring
        File deployFile = new File(
                puservicebeanFilename
        );
        GSM gsm = ProvisionerFinder.find(null, 5000, new String[]{jiniGroup});
        String codeserver = getExportCodebase(gsm);
        OperationalString opString = loadDeployment(deployFile, buffer.toString(), codeserver);

        //deploy to sg
        DeployAdmin deployAdmin = (DeployAdmin) gsm.getAdmin();
        Map result = deployAdmin.deploy(opString);
        System.out.println("result = " + result);
    }
}
