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

package org.openspaces.pu.container.support;

import org.openspaces.core.properties.BeanLevelProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A {@link org.openspaces.core.properties.BeanLevelProperties} parser that parses -properties
 * parameter(s) and transforms it into bean level properties. The format of the command is
 * <code>-properties [beanName] [properties]</code>.
 *
 * <p>
 * [beanName] is optional, if not used, the properties will set the
 * {@link org.openspaces.core.properties.BeanLevelProperties#setContextProperties(java.util.Properties)}.
 * If used, will inject properties only to the bean registered under the provided beanName within
 * the Spring context (see
 * {@link org.openspaces.core.properties.BeanLevelProperties#setBeanProperties(String,java.util.Properties)}).
 * The [properties] can either start with <code>embed://</code> which mean they will be provided
 * within the command line (for example: <code>embed://propName1=propVal1;propName2=propVal2</code>)
 * or they can follow Spring {@link org.springframework.core.io.Resource} lookup based on URL syntax
 * or Spring extended <code>classpath</code> prefix (see
 * {@link org.springframework.core.io.DefaultResourceLoader}).
 *
 * @author kimchy
 */
public abstract class BeanLevelPropertiesParser {

    public static String EMBEDDED_PROPERTIES_PREFIX = "embed://";

    public static BeanLevelProperties parse(CommandLineParser.Parameter[] params) throws IllegalArgumentException {
        BeanLevelProperties beanLevelProperties = null;
        for (CommandLineParser.Parameter param : params) {
            if (!param.getName().equalsIgnoreCase("properties")) {
                continue;
            }

            if (beanLevelProperties == null) {
                beanLevelProperties = new BeanLevelProperties();
            }
            String name = null;
            String properties;
            if (param.getArguments().length == 1) {
                properties = param.getArguments()[0];
            } else if (param.getArguments().length == 2) {
                name = param.getArguments()[0];
                properties = param.getArguments()[1];
            } else {
                throw new IllegalArgumentException("-properties can accept only one or two values, not more and not less");
            }
            Properties props = new Properties();
            if (properties.startsWith(EMBEDDED_PROPERTIES_PREFIX)) {
                properties = properties.substring(EMBEDDED_PROPERTIES_PREFIX.length());
                StringTokenizer tokenizer = new StringTokenizer(properties, ";");
                while (tokenizer.hasMoreTokens()) {
                    String property = tokenizer.nextToken();
                    int equalsIndex = property.indexOf("=");
                    if (equalsIndex == -1) {
                        props.setProperty(property, "");
                    } else {
                        props.setProperty(property.substring(0, equalsIndex), property.substring(equalsIndex + 1));
                    }
                }
            } else {
                Resource resource = new DefaultResourceLoader() {
                    // override the default load from the classpath to load from the file system
                    protected Resource getResourceByPath(String path) {
                        return new FileSystemResource(path);
                    }
                }.getResource(properties);
                try {
                    InputStream is = resource.getInputStream();
                    props.load(is);
                    is.close();
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to load resoruce [" + properties + "] " + e.getMessage());
                }
            }
            if (name == null) {
                beanLevelProperties.setContextProperties(props);
            } else {
                beanLevelProperties.setBeanProperties(name, props);
            }
        }
        return beanLevelProperties;

    }
}
