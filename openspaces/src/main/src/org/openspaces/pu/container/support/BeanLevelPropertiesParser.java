package org.openspaces.pu.container.support;

import org.openspaces.core.config.BeanLevelProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Properties;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author kimchy
 */
public abstract class BeanLevelPropertiesParser {

    public static String EMBEDDED_PROPERTIES_PREFIX = "embed://";

    public static BeanLevelProperties parse(CommandLineParser.Parameter[] params) throws IllegalArgumentException {
        BeanLevelProperties beanLevelProperties = null;
        for (int i = 0; i < params.length; i++) {
            if (!params[i].getName().equalsIgnoreCase("properties")) {
                continue;
            }

            if (beanLevelProperties == null) {
                beanLevelProperties = new BeanLevelProperties();
            }
            String name = null;
            String properties;
            if (params[i].getArguments().length == 1) {
                properties = params[i].getArguments()[0];
            } else if (params[i].getArguments().length == 2) {
                name = params[i].getArguments()[0];
                properties = params[i].getArguments()[1];
            } else {
                throw new IllegalArgumentException("-config can accept only one or two values, not more and not less");
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
                Resource resource = new DefaultResourceLoader().getResource(properties);
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
