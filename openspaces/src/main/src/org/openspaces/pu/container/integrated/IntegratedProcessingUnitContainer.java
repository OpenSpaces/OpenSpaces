package org.openspaces.pu.container.integrated;

import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.rmi.RMISecurityManager;

/**
 * @author kimchy
 */
public class IntegratedProcessingUnitContainer implements ProcessingUnitContainer {

    private ApplicationContext applicationContext;

    public IntegratedProcessingUnitContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void close() throws CannotCloseContainerException {
        if (applicationContext instanceof DisposableBean) {
            try {
                ((DisposableBean) applicationContext).destroy();
            } catch (Exception e) {
                throw new CannotCloseContainerException("Failed to close container with application context [" + applicationContext + "]", e);
            }
        }
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

        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.setBeanLevelProperties(BeanLevelPropertiesParser.parse(params));
        provider.setClusterInfo(ClusterInfoParser.parse(params));

        // parse the config location parameters
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equalsIgnoreCase("config")) {
                for (int j = 0; j < params[i].getArguments().length; j++) {
                    provider.addConfigLocation(params[i].getArguments()[j]);
                }
            }
        }

        IntegratedProcessingUnitContainer container = (IntegratedProcessingUnitContainer) provider.createContainer();
        ((ConfigurableApplicationContext) container.getApplicationContext()).registerShutdownHook();
    }
}
