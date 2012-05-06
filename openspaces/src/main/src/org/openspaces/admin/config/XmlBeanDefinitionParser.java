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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.gigaspaces.internal.utils.StringUtils;

/**
 * @author itaif
 *
 */
public class XmlBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

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

        /* parse properties */
        for (int i = 0; i < attributes.getLength(); i++) {

            Attr attribute = (Attr) attributes.item(i);
            String xmlName = attribute.getName();
            Method setter = findSetterMethodFromXmlName(xmlName);
            if (setter != null) {
                String propertyName = convertSetterMethodToPropertyName(setter);
                String value = attribute.getValue();
                builder.addPropertyValue(propertyName, value);
            }
        }

        /* parse child elements */
        Map<Method,ManagedList<Object>> listValues = new HashMap<Method,ManagedList<Object>>();
        for (Element child : DomUtils.getChildElements(element)) {
            
            String xmlName = child.getLocalName();
            
            Object value = parserContext.getDelegate().parsePropertySubElement(child, builder.getRawBeanDefinition());
            Method setter = findSetterMethodFromXmlName(xmlName);
            if (setter != null) {
             
                if (!isListSetterMethod(setter)) {
                    String propertyName = convertSetterMethodToPropertyName(setter);
                    builder.addPropertyValue(propertyName, value);
                }
                
                else {
                    
                    ManagedList<Object> values = listValues.get(setter);
                    if (values == null) {
                        values = new ManagedList<Object>();
                    }
                    values.add(value);
                    
                    listValues.put(setter, values);
                }
            }
        }
        
        
        for (Entry<Method, ManagedList<Object>> pair : listValues.entrySet()) {
            String propertyName = convertSetterMethodToPropertyName(pair.getKey());
            builder.addPropertyValue(propertyName, pair.getValue());
        }
        
    }
    
    /**
     * @param propertyName
     * @return
     */
    private String pluralOf(String propertyName) {
        if (propertyName.endsWith("y")) {
            return propertyName.substring(0, propertyName.length()-1)+"ies";
        }
        return propertyName+"s";
    }  
    

    /**
     * Auto generating bean ids to avoid the error
     * Configuration problem: Id is required for element when used as a top-level tag
     */
    @Override
    protected boolean shouldGenerateIdAsFallback() {
      return true;
    }
        
    private Method findSetterMethodFromXmlName(String xmlName) {
        
        for (final Method method : clazz.getMethods()) {
            XmlProperty annotation = method.getAnnotation(XmlProperty.class);
            if (annotation != null &&
                method.getParameterTypes().length == 1) {
                
                List<String> annotationValue = Arrays.asList(StringUtils.commaDelimitedListToStringArray(annotation.value()));
                if (!annotationValue.isEmpty()) {
                    if (annotationValue.contains(xmlName)) {
                        //xmlName matches the setter method annotation value.
                        return method;
                    }
                    else {
                        //not found in method annotation value
                    }
                }
                else if (isXmlNameMatchesMethodName(xmlName, method)) {
                    // no method annotation value, but xmlName matches method name.
                    return method;
                }
            }
        }
        return null;
    }

    private boolean isXmlNameMatchesMethodName(final String xmlName, Method method) {
        
        String propertyName = Conventions.attributeNameToPropertyName(xmlName);
        if (isListSetterMethod(method)) {
            propertyName = pluralOf(propertyName);
        }
        String methodName =  convertPropertyNameToSetterMethodName(propertyName);
        return method.getName().equals(methodName);
    }

    private String convertPropertyNameToSetterMethodName(String propertyName) {
        return "set"+String.valueOf(propertyName.charAt(0)).toUpperCase()+ propertyName.substring(1);
    }
    
    private String convertSetterMethodToPropertyName(Method method) {
        String methodName = method.getName();
        if (!methodName.startsWith("set")) {
            throw new IllegalArgumentException(methodName + " does not start with 'set':" + methodName);
        }
        return String.valueOf(methodName.charAt(3)).toLowerCase() + methodName.substring(4);
    }

    private boolean isListSetterMethod(Method method) {
        return method.getParameterTypes()[0].equals(List.class);
    }

}
