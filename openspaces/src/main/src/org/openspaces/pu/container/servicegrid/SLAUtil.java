package org.openspaces.pu.container.servicegrid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.pu.container.servicegrid.sla.SLA;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 */
public class SLAUtil {
    private static final Log logger = LogFactory.getLog(SLAUtil.class);

    public static SLA loadSLA(String puString) {
        //read sla
        Resource resource = new ByteArrayResource(puString.getBytes());
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
        SLA sla;
        try {
            sla = (SLA) xmlBeanFactory.getBean("SLA");
        } catch (NoSuchBeanDefinitionException e) {
            logger.info("SLA Not Found in PU.  Using Default SLA.");
            sla = new SLA();
        }
        return sla;
    }

}
