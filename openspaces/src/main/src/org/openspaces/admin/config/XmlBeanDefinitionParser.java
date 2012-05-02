/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.config;

import java.lang.reflect.Method;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author itaif
 *
 */
public class XmlBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private Class<?> clazz;

    @Override
    protected Class<?> getBeanClass(Element element) {
        return clazz;
    }
    
    public XmlBeanDefinitionParser(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            
            Method method = findSetterMethodForAttribute(attribute.getName());
            if (super.isEligibleAttribute(attribute, parserContext) &&
                method != null &&
                method.getReturnType().getPackage().getName().startsWith("org.openspaces.admin")) {
                String propertyName = extractPropertyName(attribute.getLocalName());
                Assert.state(StringUtils.hasText(propertyName),
                        "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
                builder.addPropertyValue(propertyName, attribute.getValue());
            }
        }
        super.doParse(element, parserContext, builder);
    }
    
    @Override
    protected boolean isEligibleAttribute(String attributeName) {
        return super.isEligibleAttribute(attributeName) && 
               isSetterMethodForAttribute(attributeName);
    }

    private boolean isSetterMethodForAttribute(String attributeName) {
        return findSetterMethodForAttribute(attributeName) != null;
    }

    private Method findSetterMethodForAttribute(String attributeName) {
        final String expectedPropertyName = extractPropertyName(attributeName);
        final String expectedMethodName = "set"+(""+expectedPropertyName.charAt(0)).toUpperCase()+ expectedPropertyName.substring(1);
        for (final Method method : clazz.getMethods()) {
            if (method.getAnnotation(XmlProperty.class) != null &&
                method.getName().equals(expectedMethodName) && 
                method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }
}
