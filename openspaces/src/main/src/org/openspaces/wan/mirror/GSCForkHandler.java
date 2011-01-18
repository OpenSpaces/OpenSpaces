package org.openspaces.wan.mirror;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import com.j_spaces.kernel.Environment;

public class GSCForkHandler {
    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(GSCForkHandler.class.getName());

    private final Admin admin;
    private final ProcessingUnitInstance pui;
    private final int gscPort;

    private final Object myNICAddress;
    private GridServiceContainer gscResult;

    private int discoveryPort;

    private static final String LRMI_PORT_PROPERTY_TEMPLATE =
            "-Dcom.gs.transport_protocol.lrmi.bind-port=<LRMI_PORT>  " +
                    "-Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=<DISCOVERY_PORT>";

    private static final String WAN_MIRROR_GSC_SPAWN_MARKER = "WAN_GSC_MARKER";

    public static final String AGENT_EXT_JAVA_OPTIONS =
            LRMI_PORT_PROPERTY_TEMPLATE +
                    " -D" + WAN_MIRROR_GSC_SPAWN_MARKER + "=<MARKER>";

    public GSCForkHandler(final Admin admin, final int gscPort, final int discoveryPort,
            final ProcessingUnitInstance pui,
            final String myNICAddress) {

        this.admin = admin;
        this.gscPort = gscPort;
        this.pui = pui;
        this.myNICAddress = myNICAddress;
        this.discoveryPort = discoveryPort;
    }

    private String createExtJavaOptionsForGSC(final int gscBindPort,
            final int discoveryPort,
            final String marker) {
        return AGENT_EXT_JAVA_OPTIONS
                    .replace("<LRMI_PORT>", Integer.toString(gscBindPort))
                    .replace("<DISCOVERY_PORT>", Integer.toString(discoveryPort))
                    .replace("<MARKER>", marker);
    }

    private String createLrmiPortProperty(final int gscPort, final int discoveryPort) {
        return LRMI_PORT_PROPERTY_TEMPLATE
            .replace("<LRMI_PORT>", Integer.toString(gscPort))
            .replace("<DISCOVERY_PORT>", Integer.toString(discoveryPort));

    }

    private GridServiceContainer createNewGSCANdWait() {

        final String marker = Long.toString(System.currentTimeMillis());
        logger.info("New GSC will be tagged with: " + marker);

        final Object notifyObject = new Object();
        synchronized (notifyObject) {
            registerForNewGSCNotification(admin, marker, notifyObject);
            startGSCOnPort(gscPort, marker);
            waitForNewGSC(notifyObject);
        }

        return this.gscResult;
    }

    public void moveMirrorToAlternativeGSC() {

        if (!WanUtils.checkPortAvailable(gscPort)) {
            throw new IllegalArgumentException("The required port for the new GSC(" + gscPort + ") is not available!");
        }

        logger.info("Looking up GSA on local machine with address: " + this.myNICAddress);
        final GridServiceAgent gsa = admin.getGridServiceAgents().getHostAddress().get(this.myNICAddress);

        GridServiceContainer gsc = null;

        if (gsa == null) {
            logger.info("Could not find local GSA. Calling script to start GSC.");
            gsc = createNewGSCANdWait();
            logger.info("Created new GSC: " + gsc.getUid());

        } else {
            logger.info("Found local GSA - starting new GSC");
            final GridServiceContainerOptions gsco = new GridServiceContainerOptions();
            final String lrmiProperty = createLrmiPortProperty(gscPort, discoveryPort);
            gsco.vmInputArgument(lrmiProperty);
            gsc = gsa.startGridServiceAndWait(gsco, 30, TimeUnit.SECONDS);
            if (gsc == null) {
                // Did not receive the GSC in the response. Double check in the Admin
                final GridServiceContainer[] containers = gsa.getMachine().getGridServiceContainers().getContainers();
                for (final GridServiceContainer gridServiceContainer : containers) {
                    final String port =
                            gridServiceContainer.getVirtualMachine()
                                .getDetails()
                                .getSystemProperties()
                                .get("com.gs.transport_protocol.lrmi.bind-port");

                    if ((port != null) && port.equals(Integer.toString(gscPort))) {
                        gsc = gridServiceContainer;
                    }

                }
            }

            if (gsc == null) {
                throw new IllegalStateException("Failed to create new GSC for WAN Mirror");
            }

            logger.info("Created new GSC: " + gsc.getUid());

        }

        pui.relocateAndWait(gsc);
        // throw new IllegalStateException("This Mirror instance has been moved to a new GSC.");

    }

    private void registerForNewGSCNotification(final Admin admin, final String marker, final Object notifyObject) {
        logger.info("Registering for new GSC notification");

        admin.getGridServiceContainers()
            .getGridServiceContainerAdded()
            .add(new GridServiceContainerAddedEventListener() {

                public void gridServiceContainerAdded(final GridServiceContainer gridServiceContainer) {
                    logger.info("New GSC Added: " + gridServiceContainer.getUid());

                    final String temp = gridServiceContainer.getVirtualMachine().
                        getDetails().getSystemProperties().get(WAN_MIRROR_GSC_SPAWN_MARKER);
                    logger.info("Marker(" + WAN_MIRROR_GSC_SPAWN_MARKER + ") value for new GSC: " + temp
                            + ", expected Marker Value: " + marker);

                    if ((temp != null) && temp.equals(marker)) {

                        synchronized (notifyObject) {
                            gscResult = gridServiceContainer;
                            notifyObject.notify();
                        }
                        admin.getGridServiceContainers().getGridServiceContainerAdded().remove(this);
                    }
                }
            });
    }

    private void startGSCOnPort(final int lusPort, final String marker) {
        final String os = System.getProperty("os.name").toLowerCase();
        final boolean isWin = os.startsWith("win");

        // output is redirected to null
        String scriptName = "gsc.sh > /dev/null";
        if (isWin) {
            scriptName = "gsc.bat >NUL 2>NUL";
        }

        final String gsHome = Environment.getHomeDirectory();
        if ((gsHome == null) || (gsHome.length() == 0)) {
            throw new IllegalStateException("Could not find the GigaSpaces home directoy.");
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.fine("GigaSpaces Home DIrectory is: " + gsHome);
        }

        final String gsBin = gsHome + "/bin";
        final File binDir = new File(gsBin);

        if (!binDir.exists()) {
            throw new IllegalStateException("Could not find the GigaSpaces bin directory at: " + gsBin);
        }

        final String fullCommandLine = gsBin + "/" + scriptName;

        if (!new File(fullCommandLine).exists()) {
            throw new IllegalStateException("Could not find the Container startup script at: " + fullCommandLine);
        }

        logger.info("Starting GSC with command: " + fullCommandLine);
        final String[] parts = fullCommandLine.split(" ");
        final ProcessBuilder builder =
                new ProcessBuilder(parts);

        builder.directory(binDir);
        final String extJavaOptions = createExtJavaOptionsForGSC(gscPort, discoveryPort, marker);
        logger.info("Setting GSC_JAVA_OPTIONS to: " + extJavaOptions);

        builder.environment().put("GSC_JAVA_OPTIONS", extJavaOptions);

        builder.redirectErrorStream(true);
        try {
            builder.start();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to launch new Container: " + e.getMessage(), e);
            throw new IllegalStateException("Failed to launch new Container: " + e.getMessage(), e);
        }

    }

    private void waitForNewGSC(final Object notifyObject) {
        try {
            if (this.gscResult != null) {
                logger.info("New GSC found");
                return; // new gsc already detected
            }
            logger.info("Waiting for new GSC to start up");
            notifyObject.wait(30000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (this.gscResult == null) {
            throw new IllegalStateException(
                    "New GSC was not discovered in time! Can't start new WAN mirror on required GSC");
        }
    }
}
