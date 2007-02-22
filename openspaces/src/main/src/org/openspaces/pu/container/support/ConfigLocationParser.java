package org.openspaces.pu.container.support;

import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;

import java.io.IOException;

/**
 * Parses multiple -config parameter by adding it to
 * {@link org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider#addConfigLocation(String)}.
 *
 * @author kimchy
 */
public abstract class ConfigLocationParser {

    public static final String CONFIG_PARAMETER = "config";

    /**
     * Parses multiple -config parameter by adding it to
     * {@link org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider#addConfigLocation(String)}.
     *
     * @param containerProvider The container provider to add the config location to
     * @param params            The parameteres to parse for -config options
     * @throws IOException
     */
    public static void parse(ApplicationContextProcessingUnitContainerProvider containerProvider,
                             CommandLineParser.Parameter[] params) throws IOException {
        // parse the config location parameters
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equalsIgnoreCase(CONFIG_PARAMETER)) {
                for (int j = 0; j < params[i].getArguments().length; j++) {
                    containerProvider.addConfigLocation(params[i].getArguments()[j]);
                }
            }
        }
    }
}
