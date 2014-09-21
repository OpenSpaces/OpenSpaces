/*******************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces. 
 * You may use the software source code solely under the terms and limitations of 
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
/**
 *
 */
package org.openspaces.persistency.hibernate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;

import com.j_spaces.kernel.ClassLoaderHelper;


/**
 * Utility class - used to create and configure hibernate session factory 
 * @author anna
 * @since 6.0
 * @deprecated use org.openspaces.persistency.hibernate package instead
 */
@Deprecated
public class SessionFactoryBuilder
{
    private static final String HIBERNATE_NAMING_STRATEGY = "hibernate.naming_strategy";
    /**
     *
     */
    private static final String ANNOTATION_CONFIGURATION_CLASS = "org.hibernate.cfg.AnnotationConfiguration";
    private final static Logger _logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_PERSISTENT);


    /**
     * Create and configure new hibernate session factory
     * @param hibernateFile
     * @return
     * @throws Exception
     */
    public static SessionFactory getFactory(String hibernateFile)
            throws Exception
    {
        Configuration config = null;

        try
        {
            // load the class using reflection to avoid JIT exceptions  
            config = configure((Configuration) ClassLoaderHelper.loadClass(ANNOTATION_CONFIGURATION_CLASS).newInstance(),
                    hibernateFile);
        }
        catch (Throwable t)
        {
            try
            {
                config = configure(new Configuration(), hibernateFile);
            }
            catch (Exception e)
            {
                // if both methods failed - log first exception
                // and throw the second
                if(_logger.isLoggable(Level.SEVERE))
                {
                    _logger.log(Level.SEVERE, "Failed to configure using hibernate annotations.",t);

                }

                throw e;
            }
        }
        // since hibernate doesn't support configuring naming strategies in cfg.xml.
        // added an option to configure it programatically while using the hibernate.cfg.xml
        // for example: add this to hibernate.cfg.xml
        //<property name="hibernate.naming_strategy">com.gigaspaces.test.persistent.SpaceNamingStrategy</property>

        String namingStrategyClass = config.getProperty(HIBERNATE_NAMING_STRATEGY);

        if(namingStrategyClass != null)
        {
            NamingStrategy namingStrategy = (NamingStrategy) ClassLoaderHelper.loadClass(namingStrategyClass).newInstance();
            config.setNamingStrategy(namingStrategy);
        }
        return config.buildSessionFactory();
    }

    /**
     * Configure according to hibernate.cfg.xml
     * @param config
     * @param hibernateFile
     * @return
     */
    private static Configuration configure(Configuration config,
                                           String hibernateFile)
    {
        // In case that hibernate config file location is null find hibernate.cfg.xml
        // file in classpath
        if (hibernateFile == null)
            return config.configure();
        else
            return config.configure(hibernateFile);

    }
}
