package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.SLA;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.SLA}.
 *
 * @author kimchy
 */
public class SLABeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<SLA> getBeanClass(Element element) {
        return SLA.class;
    }

    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        // there can be only one SLA bean within a processing unit application context, and its name must be SLA
        return "SLA";
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String numberOfInstnaces = element.getAttribute("number-of-instances");
        if (StringUtils.hasLength(numberOfInstnaces)) {
            builder.addPropertyValue("numberOfInstances", numberOfInstnaces);
        }

        String numberOfBackups = element.getAttribute("number-of-backups");
        if (StringUtils.hasLength(numberOfBackups)) {
            builder.addPropertyValue("numberOfBackups", numberOfBackups);
        }

        String clusterSchema = element.getAttribute("cluster-schema");
        if (StringUtils.hasLength(clusterSchema)) {
            builder.addPropertyValue("clusterSchema", clusterSchema);
        }

        String maxInstancesPerVm = element.getAttribute("max-instances-per-vm");
        if (StringUtils.hasLength(maxInstancesPerVm)) {
            builder.addPropertyValue("maxInstancesPerVM", maxInstancesPerVm);
        }
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        // policy
        Element scaleUpPolicyEle = DomUtils.getChildElementByTagName(element, "scale-up-policy");
        if (scaleUpPolicyEle != null) {
            builder.addPropertyValue("policy", parserContext.getDelegate().parsePropertySubElement(scaleUpPolicyEle, builder.getRawBeanDefinition()));
        }
        Element relocationPolicyEle = DomUtils.getChildElementByTagName(element, "relocation-policy");
        if (relocationPolicyEle != null) {
            builder.addPropertyValue("policy", parserContext.getDelegate().parsePropertySubElement(relocationPolicyEle, builder.getRawBeanDefinition()));
        }

        // montiors
        Element monitorsEle = DomUtils.getChildElementByTagName(element, "monitors");
        if (monitorsEle != null) {
            List monitors = parserContext.getDelegate().parseListElement(monitorsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("monitors", monitors);
        }

        // requirements
        Element requirementsEle = DomUtils.getChildElementByTagName(element, "requirements");
        if (requirementsEle != null) {
            List requirements = parserContext.getDelegate().parseListElement(requirementsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("requirements", requirements);
        }
    }
}