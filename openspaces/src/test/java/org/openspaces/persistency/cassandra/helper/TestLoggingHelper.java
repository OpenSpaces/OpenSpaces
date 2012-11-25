package org.openspaces.persistency.cassandra.helper;

import java.util.Properties;

import com.gigaspaces.logger.GSLogConfigLoader;


public class TestLoggingHelper
{

    public static void init()
    {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
        System.setProperty("com.gs.logging.debug", "true");
        
        Properties props = new Properties();
        
        props.setProperty("org.apache.cassandra.level", "WARNING");
        props.setProperty("me.prettyprint.cassandra.level", "WARNING");
        
        GSLogConfigLoader.getLoader(props);
    }
    
}
