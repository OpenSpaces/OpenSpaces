/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.pu.container.servicegrid.puservicebean;

import org.jini.rio.jsb.ServiceBeanAdapter;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
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
    private IntegratedProcessingUnitContainer integratedContainer;

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface ServiceBeanAdapterMBean ---------------------

    public void advertise() throws IOException {
        String springXML = (String) context.getInitParameter("pu");
        startPU(springXML);

        super.advertise();
    }

    public void unadvertise() {
        super.unadvertise();

        stopPU();
    }

// -------------------------- OTHER METHODS --------------------------

    private void startPU(String springXml) throws MalformedURLException {
        System.out.println("PUServiceBeanImpl.startPU:\n" + springXml);

        Resource resource = new ByteArrayResource(springXml.getBytes());
        IntegratedProcessingUnitContainerProvider factory = new IntegratedProcessingUnitContainerProvider();
        factory.addConfigLocation(resource);

/*
        BeanLevelProperties beanLevelProperties = new BeanLevelProperties();
        beanLevelProperties.getBeanProperties("testBean1").setProperty("prop.value", "testme");
        factory.setBeanLevelProperties(beanLevelProperties);
*/

        integratedContainer = (IntegratedProcessingUnitContainer) factory.createContainer();
    }

    private void stopPU() {
        try {
            integratedContainer.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }
}
