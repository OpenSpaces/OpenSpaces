/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.alert.config.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.alert.config.AlertConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple XML alert configuration parser.
 * <p>
 * The alert configurations are read from an XML file. For example, the following alert.xml file
 * specifies a CPU Utilization alert configuration, which is enabled, and has three configuration
 * properties.
 * 
 * <pre>
 * {@code
 * <alerts>
 *     <alert
 *         class="org.openspaces.admin.alert.config.CpuUtilizationAlertConfiguration"
 *         enabled="true">
 *         <property key="high-threshold-perc" value="80" />
 *         <property key="low-threshold-perc" value="60" />
 *         <property key="measurement-period-milliseconds" value="60000" />
 *     </alert>
 *     
 *     <alert ...> 
 *          ...
 *     </alert>
 *     
 * </alerts>
 * }
 * </pre>
 * <p>
 * The alert XML configuration file can be located using a direct path to the file, or by reference to a resource located in the classpath.
 * The default classpath config directory is {@value #DEFAULT_ALERT_CONFIG_DIRECTORY} and the default alert XML configuration file name is {@value #DEFAULT_ALERT_RESOURCE_NAME}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class XmlAlertConfigurationParser implements AlertConfigurationParser {

    private static final Log logger = LogFactory.getLog(XmlAlertConfigurationParser.class);
    /** default alert classpath relative configuration directory */
    public static final String DEFAULT_ALERT_CONFIG_DIRECTORY = "config/alerts/";
    /** default alert configuration file name */
    public static final String DEFAULT_ALERT_RESOURCE_NAME = "alerts.xml";
    
    private final InputStream is;
    
    public XmlAlertConfigurationParser() throws AlertConfigurationParserException {
        //look for alerts.xml
        String resourceName = DEFAULT_ALERT_RESOURCE_NAME;
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to locate " + resourceName + " in classpath");
        }
        
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (resourceAsStream == null) {
            //look for config/alerts/alerts.xml
        	String altResourceName = DEFAULT_ALERT_CONFIG_DIRECTORY+DEFAULT_ALERT_RESOURCE_NAME;
        	if (logger.isDebugEnabled()) {
                logger.debug("Failed to locate " + resourceName +". Trying to locate " + altResourceName + " in classpath");
            }
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(altResourceName);
        }
        
        if (resourceAsStream == null) {
            throw new AlertConfigurationParserException("Could not locate default alerts configuration file ["+DEFAULT_ALERT_RESOURCE_NAME+"]");
        }
        
        this.is = resourceAsStream;
    }
    
    public XmlAlertConfigurationParser(String resourceName) throws AlertConfigurationParserException {
        InputStream resourceAsStream = null;
        //try loading it using direct path, otherwise look it up in the classpath
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to load " + resourceName + " using direct file path");
        }
        File file = new File(resourceName);
        if (file.exists()) {
            try {
                resourceAsStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new AlertConfigurationParserException("Failed to find file " + resourceName, e);
            }
        } else {
            //look for <resourceName> in classpath
            if (logger.isDebugEnabled()) {
                logger.debug("Trying to locate " + resourceName + " in classpath");
            }
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            if (resourceAsStream == null) {
                //look for config/alerts/<resourceName>
                resourceName = DEFAULT_ALERT_CONFIG_DIRECTORY+resourceName;
                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to locate " + resourceName + " in classpath");
                }
                resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            }
        }
        
        if (resourceAsStream == null) {
            throw new AlertConfigurationParserException("Could not locate alerts configuration file ["+resourceName+"]");
        }
        
        is = resourceAsStream;
    }
    
    
    @Override
    public AlertConfiguration[] parse() throws AlertConfigurationParserException {
        List<AlertConfiguration> alertConfigurations = new ArrayList<AlertConfiguration>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            docBuilder.setErrorHandler(new SimpleSaxErrorHandler());
            Document doc = docBuilder.parse(new InputSource(is));
            Element alertsElm = doc.getDocumentElement();
            List<Element> list = getChildElementsByTagName(alertsElm, "alert");
            for (Element alertElm : list) {
                String classAttr = alertElm.getAttribute("class");
                String enabledAttr = alertElm.getAttribute("enabled");
                if (logger.isTraceEnabled()) {
                    logger.trace("alert: class=" + classAttr + " enabled=" + enabledAttr);
                    logger.trace("properties: ");
                }

                Map<String, String> properties = new HashMap<String, String>();
                List<Element> propertiesElm = getChildElementsByTagName(alertElm, "property");
                for (Element property : propertiesElm) {
                    String keyAttr = property.getAttribute("key");
                    String valueAttr = property.getAttribute("value");
                    properties.put(keyAttr, valueAttr);
                    if (logger.isTraceEnabled()) {
                        logger.trace("property: key="+keyAttr + " value=" + valueAttr);
                    }
                }
                
                Class<? extends AlertConfiguration> clazz = Thread.currentThread().getContextClassLoader().loadClass(classAttr).asSubclass(AlertConfiguration.class);
                AlertConfiguration alertConfiguration = clazz.newInstance();
                alertConfiguration.setEnabled(Boolean.parseBoolean(enabledAttr));
                alertConfiguration.setProperties(properties);
                alertConfigurations.add(alertConfiguration);
            }
        } catch (Throwable t) {
            throw new AlertConfigurationParserException("Failed to parse configuration file", t);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
        
        return alertConfigurations.toArray(new AlertConfiguration[alertConfigurations.size()]);
    }
    
    private static class SimpleSaxErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException ex) throws SAXException {
            logger.warn("Ignored XML validation warning [" + ex.getMessage() + "]", ex);
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

    }
    
    private static List<Element> getChildElementsByTagName(Element ele, String childEleName) {
        NodeList nl = ele.getChildNodes();
        List<Element> childEles = new ArrayList<Element>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            String name = node.getNodeName();
            if (node instanceof Element && childEleName.equals(name)) {
                childEles.add((Element) node);
            }
        }
        return childEles;
    }
}
