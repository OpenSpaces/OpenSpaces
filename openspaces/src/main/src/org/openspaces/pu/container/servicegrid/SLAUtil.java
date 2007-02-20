package org.openspaces.pu.container.servicegrid;

import org.openspaces.pu.container.servicegrid.sla.SLA;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 16, 2007
 * Time: 10:40:24 AM
 */
public class SLAUtil {
    private static final Logger LOGGER = Logger.getLogger(SLAUtil.class.getName());

    public static SLA loadSLA(String puString) {
        //read sla
        Resource resource = new ByteArrayResource(puString.getBytes());
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
        SLA sla;
        try {
            sla = (SLA) xmlBeanFactory.getBean("SLA");
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.log(Level.INFO, "SLA Not Found in PU.  Using Default SLA.");
            sla = new SLA();
        }
        return sla;
    }

}
