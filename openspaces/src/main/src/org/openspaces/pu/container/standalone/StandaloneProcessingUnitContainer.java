package org.openspaces.pu.container.standalone;

import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.container.support.ConfigLocationParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.rmi.RMISecurityManager;

/**
 * @author kimchy
 */
public class StandaloneProcessingUnitContainer implements ApplicationContextProcessingUnitContainer {

    private StandaloneContainerRunnable containerRunnable;

    public StandaloneProcessingUnitContainer(StandaloneContainerRunnable containerRunnable) {
        this.containerRunnable = containerRunnable;
    }

    public ApplicationContext getApplicationContext() {
        return this.containerRunnable.getApplicationContext();
    }

    public void close() throws CannotCloseContainerException {
        containerRunnable.stop();
        // TODO wait till it shuts down
    }

    public static void main(String[] args) throws Exception {
        // disable security manager (for now)
        System.setSecurityManager(new RMISecurityManager() {
            public void checkPermission(java.security.Permission perm) {
            }

            public void checkPermission(java.security.Permission perm, Object context) {
            }
        });
        CommandLineParser.Parameter[] params = CommandLineParser.parse(args);
        if (params.length == 0) {
            throw new IllegalArgumentException("-pu parameter must be defined");
        }
        String puLocation = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equalsIgnoreCase("pu")) {
                puLocation = params[i].getArguments()[0];
            }
        }
        if (puLocation == null) {
            throw new IllegalArgumentException("-pu parameter must be defined");
        }
        StandaloneProcessingUnitContainerProvider provider = new StandaloneProcessingUnitContainerProvider(puLocation);

        provider.setBeanLevelProperties(BeanLevelPropertiesParser.parse(params));
        provider.setClusterInfo(ClusterInfoParser.parse(params));
        ConfigLocationParser.parse(provider, params);

        StandaloneProcessingUnitContainer container = (StandaloneProcessingUnitContainer) provider.createContainer();
        ((ConfigurableApplicationContext) container.getApplicationContext()).registerShutdownHook();
    }
}
