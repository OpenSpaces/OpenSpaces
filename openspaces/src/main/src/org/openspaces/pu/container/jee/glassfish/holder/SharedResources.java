package org.openspaces.pu.container.jee.glassfish.holder;

import com.j_spaces.kernel.Environment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * Shared resources for shared Glassfish instance
 *
 * @author kimchy
 */
public class SharedResources {

    private static final Log logger = LogFactory.getLog(SharedResources.class);

    private static File glassfishGlobalWorkDir;
    private static File glassfishInstanceWorkDir;
    // HACK to manage ports, store a JVM level file lock with the port number as file
    // this means that once started, Glassfish will use that port even when stopped.
    // in glassfish (the version we use), even when stopping the server causes it to still use the same port
    private static RandomAccessFile portFile;
    private static FileLock portFileLock;
    private static int portNumber;

    static {
        glassfishGlobalWorkDir = new File(System.getProperty("com.gs.work", Environment.getHomeDirectory() + "/work") + "/glassfish");
        File portDir = new File(glassfishGlobalWorkDir, "ports");
        portDir.mkdirs();
        int startFromPort = Integer.getInteger("com.gs.glassfish.port", 9008);
        int retryCount = 20;
        for (int i = 0; i < retryCount; i++) {
            portNumber = startFromPort + i;
            File portF = new File(portDir, portNumber + ".port");
            try {
                portFile = new RandomAccessFile(portF, "rw");
            } catch (FileNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to open port marker file [" + portF.getAbsolutePath() + "]", e);
                }
                // can't get this one, continue
                continue;
            }
            try {
                portFileLock = portFile.getChannel().tryLock();
                if (portFileLock == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Can't get lock for [" + portF.getAbsolutePath() + "], try another");
                    }
                    continue;
                }
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Faield to get lock file for [" + portF.getAbsolutePath() + "]", e);
                }
                // failed to get the lock, continue
                continue;
            }
            // all is well, break
            break;
        }
        if (portFileLock == null) {
            portNumber = -1;
            throw new IllegalStateException("Failed to acquire port file lock, tried for [" + retryCount + "], basePort [" + startFromPort + "]");
        }
        glassfishInstanceWorkDir = new File(glassfishGlobalWorkDir, "" + portNumber);
        glassfishInstanceWorkDir.mkdirs();
        System.setProperty("com.gs.glassfish.port", "" + portNumber);
        logger.info("Glassfish instance will use port [" + portNumber + "] and location [" + glassfishInstanceWorkDir.getAbsolutePath() + "]");
    }

    public static int getPortNumber() {
        return portNumber;
    }

    public static File getGlassfishGlobalWorkDir() {
        return glassfishGlobalWorkDir;
    }

    public static File getGlassfishInstanceWorkDir() {
        return glassfishInstanceWorkDir;
    }
}
