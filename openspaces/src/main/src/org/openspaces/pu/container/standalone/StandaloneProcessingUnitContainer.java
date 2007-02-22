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
 * <p>A {@link StandaloneProcessingUnitContainer} provider. A standalone processing unit container is a container
 * that understands a processing unit archive structure (both when working with an "exploded" directory and when
 * working with a zip/jar archive of it).
 *
 * <p>The standalone processing unit container also provides a a main method ({@link #main(String[])} which uses
 * the {@link StandaloneProcessingUnitContainerProvider} and the provided
 * parameters create itself. Please see the javadoc for the main method for a full list of the possible paramters
 * values.
 *
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

    /**
     * <p>Allows to run the standalone processing unit container. Uses the
     * {@link StandaloneProcessingUnitContainerProvider} and the parameters
     * provided in order to configure it.
     *
     * <p>The following parameters are allowed:
     * <ul>
     * <li><b>-pu [location]</b>: The location of the procesing unit archive.
     * See {@link org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainerProvider#StandaloneProcessingUnitContainerProvider(String)}. This parameter
     * is required.</li>
     * <li><b>-conifg [configLocation]</b>: Allows to add a Spring application context config location. See
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#addConfigLocation(String)}. This is
     * an optional paramter and it can be provided multiple times.</li>
     * <li><b>-properties [beanName] [propreties]</b>: Allows to inject {@link org.openspaces.core.config.BeanLevelProperties}, see
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#setBeanLevelProperties(org.openspaces.core.config.BeanLevelProperties)}.
     * [beanName] is optional, if not used, the properties will set the {@link org.openspaces.core.config.BeanLevelProperties#setContextProperties(java.util.Properties)}.
     * If used, will inject properties only to the bean registered under the provided beanName within the Spring context (see
     * {@link org.openspaces.core.config.BeanLevelProperties#setBeanProperties(String,java.util.Properties)}). The [properties] can
     * either start with <code>embed://</code> which mean they will be provided within the command line (for example:
     * <code>embed://propName1=propVal1;propName2=propVal2</code>) or they can follow Spring {@link org.springframework.core.io.Resource}
     * lookup based on URL syntax or Spring extended <code>classpath</code> prefix (see {@link org.springframework.core.io.DefaultResourceLoader}).</li>
     * <li><b>-cluster [cluster parameters]</b>: Allows to configure {@link org.openspaces.core.cluster.ClusterInfo}, see
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#setClusterInfo(org.openspaces.core.cluster.ClusterInfo)}.</li>
     * The following parameters are allowed: <code>totalMembers=1,1</code> (1,1 is an example value),
     * <code>id=1</code> (1 is an example value), <code>backupId=1</code> (1 is an example value) and
     * <code>schema=primary_backup</code> (primary_backup is an example value). No parameter is required. For more
     * information regarding the Space meaning of this parameters please consult GigaSpaces reference documentation
     * within the Space URL section.
     * </ul>
     */
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
